@file:Suppress("UnstableApiUsage")

import abstractor.AbstractionManifest
import abstractor.AbstractionManifestSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import metautils.asm.readToClassNode
import metautils.asm.writeTo
import metautils.util.*
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.processors.JarProcessor
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode
import java.nio.file.Path
import kotlin.system.measureTimeMillis


class FebbGradle : Plugin<Project> {
    override fun apply(project: Project) {
        InitProjectContext(project).apply()
    }
}

open class FebbGradleExtension {
    var minecraftVersion: String? = null
    var yarnBuild: String? = null
    var febbBuild: String? = null
    internal var dependenciesAdded: Boolean = false

    @JvmOverloads
    fun addDependencies(project: Project, setMappings: Boolean = true) {
        require(minecraftVersion != null && yarnBuild != null && febbBuild != null) {
            "minecraftVersion, yarnBuild and febbBuild must be set BEFORE the addDependencies() call in the febb {} block."
        }

        project.repositories.maven {
            //TODO: remove when we have jcenter
            it.name = "F2BB Maven"
            it.setUrl("https://dl.bintray.com/febb/maven")
        }

        operator fun String.invoke(notation: String) = project.dependencies.add(this, notation)

        val path = "io.github.febb:api:$minecraftVersion+$yarnBuild-$febbBuild"
        "commonMainCompileOnly"("$path:api")
        "commonMainCompileOnly"("$path:api-sources")
        "modRuntime"("$path:impl-fabric")
        "minecraft"("com.mojang:minecraft:$minecraftVersion")
        if (setMappings) "mappings"("net.fabricmc:yarn:$minecraftVersion+build.$yarnBuild:v2")

        dependenciesAdded = true
    }
}


private fun FebbGradleExtension.validate() {
    require(minecraftVersion != null) { "minecraftVersion must be set in a febb {} block!" }
    require(yarnBuild != null) { "yarnBuild must be set in a febb {} block!" }
    require(febbBuild != null) { "febbBuild must be set in a febb {} block!" }
}


interface ProjectContext {
    val project: Project
}

inline fun <reified T> ProjectContext.getExtension(): T = project.extensions.getByType(T::class.java)
fun ProjectContext.getSourceSets(): SourceSetContainer = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets

private class InitProjectContext(override val project: Project) : ProjectContext {

    fun apply() {
        val febb = project.extensions.create("febb", FebbGradleExtension::class.java)

        addJarProcessor(febb)
        addSourceSets()

        project.afterEvaluate { AfterEvaluateContext(it, febb).afterEvaluate() }
    }

    private fun addJarProcessor(febb: FebbGradleExtension) {
        val loom = getExtension<LoomGradleExtension>()
        loom.addProcessor(FebbJarProcessor(project, febb))
    }

    private fun addSourceSets(): Unit = with(getSourceSets()) {
        val commonMain = create("commonMain")
        val main = getByName("main")
        val fabricMain = create("fabricMain").apply {
            compileClasspath += main.compileClasspath + commonMain.compileClasspath + commonMain.output
            runtimeClasspath += main.runtimeClasspath + commonMain.output
        }

        project.tasks.withType(Jar::class.java) {
            if (it.name == "jar") {
                it.from(commonMain.output)
                it.from(fabricMain.output)
            }
        }

        project.tasks.withType(ProcessResources::class.java) {
            if (it.name == fabricMain.processResourcesTaskName) {
                it.from(commonMain.resources.srcDirs)
            }
        }
    }
}

private class AfterEvaluateContext(override val project: Project, private val febb: FebbGradleExtension) : ProjectContext {
    fun afterEvaluate() {
        require(febb.dependenciesAdded) { "addDependencies(project) must be called at the end of the febb {} block!" }
    }
}

private class FebbJarProcessor(private val project: Project, private val febb: FebbGradleExtension) : JarProcessor {

    override fun processRemapped(project: Project, task: RemapJarTask, remapper: Remapper, jar: Path) {
        //TODO: remap array constructors to .array() calls
    }

    override fun processInput(project: Project, from: Path, to: Path): Boolean {
        println("Attaching f2bb interfaces")

        val devManifest = getDevManifest()

        val time = measureTimeMillis {
            from.processJar(to, {
                devManifest.containsKey(it.toString().removeSurrounding("/", ".class"))
            }) { classNode ->
                val manifestValue = devManifest.getValue(classNode.name)
                classNode.interfaces.add(manifestValue.apiClassName)
                classNode.signature = manifestValue.newSignature
            }
        }

        to.addF2bbVersionMarker()

        println("Attached interfaces in $time millis")

        return false

    }

    private fun getDevManifest(): AbstractionManifest = with(febb) {
        validate()
        val jar = project.configurations.detachedConfiguration(project.dependencies.create(
                "io.github.febb:api:$minecraftVersion+$yarnBuild-$febbBuild:dev-manifest"
        )).resolve().first()

        val json = jar.toPath().openJar {
            it.getPath("/abstractionManifest.json").readToString()
        }
        return@with Json(JsonConfiguration.Stable).parse(AbstractionManifestSerializer, json)
    }

    private inline fun <T> Path.getF2bbVersionMarkerPath(usage: (Path) -> T) = openJar {
        usage(it.getPath("/f2bb_version.txt"))
    }

    private fun Path.addF2bbVersionMarker() = with(febb) {
        getF2bbVersionMarkerPath {
            it.writeString(versionMarker())
        }
    }

    private fun FebbGradleExtension.versionMarker() = "$minecraftVersion**$yarnBuild**$febbBuild"

    override fun isUpToDate(project: Project, path: Path): Boolean = with(febb) {
        path.getF2bbVersionMarkerPath {
            if (!it.exists()) return false
            return it.readToString() == versionMarker()
        }
    }

}

private fun Path.processJar(dest: Path, filter: (Path) -> Boolean, processor: (ClassNode) -> Unit) {
    dest.deleteIfExists()
    dest.createParentDirectories()
    dest.createJar()
    dest.openJar { destJar ->
        val destRoot = destJar.getPath("/")
        walkJar { classes ->
            classes.forEach { classFile ->
                val destPath = destRoot.resolve(classFile.toString())
                if (!classFile.isDirectory()) {
                    destPath.createParentDirectories()
                    if (classFile.isClassfile() && filter(classFile)) {
                        val node = readToClassNode(classFile)
                        processor(node)
                        node.writeTo(destPath)
                    } else {
                        classFile.copyTo(destPath)
                    }
                }
            }
        }
    }

}
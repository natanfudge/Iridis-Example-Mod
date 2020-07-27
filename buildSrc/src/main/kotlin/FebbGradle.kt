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

private class InitProjectContext(override val project: Project) : ProjectContext {

    fun apply() {
        val loom = getExtension<LoomGradleExtension>()
        val febb = project.extensions.create("febb", FebbGradleExtension::class.java)
//        val devManifestConfig = project.configurations.maybeCreate("devManifest")
//        val devManifestDep = project.dependencies.add("devManifest",
//                "io.github.febb:api:${febb.minecraftVersion}+${febb.yarnBuild}-${febb.febbBuild}:api-sources"
//        )!!
//        devManifestDep.
        loom.addProcessor(FebbJarProcessor(project, febb))

        project.afterEvaluate { AfterEvaluateContext(it).afterEvaluate() }

    }
}

private class AfterEvaluateContext(override val project: Project) : ProjectContext {
    fun afterEvaluate() {
//        with(getExtension<FebbGradleExtension>()){
//            require(minecraftVersion != null) {"minecraftVersion must be set in a febb {} block!"}
//            require(yarnBuild != null) {"yarnBuild must be set in a febb {} block!"}
//            require(febbBuild != null) {"febbBuild must be set in a febb {} block!"}
//        }
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
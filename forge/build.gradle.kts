import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.UserDevExtension


plugins {
    java
    id("net.minecraftforge.gradle") version "3.0.170"
}

val minecraftVersion = "1.15.2"
val nextMajorMinecraftVersion: String = minecraftVersion.split('.').let { (useless, major) ->
    "$useless.${major.toInt() + 1}"
}
val mappingsMinecraftVersion = "1.15.1"
val forgeVersion = "31.0.14"


repositories {
    maven(url = "https://repo.spongepowered.org/maven")
}

dependencies {
    compile(project(":common"))

    minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    implementation(fg.deobf("io.github.ladder:ladder-impl-forge:1.0-SNAPSHOT"))
}


configure<UserDevExtension> {
    mappings(mapOf(
            "channel" to "snapshot",
            "version" to "20200201-$mappingsMinecraftVersion"
    ))


    runs {
        val runConfig = Action<RunConfig> {
            properties(mapOf(
                    "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
                    "forge.logging.console.level" to "debug"
            ))
            workingDirectory = project.file("run").canonicalPath
            source(sourceSets["main"])
        }
        create("client", runConfig)
        create("server", runConfig)
    }

}

configure<BasePluginConvention> {
    archivesBaseName = "$archivesBaseName-mc$minecraftVersion"
}

tasks.named<Copy>("processResources") {
    // this will ensure that this task is redone when the versions change.
    val properties = mapOf(
            "version" to version,
            "forgeVersion" to forgeVersion,
            "minecraftVersion" to minecraftVersion,
            "nextMajorMinecraftVersion" to nextMajorMinecraftVersion
    )
    properties.forEach { (key, value) ->
        inputs.property(key, value)
    }

    // replace stuff in mcmod.info, nothing else
    from(sourceSets["main"].resources.srcDirs) {
        include("META-INF/mods.toml")

        // replace version and mcversion
        expand(properties)
    }

    // copy everything else except the mcmod.info
    from(sourceSets["main"].resources.srcDirs) {
        exclude("META-INF/mods.toml")
    }

    from(project(":common").tasks.named("processResources"))
}



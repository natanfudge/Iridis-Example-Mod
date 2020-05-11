import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.UserDevExtension


plugins {
    java
    id("net.minecraftforge.gradle") version "3.0.170"
}

fun prop(key: String): String = project.property(key).toString()


repositories {
    maven(url = "https://repo.spongepowered.org/maven")
}

dependencies {
    compile(project(":common"))

    minecraft("net.minecraftforge:forge:${prop("minecraft_version")}-${prop("forge_version")}")

    implementation(fg.deobf("io.github.ladder:ladder-impl-forge:1.0-SNAPSHOT"))

}



//TODO: make this more incremental or find a way to not need this
val moveCommonSourcesToForge = tasks.create<Copy>("moveCommonSourcesToForge"){
    val fromDir = project(":common").tasks.compileJava.get().destinationDir.parentFile
    val toDir = tasks.compileJava.get().destinationDir.parentFile
    from(fromDir)
    into(toDir)
}

tasks.classes.get().dependsOn(moveCommonSourcesToForge)


configure<UserDevExtension> {
    mappings(mapOf(
            "channel" to "snapshot",
            "version" to prop("mcp_version")
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


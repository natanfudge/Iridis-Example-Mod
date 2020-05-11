plugins {
    java
    id("fabric-loom") version "0.2.7-SNAPSHOT"
}

fun prop(key: String): String = project.property(key).toString()


repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    compile(project(":common"))

    minecraft("com.mojang:minecraft:${prop("minecraft_version")}")
    mappings("net.fabricmc:yarn:${prop("yarn_mappings")}")
    modCompile("net.fabricmc:fabric-loader:${prop("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("fabric_version")}")

    modImplementation("io.github.ladder:ladder-impl-fabric:1.0-SNAPSHOT")
    include("io.github.ladder:ladder-impl-fabric:1.0-SNAPSHOT")
    include ("com.github.Chocohead:Fabric-ASM:v2.0.1")


}

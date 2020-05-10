plugins {
    java
    id("fabric-loom") version "0.2.7-SNAPSHOT"
}

val minecraftVersion = "1.15.2"
val yarnMappings = "1.15.2+build.14:v2"
val loaderVersion = "0.7.8+build.189"

repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    compile(project(":common"))

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings")
    modCompile("net.fabricmc:fabric-loader:$loaderVersion")

    modImplementation("io.github.ladder:ladder-impl-fabric:1.0-SNAPSHOT")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.7.1+build.301-1.15")

}


configure<BasePluginConvention> {
    archivesBaseName = "$archivesBaseName-mc$minecraftVersion"
}

tasks.named<Copy>("processResources") {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", version)

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand("version" to version)
    }

    // copy everything else except the mod json
    from(sourceSets["main"].resources.srcDirs) {
        exclude("fabric.mod.json")
    }

    from(project(":common").tasks.named("processResources"))
}

pluginManagement {
    repositories {
        jcenter()

        maven {
            name = "Mixin maven"
            url = uri("https://repo.spongepowered.org/maven" )}
        maven {
            name = "Fabric maven"
            url = uri("https://maven.fabricmc.net/")
        }

        maven{
            name = "Forge maven"
            url = uri("https://files.minecraftforge.net/maven")
        }

        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if ("net.minecraftforge.gradle" == requested.id.id) {
                useModule("net.minecraftforge.gradle:ForgeGradle:${requested.version}")
            }
            if("org.spongepowered.mixin" == requested.id.id){
                useModule("org.spongepowered:mixingradle:${requested.version}")
            }
        }
    }
}

rootProject.name = "Ladder-Example-Mod"

include("common")
include("fabric")
include("forge")

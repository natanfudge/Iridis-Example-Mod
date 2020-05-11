plugins {
    java
}

subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        mavenLocal()
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    fun complainAboutLoaderProp(): Nothing = throw kotlin.IllegalArgumentException("The 'loader' property must be set to either FABRIC, FORGE, or COMMON")

    if (!project.hasProperty("loader")) complainAboutLoaderProp()

    val loader = project.property("loader").toString()

    if (loader != "COMMON") {
        tasks.named<Copy>("processResources") {

            val modManifest = when (loader) {
                "FORGE" -> "META-INF/mods.toml"
                "FABRIC" -> "fabric.mod.json"
                else -> complainAboutLoaderProp()
            }

            val propertiesWithoutAuthor = listOf("version", "mod_id", "display_name", "description", "issue_tracker", "home_page", "logo")
                    .map { it to project.property(it) }.toMap()

            val templateProperties = propertiesWithoutAuthor +
                    if (loader == "FORGE") "authors" to project.property("authors")
                    else ("authors" to project.property("authors").toString()
                            .split(",")
                            .map { it.trim() }
                            .joinToString(", ") { "\"" + it + "\"" })

            inputs.properties(templateProperties)


            // replace stuff in the manifest, nothing else
            from(sourceSets["main"].resources.srcDirs) {
                include(modManifest)

                // replace version and mcversion
                expand(templateProperties)
            }

            // copy everything else except the manifest
            from(sourceSets["main"].resources.srcDirs) {
                exclude(modManifest)
            }

            from(project(":common").tasks.named("processResources"))
        }
    }

}
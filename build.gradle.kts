import groovy.lang.MissingPropertyException

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

    fun complainAboutLoaderProp(): Nothing = throw MissingPropertyException("The 'loader' property must be set to either FABRIC, FORGE, or COMMON")

    if (!project.hasProperty("loader")) complainAboutLoaderProp()

    fun prop(key: String): String = project.property(key).toString()

    val isFabric = when (prop("loader")) {
        "FORGE" -> false
        "FABRIC" -> true
        "COMMON" -> return@subprojects
        else -> complainAboutLoaderProp()
    }

    val modManifest = if (isFabric) "fabric.mod.json" else "META-INF/mods.toml"

    base {
        archivesBaseName = prop("mod_id") + if (isFabric) "-fabric" else "-forge"
    }

    tasks.named<Copy>("processResources") {

        val propertiesWithoutAuthor = listOf("version", "mod_id", "display_name", "description", "issue_tracker", "home_page", "logo")
                .map { it to project.property(it) }.toMap()

        val templateProperties = propertiesWithoutAuthor +
                if (isFabric) {
                    ("authors" to project.property("authors").toString()
                            .split(",")
                            .map { it.trim() }
                            .joinToString(", ") { "\"" + it + "\"" })
                } else "authors" to project.property("authors")

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
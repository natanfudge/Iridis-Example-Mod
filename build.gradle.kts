
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
}
import org.apache.commons.io.FileUtils
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("commons-io","commons-io","2.6")
    }
}

plugins {
    java
}

dependencies {
    compileOnly(files("../libs/ladder-stubs.jar"))
    compileOnly(files("../libs/ladder-api-1.0-SNAPSHOT.jar"))
}




val moveSrc = tasks.register("moveCommonSourcesIntoPlatform") {
    group = "ladder"
    doLast {
        // TODO: optimize to be incremental

        copyFromCommon("classes/java/main", "forge/build/classes/java")
        copyFromCommon("classes/java/main", "fabric/build/classes/java")
    }

}



fun copyFromCommon(dir: String, to: String) {
    val source = File("${project.projectDir}/build/$dir")
    val dest = File("${project.projectDir}").parentFile.resolve(to)
    println("Copying from $source to $dest")
    FileUtils.copyDirectoryToDirectory(source, dest)
}

tasks.named("classes") {
    finalizedBy(moveSrc)
}



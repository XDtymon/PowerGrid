import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation("com.mysql:mysql-connector-j:8.4.0")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

// ---- Numeracja builda ----
val buildPropsFile = file("build.properties")
val buildProps = Properties()
buildProps.load(FileInputStream(buildPropsFile))

val buildNumber: Int = Integer.parseInt(buildProps.getProperty("build.number", "1"))
val versionNumber: Int = Integer.parseInt(buildProps.getProperty("version.number", "1"))

version = "ALPHA-BUILD-${versionNumber.toString().padStart(2, '0')}.${buildNumber.toString().padStart(4, '0')}"

tasks.register("incrementBuildNumber") {
    doLast {
        buildProps.setProperty("build.number", (buildNumber + 1).toString())
        buildProps.store(FileOutputStream(buildPropsFile), null)
    }
}

tasks {
    build {
        dependsOn(shadowJar)
        dependsOn("incrementBuildNumber")
    }

    shadowJar {
        dependsOn("incrementBuildNumber")
    }

    runServer {
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
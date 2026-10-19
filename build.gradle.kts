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
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.helpch.at/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation("com.mysql:mysql-connector-j:8.4.0")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    implementation("xyz.xenondevs.invui:invui:2.3.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

// ---- Numeracja builda ----
val buildPropsFile = file("build.properties")
val buildProps = Properties().apply {
    buildPropsFile.inputStream().use { load(it) }
}

val buildNumber: Int = buildProps.getProperty("build.number", "1").toInt()
val versionNumber: Int = buildProps.getProperty("build.version", "1").toInt()

version = "ALPHA-BUILD-${versionNumber.toString().padStart(2, '0')}.${buildNumber.toString().padStart(4, '0')}"

tasks.register("incrementBuildNumber") {
    val propsFile = layout.projectDirectory.file("build.properties").asFile

    doLast {
        val props = Properties()
        propsFile.inputStream().use { props.load(it) }

        val current = props.getProperty("build.number", "1").toInt()
        props.setProperty("build.number", (current + 1).toString())

        propsFile.outputStream().use { props.store(it, null) }
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
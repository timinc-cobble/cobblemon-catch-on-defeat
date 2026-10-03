plugins {
    id("java")
    id("java-library")
    kotlin("jvm") version("2.4.0")

    id("dev.architectury.loom") version("1.11-SNAPSHOT") apply false
    id("architectury-plugin") version("3.4-SNAPSHOT") apply false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    version = "${project.property("modCobblemonVersion")}-${project.property("modMyVersion")}"
    group = project.property("maven_group") as String

    repositories {
        mavenCentral()
        maven("https://artefacts.cobblemon.com/releases/")
        maven("https://maven.neoforged.net/releases")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
        maven("https://api.modrinth.com/maven")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    java {
        withSourcesJar()
    }
}


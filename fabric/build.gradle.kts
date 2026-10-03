plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow") version "9.2.2"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    enableTransitiveAccessWideners.set(true)
    silentMojangMappingsLicense()

    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

val shadowBundle = configurations.create("shadowBundle") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    modRuntimeOnly("org.graalvm.js:js:${property("graalvm_version")}")
    modRuntimeOnly("org.graalvm.sdk:graal-sdk:${property("graalvm_version")}")
    modRuntimeOnly("org.graalvm.regex:regex:${property("graalvm_version")}")
    modRuntimeOnly("org.graalvm.truffle:truffle-api:${property("graalvm_version")}")

    @Suppress("AvoidDuplicateDependencies")
    "com.ibm.icu:icu4j:${property("icu4j_version")}".let {
        modRuntimeOnly(it)
        minecraftServerLibraries(it)
    }

    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation(fabricApi.module("fabric-command-api-v2", property("fabric_api_version").toString()))
    modImplementation(fabricApi.module("fabric-networking-api-v1", property("fabric_api_version").toString()))

    //needed for cobblemon
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin")}")
    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}") { isTransitive = false }

    shadowBundle(project(":common", configuration = "transformProductionFabric"))
    @Suppress("AvoidDuplicateDependencies")
    project(":common", configuration = "namedElements").let {
        implementation(it)
        "developmentFabric"(it) { isTransitive = false }
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit_version")}")

    modImplementation("maven.modrinth:cobblemon-tim-core:${property("tim_core_fabric_version")}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        @Suppress("DEPRECATION", "We'll think about it when gradle 10 happens")
        expand(project.properties)
    }

}

tasks {

    jar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        archiveClassifier.set("dev-slim")
    }

    shadowJar {
        archiveClassifier.set("dev-shadow")
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        configurations = listOf(shadowBundle)
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }
}

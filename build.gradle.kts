plugins {
    id("java-library")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.121-stable")
    // Non-transitive: WorldEdit pins older Mojang libs with strict constraints that clash with Paper API.
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.10") {
        isTransitive = false
    }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.10") {
        isTransitive = false
    }
    compileOnly("com.github.networkminesky.mainframe:mainframe-api:578767c23e")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

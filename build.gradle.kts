import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "com.alttd.proxydiscordlink"
version = "1.0.0-BETA-SNAPSHOT"
description = "A velocity plugin to link Discord and Minecraft accounts."


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    shadowJar {
        dependsOn(getByName("relocateJars") as ConfigureShadowRelocation)
        archiveFileName.set("${project.name}-${project.version}.jar")
        minimize()
        configurations = listOf(project.configurations.shadow.get())
    }

    build {
        dependsOn(shadowJar)
    }

    create<ConfigureShadowRelocation>("relocateJars") {
        target = shadowJar.get()
        prefix = "${project.name}.lib"
    }
}


dependencies {
    // Minimessage // TODO : update all usages to 4.2
    shadow("net.kyori:adventure-text-minimessage:4.2.0-SNAPSHOT") {
        exclude("net.kyori", "adventure-api")
    }
    // Velocity
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.0.1")
    // JDA
    shadow("net.dv8tion:JDA:5.0.0-alpha.3") {
        exclude("opus-java") // exclude audio
    }
    // LuckPerms
    compileOnly("net.luckperms:api:5.3")
    // MySQL
    shadow("mysql:mysql-connector-java:8.0.25")
    // ShutdownInfo
    compileOnly("com.alttd:ShutdownInfo:1.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories{
        maven {
            name = "maven"
            url = uri("https://repo.destro.xyz/snapshots")
            credentials(PasswordCredentials::class)
        }
    }
}

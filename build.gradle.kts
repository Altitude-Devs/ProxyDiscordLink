plugins {
    `java`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

allprojects {
    val build = System.getenv("BUILD_NUMBER") ?: "SNAPSHOT"
    group = "com.alttd.proxydiscordlink"
    version = "1.0.0-BETA-$build"
    description = "A velocity plugin to link Discord and Minecraft accounts."

    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(16))
        }
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = Charsets.UTF_8.name()
            options.release.set(16)
        }

        withType<Javadoc> {
            options.encoding = Charsets.UTF_8.name()
        }
    }
}

dependencies {
    // Minimessage
    implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT")
    // Velocity
    compileOnly("com.velocitypowered:velocity-api:1.1.5")
    annotationProcessor("com.velocitypowered:velocity-api:1.1.5")
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-alpha.3") {
        exclude("opus-java") // exclude audio
    }
    compileOnly("com.gitlab.ruany:LitebansAPI:0.3.5")
    // LuckPerms
    compileOnly("net.luckperms:api:5.3")
    // MySQL
    runtimeOnly("mysql:mysql-connector-java:8.0.23")
    // ShutdownInfo
    compileOnly("com.alttd:ShutdownInfo:1.0")
}

tasks {

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
        exclude("net.kyori.adventure")
        exclude("net.kyori.examination")
        minimize {
            //exclude(dependency("net.kyori:.*:.*"))
        }
        listOf(
//            "net.kyori",
            "net.dv8tion.jda"
        ).forEach { relocate(it, "${rootProject.group}.lib.$it") }
    }

    build {
        dependsOn(shadowJar)
    }

}
rootProject.name = "ProxyDiscordLink"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // Altitude
        maven {
            name = "maven"
            url = uri("https://repo.destro.xyz/snapshots")
            credentials(PasswordCredentials::class)
        }
        // Velocity
        maven("https://nexus.velocitypowered.com/repository/maven-public/")
        // JDA
        maven("https://m2.dv8tion.net/releases/")
        // MiniMessage
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        // LiteBans
        maven("https://jitpack.io")
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

// Met -Pspiegel=http://127.0.0.1:8765 gaan downloads via maven_spiegel.py (zie README).
val spiegel = providers.gradleProperty("spiegel").orNull

pluginManagement {
    val spiegel = providers.gradleProperty("spiegel").orNull
    repositories {
        if (spiegel != null) {
            maven(spiegel) { isAllowInsecureProtocol = true }
        } else {
            gradlePluginPortal()
            mavenCentral()
        }
    }
}

dependencyResolutionManagement {
    repositories {
        if (spiegel != null) {
            maven(spiegel) { isAllowInsecureProtocol = true }
        } else {
            mavenCentral()
        }
    }
}

rootProject.name = "voorproef"

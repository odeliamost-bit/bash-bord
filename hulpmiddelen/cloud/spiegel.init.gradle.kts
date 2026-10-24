// Alleen in de cloud-sessie: Maven Central en de Gradle-plugin-portal via maven_spiegel.py
// (die probeert downloads opnieuw als Maven Central hier wisselend 404 geeft).
fun RepositoryHandler.viaSpiegel() = withType(MavenArtifactRepository::class.java).configureEach {
    if (url.host == "repo.maven.apache.org" || url.host == "plugins.gradle.org") {
        url = uri("http://127.0.0.1:8765")
        isAllowInsecureProtocol = true
    }
}
beforeSettings {
    pluginManagement.repositories.viaSpiegel()
    dependencyResolutionManagement.repositories.viaSpiegel()
}

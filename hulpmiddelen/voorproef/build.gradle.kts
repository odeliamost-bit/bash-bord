// -Pcmp=1.7.3 (standaard): code-check tegen dezelfde Compose-versie als de app.
// -Pcmp=1.5.12: om de schermen te tekenen (heeft geen onderdelen van Google Maven nodig).
val cmp = (findProperty("cmp") as String?) ?: "1.7.3"

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

dependencies {
    implementation("org.jetbrains.compose.desktop:desktop-jvm-linux-x64:$cmp")
    implementation("org.jetbrains.compose.material3:material3-desktop:$cmp")
    implementation("org.json:json:20251224")
    testImplementation("junit:junit:4.13.2")
}

layout.buildDirectory.set(file("build-$cmp"))

val app = rootDir.resolve("../../app/src/main")

sourceSets.main {
    kotlin.srcDir(app.resolve("java"))
    // Alleen de rekenregels en de Compose-schermen; de rest bestaat alleen op Android.
    kotlin.exclude(
        "**/MainActivity.kt", "**/Voorlezer.kt", "**/Voorbeelden.kt", "**/DashboardModel.kt", "**/Acties.kt",
        "**/data/**", "**/meldingen/**", "**/widget/**", "**/online/**",
    )
}
sourceSets.test {
    kotlin.srcDir(rootDir.resolve("../../app/src/test/java"))
}

tasks.register<JavaExec>("teken") {
    mainClass.set("voorproef.TekenKt")
    classpath = sourceSets.main.get().runtimeClasspath
    systemProperty("res", app.resolve("res").absolutePath)
    args(findProperty("uit")?.toString() ?: layout.buildDirectory.dir("plaatjes").get().asFile.absolutePath)
}

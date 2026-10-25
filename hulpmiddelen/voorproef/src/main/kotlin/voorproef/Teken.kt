package voorproef

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.Density
import java.io.File
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.DashboardApp
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.Voorbeeld
import nl.odelia.dashboard.logica.Leren
import nl.odelia.dashboard.logica.PauzeReden
import nl.odelia.dashboard.logica.Rondje
import nl.odelia.dashboard.ui.leren.LerenBegin
import nl.odelia.dashboard.ui.leren.LerenTab
import java.time.Instant
import kotlin.random.Random
import nl.odelia.dashboard.ui.theme.DashboardThema
import nl.odelia.dashboard.ui.thuis.Tab
import org.jetbrains.skia.EncodedImageFormat

// Nothing Phone (1): 1080 px breed, dichtheid 2.625 → 411 dp
private const val DICHT = 2.625f

fun main(args: Array<String>) {
    val uit = File(args.firstOrNull() ?: "uit").apply { mkdirs() }
    val geen = object : AppActies {}
    val staat = Voorbeeld.staat()
    fun teken(naam: String, b: Int, h: Int, inhoud: @Composable () -> Unit) {
        val scene = ImageComposeScene((b * DICHT).toInt(), (h * DICHT).toInt(), Density(DICHT), content = { DashboardThema { inhoud() } })
        scene.render(0)
        val beeld = scene.render(500_000_000)
        scene.close()
        File(uit, "$naam.png").writeBytes(beeld.encodeToData(EncodedImageFormat.PNG)!!.bytes)
        println("klaar: $naam.png")
    }
    teken("1-vandaag-scherm", 411, 870) { DashboardApp(staat, geen) }
    teken("2-vandaag-heel", 411, 1650) { DashboardApp(staat, geen) }
    teken("3-werk-heel", 411, 2100) { DashboardApp(staat, geen, beginTab = Tab.Werk) }
    teken("4-onthoud-heel", 411, 1650) { DashboardApp(staat, geen, beginTab = Tab.Onthoud) }
    teken("5-menu-open", 411, 870) { DashboardApp(staat, geen, beginMenuOpen = true) }
    teken("6-toevoegen", 411, 1500) { DashboardApp(staat, geen, beginScherm = Scherm.Toevoegen()) }
    teken("7-lijst", 411, 1300) { DashboardApp(staat, geen, beginScherm = Scherm.Lijst()) }
    teken("8-mensen", 411, 1300) { DashboardApp(staat, geen, beginScherm = Scherm.Lijst(perPersoon = true)) }
    teken("9-urenmail", 411, 1500) { DashboardApp(staat, geen, beginScherm = Scherm.Urenmail) }
    teken("10-tablet", 800, 1200) { DashboardApp(staat, geen) }
    val metWeer = staat.copy(zeilweer = Voorbeeld.zeilweer())
    // Op de computer staan geen weer-apps: grijze vlakjes in plaats van de echte icoontjes.
    val metApps = object : AppActies {
        override fun appIcoon(pakket: String) = ImageBitmap(144, 144).also {
            Canvas(it).drawRect(0f, 0f, 144f, 144f, Paint().apply { color = Color(0xFFB8C4CC) })
        }
    }
    teken("11-zeilweer", 411, 2150) { DashboardApp(metWeer, metApps, beginScherm = Scherm.Zeilweer) }
    teken("12-zeilweer-plek", 411, 870) { DashboardApp(staat, geen, beginScherm = Scherm.Zeilweer) }
    val leer = staat.copy(leren = Voorbeeld.leren())
    fun leren(naam: String, h: Int, begin: LerenBegin) = teken(naam, 411, h) { DashboardApp(leer, geen, beginScherm = Scherm.Leren(begin)) }
    leren("13-leren-overzicht", 1900, LerenBegin())
    leren("14-leren-kaarten", 1200, LerenBegin(LerenTab.Kaarten, onderwerp = "relais"))
    val rondje = Rondje(listOf("r3", "r4", "r5"), totaal = 8, goed = 5)
    leren("15-leren-kaart-vraag", 1200, LerenBegin(LerenTab.Kaarten, onderwerp = "relais", rondje = rondje))
    leren("16-leren-kaart-antwoord", 1300, LerenBegin(LerenTab.Kaarten, onderwerp = "relais", rondje = rondje, omgedraaid = true))
    leren("17-leren-quiz", 1300, LerenBegin(LerenTab.Quiz, onderwerp = "spelling", quiz = Leren.nieuweQuiz("spelling", Random(2))))
    leren("18-leren-rekenen", 1100, LerenBegin(LerenTab.Quiz, onderwerp = "ohm", quiz = Leren.nieuweQuiz("ohm", Random(5))))
    leren("19-leren-maken", 1700, LerenBegin(LerenTab.Maken, onderwerp = "project"))
    leren("20-leren-pauze-vraag", 870, LerenBegin(LerenTab.Kaarten, onderwerp = "relais", rondje = rondje, seintje = PauzeReden.Langzamer))
    leren("21-leren-pauze", 870, LerenBegin(LerenTab.Kaarten, pauzeTot = Instant.now().plusSeconds(263)))
    leren("22-leren-burgerschap", 1200, LerenBegin(LerenTab.Kaarten, onderwerp = "politiek"))
}

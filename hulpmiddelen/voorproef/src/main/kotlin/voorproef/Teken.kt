package voorproef

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import java.io.File
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.DashboardApp
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.Voorbeeld
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
    teken("11-zeilweer", 411, 1900) { DashboardApp(metWeer, geen, beginScherm = Scherm.Zeilweer) }
    teken("12-zeilweer-plek", 411, 870) { DashboardApp(staat, geen, beginScherm = Scherm.Zeilweer) }
}

package voorproef

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import java.io.File
import java.time.LocalDateTime
import nl.odelia.dashboard.ui.DashboardApp
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.theme.DashboardThema
import nl.odelia.dashboard.ui.thuis.Tab
import org.jetbrains.skia.EncodedImageFormat

// Nothing Phone (1): 1080 px breed, dichtheid 2.625 → 411 dp
private const val DICHT = 2.625f

fun main(args: Array<String>) {
    val uit = File(args.firstOrNull() ?: "uit").apply { mkdirs() }
    val vrijdag = LocalDateTime.of(2026, 10, 23, 8, 15)
    fun teken(naam: String, b: Int, h: Int, inhoud: @Composable () -> Unit) {
        val scene = ImageComposeScene((b * DICHT).toInt(), (h * DICHT).toInt(), Density(DICHT), content = inhoud)
        scene.render(0)
        val beeld = scene.render(500_000_000)
        scene.close()
        File(uit, "$naam.png").writeBytes(beeld.encodeToData(EncodedImageFormat.PNG)!!.bytes)
        println("klaar: $naam.png")
    }
    teken("1-vandaag-scherm", 411, 870) { DashboardThema { DashboardApp({ true }, vrijdag) } }
    teken("2-vandaag-heel", 411, 1560) { DashboardThema { DashboardApp({ true }, vrijdag) } }
    teken("3-werk-heel", 411, 1560) { DashboardThema { DashboardApp({ true }, vrijdag, beginTab = Tab.Werk) } }
    teken("4-onthoud-heel", 411, 1560) { DashboardThema { DashboardApp({ true }, vrijdag, beginTab = Tab.Onthoud) } }
    teken("5-menu-open", 411, 870) { DashboardThema { DashboardApp({ true }, vrijdag, beginMenuOpen = true) } }
    teken("6-tablet", 800, 1200) { DashboardThema { DashboardApp({ true }, vrijdag) } }
    teken("7-geld-komt-later", 411, 870) { DashboardThema { DashboardApp({ true }, vrijdag, beginPlek = Plek.Geld) } }
    teken("8-wensen-komt-later", 411, 870) { DashboardThema { DashboardApp({ true }, vrijdag, beginPlek = Plek.Wensen) } }
}

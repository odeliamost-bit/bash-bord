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
import nl.odelia.dashboard.ui.thuis.Voorbeeld
import nl.odelia.dashboard.uren.KlokActies
import nl.odelia.dashboard.uren.KlokStaat
import org.jetbrains.skia.EncodedImageFormat

// Nothing Phone (1): 1080 px breed, dichtheid 2.625 → 411 dp
private const val DICHT = 2.625f

fun main(args: Array<String>) {
    val uit = File(args.firstOrNull() ?: "uit").apply { mkdirs() }
    val vrijdag = LocalDateTime.of(2026, 10, 23, 14, 0)
    val klok = Voorbeeld.klok(vrijdag)
    fun teken(naam: String, b: Int, h: Int, inhoud: @Composable () -> Unit) {
        val scene = ImageComposeScene((b * DICHT).toInt(), (h * DICHT).toInt(), Density(DICHT), content = inhoud)
        scene.render(0)
        val beeld = scene.render(500_000_000)
        scene.close()
        File(uit, "$naam.png").writeBytes(beeld.encodeToData(EncodedImageFormat.PNG)!!.bytes)
        println("klaar: $naam.png")
    }
    fun app(
        k: KlokStaat = klok,
        plek: Plek = Plek.Thuis,
        tab: Tab = Tab.Vandaag,
        menu: Boolean = false,
        dag: java.time.LocalDate? = null,
    ): @Composable () -> Unit = {
        DashboardThema { DashboardApp(k, KlokActies.niets, { true }, beginPlek = plek, beginTab = tab, beginMenuOpen = menu, beginDag = dag) }
    }
    teken("1-vandaag-scherm", 411, 870, app())
    teken("2-vandaag-heel", 411, 1560, app())
    teken("3-werk-heel", 411, 1560, app(tab = Tab.Werk))
    teken("4-onthoud-heel", 411, 1560, app(tab = Tab.Onthoud))
    teken("5-menu-open", 411, 870, app(menu = true))
    teken("6-tablet", 800, 1200, app())
    teken("7-geld-komt-later", 411, 870, app(plek = Plek.Geld))
    teken("8-wensen-komt-later", 411, 870, app(plek = Plek.Wensen))
    teken("9-dag-aanpassen", 411, 1400, app(dag = vrijdag.toLocalDate().minusDays(1)))
    teken("10-dag-vandaag-bezig", 411, 1100, app(dag = vrijdag.toLocalDate()))
    val ochtend = LocalDateTime.of(2026, 10, 23, 8, 5)
    teken("11-vergeten-uitklokken", 411, 1100, app(k = Voorbeeld.klok(ochtend, vergeten = true)))
    teken("12-klaar-voor-vandaag", 411, 870, app(k = Voorbeeld.klok(LocalDateTime.of(2026, 10, 23, 16, 30), vrijdagUit = "15:50")))
}

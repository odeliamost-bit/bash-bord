package voorproef

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import java.io.File
import java.time.LocalDateTime
import nl.odelia.dashboard.onthoud.OnthoudActies
import nl.odelia.dashboard.ui.DashboardApp
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.theme.DashboardThema
import nl.odelia.dashboard.ui.thuis.Tab
import nl.odelia.dashboard.ui.thuis.Voorbeeld
import nl.odelia.dashboard.uren.KlokActies
import nl.odelia.dashboard.uren.MailActies
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
        scherm: String? = null,
        adres: String = "",
    ): @Composable () -> Unit = {
        DashboardThema {
            DashboardApp(
                k, KlokActies.niets, Voorbeeld.onthoud(k.nu), OnthoudActies.niets, MailActies.niets, adres, { true },
                beginPlek = plek, beginTab = tab, beginMenuOpen = menu, beginScherm = scherm,
            )
        }
    }
    teken("1-vandaag-scherm", 411, 870, app())
    teken("2-vandaag-heel", 411, 1560, app())
    teken("3-werk-heel", 411, 1560, app(tab = Tab.Werk))
    teken("4-onthoud-heel", 411, 1560, app(tab = Tab.Onthoud))
    teken("5-menu-open", 411, 870, app(menu = true))
    teken("6-tablet", 800, 1200, app())
    teken("7-geld-komt-later", 411, 870, app(plek = Plek.Geld))
    teken("8-wensen-komt-later", 411, 870, app(plek = Plek.Wensen))
    teken("9-dag-aanpassen", 411, 1400, app(scherm = "dag/${vrijdag.toLocalDate().minusDays(1)}"))
    teken("10-dag-vandaag-bezig", 411, 1100, app(scherm = "dag/${vrijdag.toLocalDate()}"))
    val ochtend = LocalDateTime.of(2026, 10, 23, 8, 5)
    teken("11-vergeten-uitklokken", 411, 1100, app(k = Voorbeeld.klok(ochtend, vergeten = true)))
    teken("13-onthoud-dit", 411, 1500, app(scherm = "nieuw"))
    teken("14-onthoud-lijst", 411, 1500, app(scherm = "lijst"))
    teken("15-onthoud-project", 411, 1300, app(scherm = "lijst/project:PVB"))
    teken("16-mensen", 411, 1300, app(plek = Plek.Mensen))
    teken("17-afspraak-aanpassen", 411, 1500, app(scherm = "bewerk/a1"))
    teken("18-urenmail", 411, 1700, app(k = Voorbeeld.klok(LocalDateTime.of(2026, 10, 23, 16, 30), vrijdagUit = "15:50"), scherm = "urenmail", adres = "kim@voorbeeld.nl"))
    val ziek = Voorbeeld.klok(LocalDateTime.of(2026, 10, 23, 16, 30), vrijdagUit = "15:50", ziek = true)
    teken("19-werk-met-ziek", 411, 1100, app(k = ziek, tab = Tab.Werk))
    teken("20-urenmail-met-ziek", 411, 1700, app(k = ziek, scherm = "urenmail", adres = "kim@voorbeeld.nl"))
    teken("21-vandaag-met-ziek", 411, 1300, app(k = ziek))
    teken("12-klaar-voor-vandaag", 411, 870, app(k = Voorbeeld.klok(LocalDateTime.of(2026, 10, 23, 16, 30), vrijdagUit = "15:50")))
}

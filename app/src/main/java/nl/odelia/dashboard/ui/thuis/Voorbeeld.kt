package nl.odelia.dashboard.ui.thuis

import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels
import nl.odelia.dashboard.uren.Blok
import nl.odelia.dashboard.uren.DagSoort
import nl.odelia.dashboard.uren.KlokStaat
import nl.odelia.dashboard.uren.Uren

/** Kleur van een dag in de uren-ring en de staafjes (felle pastels uit het ontwerp). */
class DagKleur(val ring: Color, val staaf: List<Color>) {
    companion object {
        private val kleuren = listOf(
            DagKleur(Color(0xFFF7A8C4), listOf(Color(0xFFF6BCAE), Color(0xFFEE9F8E), Color(0xFFDD8574))),
            DagKleur(Color(0xFFF6CF5A), listOf(Color(0xFFF8D995), Color(0xFFF2C66D), Color(0xFFE0AE50))),
            DagKleur(Color(0xFF8EC5F5), listOf(Color(0xFFBFD3EA), Color(0xFF9DBBDE), Color(0xFF7FA2CC))),
            DagKleur(Color(0xFFB9A3F0), listOf(Color(0xFFDAC8EA), Color(0xFFC6AEDD), Color(0xFFAE93CB))),
            DagKleur(Color(0xFF9ACFAE), listOf(Color(0xFFB4D8BE), Color(0xFF8EC09C), Color(0xFF74A883))),
            DagKleur(Color(0xFFF8B98E), listOf(Color(0xFFF9CDAE), Color(0xFFF2B288), Color(0xFFE09766))),
            DagKleur(Color(0xFF9FD8D8), listOf(Color(0xFFC0E3E3), Color(0xFF98CFCF), Color(0xFF74B4B4))),
        )

        fun van(dag: DayOfWeek): DagKleur = kleuren[dag.value - 1]
    }
}

/**
 * Verzonnen voorbeeld-gegevens, voor de voorbeelden in Android Studio en het voorproef-hulpje.
 * De app zelf gebruikt echte gegevens.
 */
object Voorbeeld {
    /**
     * Een week zoals in het ontwerp: ma–do gewerkt, vrijdag nog bezig.
     * [vergeten]: woensdag niet uitgeklokt. [vrijdagUit]: vrijdag al klaar (bv. "15:50").
     */
    fun klok(nu: LocalDateTime, vergeten: Boolean = false, vrijdagUit: String? = null): KlokStaat {
        val maandag = Uren.maandag(nu.toLocalDate())
        fun blok(dag: Long, van: String, tot: String?) = maandag.plusDays(dag).let { d ->
            Blok("voorbeeld-$dag-$van", d.atTime(LocalTime.parse(van)), tot?.let { d.atTime(LocalTime.parse(it)) })
        }
        val blokken = listOf(
            blok(0, "07:45", "15:45"),
            blok(1, "07:45", "15:45"),
            blok(2, "07:45", if (vergeten) null else "15:30"),
            blok(3, "07:45", "15:45"),
        ) + if (nu.dayOfWeek == DayOfWeek.FRIDAY && nu.toLocalTime() > LocalTime.of(7, 45)) listOf(blok(4, "07:45", vrijdagUit)) else emptyList()
        return KlokStaat.maak(blokken, emptyMap<LocalDate, DagSoort>(), nu)
    }

    class Afspraak(val wat: String, val wie: String, val wanneer: String, val kleur: Pastel, val icoon: List<Vorm>)

    val afspraken = listOf(
        Afspraak("Verslag inleveren", "Dhr. Jansen", "ma\n26", Pastels.Koraal, Icoontjes.document),
        Afspraak("Tandarts bellen", "voor 17:00", "vandaag", Pastels.Geel, Icoontjes.telefoon),
        Afspraak("Formulier stage", "Mevr. Bakker", "do\n29", Pastels.Lila, Icoontjes.iemand),
    )
}

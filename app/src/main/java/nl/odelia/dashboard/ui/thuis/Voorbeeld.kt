package nl.odelia.dashboard.ui.thuis

import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.Duur
import nl.odelia.dashboard.onthoud.Groep
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.onthoud.Persoon
import nl.odelia.dashboard.onthoud.Soort
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
     * [vergeten]: woensdag niet uitgeklokt. [vrijdagUit]: vrijdag al klaar (bv. "15:50"). [ziek]: woensdag ziek.
     */
    fun klok(nu: LocalDateTime, vergeten: Boolean = false, vrijdagUit: String? = null, ziek: Boolean = false): KlokStaat {
        val maandag = Uren.maandag(nu.toLocalDate())
        fun blok(dag: Long, van: String, tot: String?) = maandag.plusDays(dag).let { d ->
            Blok("voorbeeld-$dag-$van", d.atTime(LocalTime.parse(van)), tot?.let { d.atTime(LocalTime.parse(it)) })
        }
        val blokken = listOf(
            blok(0, "07:45", "15:45"),
            blok(1, "07:45", "15:45"),
            blok(2, "07:45", if (vergeten) null else "15:30"),
            blok(3, "07:45", "15:45"),
        ).filter { !ziek || it.datum != maandag.plusDays(2) } + if (nu.dayOfWeek == DayOfWeek.FRIDAY && nu.toLocalTime() > LocalTime.of(7, 45)) listOf(blok(4, "07:45", vrijdagUit)) else emptyList()
        val soorten: Map<LocalDate, DagSoort> = if (ziek) mapOf(maandag.plusDays(2) to DagSoort.Ziek) else emptyMap()
        return KlokStaat.maak(blokken, soorten, nu)
    }

    /** Verzonnen mensen en afspraken (uit het ontwerp), geen echte gegevens. */
    fun onthoud(nu: LocalDateTime): OnthoudStaat {
        val vandaag = nu.toLocalDate()
        val gemaakt = nu.minusDays(3)
        val personen = listOf(
            Persoon("p1", "Dhr. Jansen", Groep.School, "leraar Nederlands", 0),
            Persoon("p2", "Mevr. Bakker", Groep.School, "stagebegeleider", 1),
            Persoon("p3", "Kim", Groep.Werk, "Randstad", 2),
            Persoon("p4", "Teamleider", Groep.Werk, "Cargill", 3),
        )
        val maandag = Uren.maandag(vandaag).plusWeeks(1)
        val afspraken = listOf(
            Afspraak("a1", "Verslag inleveren", persoonId = "p1", project = "PVB", soort = Soort.Inleveren, deadline = maandag, seintje = true, gemaakt = gemaakt, duur = Duur.Dag),
            Afspraak("a2", "Tandarts bellen", notitie = "voor 17:00", groep = Groep.Thuis, soort = Soort.Bellen, deadline = vandaag, gemaakt = gemaakt, todo = true),
            Afspraak("a3", "Formulier stage", persoonId = "p2", project = "PVB", soort = Soort.Maken, deadline = maandag.plusDays(3), gemaakt = gemaakt),
            Afspraak("a4", "Boek meenemen", persoonId = "p1", soort = Soort.Meenemen, deadline = vandaag.plusDays(1), seintje = true, gemaakt = gemaakt),
            Afspraak("a5", "Vrije dag doorgeven", persoonId = "p4", soort = Soort.Vragen, gemaakt = gemaakt),
            Afspraak("a6", "Mail sturen over toets", persoonId = "p1", soort = Soort.Mailen, klaar = true, gemaakt = gemaakt.minusDays(7)),
            Afspraak("a7", "Fietsband plakken", groep = Groep.Thuis, gemaakt = gemaakt, todo = true, duur = Duur.Kwartier),
        )
        return OnthoudStaat(afspraken, personen, vandaag)
    }
}

package nl.odelia.dashboard.ui

import java.time.LocalDate
import java.time.LocalDateTime
import nl.odelia.dashboard.logica.Kaart
import nl.odelia.dashboard.logica.KaartStand
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.LerenData
import nl.odelia.dashboard.logica.QuizScore
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.Periode
import nl.odelia.dashboard.logica.Plek
import nl.odelia.dashboard.logica.Uurweer
import nl.odelia.dashboard.logica.Weerbericht
import nl.odelia.dashboard.logica.Zeilweer

/**
 * Verzonnen voorbeeld (uit het ontwerp) voor de voorbeelden in Android Studio en de voorproef-plaatjes.
 * De echte app gebruikt dit niet.
 */
object Voorbeeld {
    /** Vrijdag 23 oktober 2026, kwart over één. */
    val nu: LocalDateTime = LocalDateTime.of(2026, 10, 23, 13, 15)

    fun staat(nu: LocalDateTime = this.nu): AppStaat {
        val maandag = LocalDate.of(2026, 10, 19)
        val perioden = listOf(
            Periode(maandag.atTime(7, 45), maandag.atTime(15, 45)),
            Periode(maandag.plusDays(1).atTime(7, 45), maandag.plusDays(1).atTime(15, 45)),
            Periode(maandag.plusDays(2).atTime(8, 0), maandag.plusDays(2).atTime(15, 45)),
            Periode(maandag.plusDays(3).atTime(7, 45), maandag.plusDays(3).atTime(15, 45)),
            Periode(maandag.plusDays(4).atTime(7, 45), null),
        )
        val onthoud = listOf(
            Onthoud(tekst = "Verslag inleveren", wie = "Dhr. Jansen", wat = "Inleveren", groep = "School", wanneer = maandag.plusDays(7)),
            Onthoud(tekst = "Tandarts bellen", wat = "Bellen", groep = "Thuis", waar = "voor 17:00", wanneer = maandag.plusDays(4)),
            Onthoud(tekst = "Formulier stage", wie = "Mevr. Bakker", wat = "Maken", groep = "School", wanneer = maandag.plusDays(10)),
            Onthoud(tekst = "Vrije dag doorgeven", wie = "Kim", wat = "Mailen", groep = "Werk"),
        )
        return AppStaat(KlokStaat.maak(perioden, emptyMap(), nu), perioden, emptyMap(), onthoud)
    }

    /** Verzonnen zeilweer voor een verzonnen plek. */
    fun zeilweer(nu: LocalDateTime = this.nu): ZeilweerStaat {
        val plek = Plek("Voorbeeldmeer", 52.0, 4.7)
        val perModel = Zeilweer.modellen.mapIndexed { m, model ->
            model to (0L..2L).flatMap { d ->
                (10..18).map { u ->
                    val wind = 18.0 + m * 1.5 + (u - 12) * 1.2 + d * 4
                    Uurweer(nu.toLocalDate().plusDays(d).atTime(u, 0), wind, wind * 1.6, 220.0 + u * 3, if (u == 15) 0.2 else 0.0, 12.0 + m * 0.2)
                }
            }
        }.toMap()
        return ZeilweerStaat(plek = plek, bericht = Weerbericht(plek, nu.withHour(9).withMinute(41), perModel))
    }

    /** Verzonnen voortgang voor Leren: een paar kaarten geoefend, één quiz gedaan, één eigen kaart. */
    fun leren(nu: LocalDateTime = this.nu): LerenData {
        val dag = nu.toLocalDate()
        return LerenData(
            standen = mapOf(
                "r1" to KaartStand(4, dag.plusDays(5)),
                "r2" to KaartStand(3, dag.plusDays(2)),
                "r3" to KaartStand(2, dag),
                "r4" to KaartStand(1, dag),
                "o1" to KaartStand(5, dag.plusDays(10)),
                "o2" to KaartStand(2, dag.plusDays(1)),
            ),
            quiz = mapOf("relais" to QuizScore(4, 5, 4, dag.minusDays(1))),
            eigen = listOf(Kaart("voorbeeld-1", "project", "Wat is het onderwerp van mijn project?", "Een slimme schakeling voor de verlichting thuis.", eigen = true)),
            geoefend = mapOf(dag.minusDays(4) to 12, dag.minusDays(3) to 8, dag.minusDays(1) to 10, dag to 6),
        )
    }
}

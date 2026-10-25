package nl.odelia.dashboard.ui

import java.time.LocalDate
import java.time.LocalDateTime
import nl.odelia.dashboard.logica.KlokStaat
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
}

package nl.odelia.dashboard.uren

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/** Wanneer de herinneringen komen (keuze van Odelia, 24 okt). */
object Planning {
    /** Nog niet ingeklokt? Dan om 8:15 een seintje. */
    val INKLOKKEN: LocalTime = LocalTime.of(8, 15)

    /** Nog niet uitgeklokt? Dan om 16:15 een seintje (met "over 10 min" en "over 20 min"). */
    val UITKLOKKEN: LocalTime = LocalTime.of(16, 15)

    /** "Vanavond nog een keer": het seintje voor morgen nog eens om 19:00 (keuze van Odelia). */
    val SEINTJE_AVOND: LocalTime = LocalTime.of(19, 0)

    /**
     * Het seintje voor morgen zit in het ochtendbericht (20 min na inklokken).
     * Op een dag zonder inklokken (weekend, vrij, ziek) komt het om 10:00 (keuze van Claude).
     */
    val SEINTJE_RESERVE: LocalTime = LocalTime.of(10, 0)

    fun isWerkdag(dag: DayOfWeek) = dag != DayOfWeek.SATURDAY && dag != DayOfWeek.SUNDAY

    /** Het eerstvolgende moment na [nu] om [tijd], elke dag (ook in het weekend). */
    fun volgendeDag(nu: LocalDateTime, tijd: LocalTime): LocalDateTime {
        val vandaag = nu.toLocalDate().atTime(tijd)
        return if (vandaag.isAfter(nu)) vandaag else vandaag.plusDays(1)
    }

    /** Het eerstvolgende moment na [nu] op een werkdag (ma–vr) om [tijd]. */
    fun volgende(nu: LocalDateTime, tijd: LocalTime): LocalDateTime {
        var kandidaat = nu.toLocalDate().atTime(tijd)
        if (!kandidaat.isAfter(nu)) kandidaat = kandidaat.plusDays(1)
        while (!isWerkdag(kandidaat.dayOfWeek)) kandidaat = kandidaat.plusDays(1)
        return kandidaat
    }
}

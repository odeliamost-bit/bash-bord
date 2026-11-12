package nl.odelia.dashboard.logica

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Vangnet voor vergeten uit te klokken (nov 2026).
 * 1. De herinnering komt nog 2 keer terug, elk kwartier.
 * 2. Om 18:00 klokt de app zelf uit op de gewone eindtijd en vraagt: "Klopt dat?"
 */
object Vangnet {
    /** De herinnering komt zoveel minuten later nog eens. */
    val NOG_EENS_NA = listOf(15L, 30L)
    /** Zo laat klokt de app zelf uit, als je nog ingeklokt staat. */
    val OM: LocalTime = LocalTime.of(18, 0)
    /** De gewone eindtijd ligt zoveel minuten vóór de herinnering (15:50 → 15:45). */
    const val EIND_VOOR_HERINNERING = 5L

    /** Het eerstvolgende moment voor "nog een keer herinneren", of `null` als ze op zijn. */
    fun nogEens(herinnering: LocalTime, nu: LocalDateTime): LocalDateTime? =
        NOG_EENS_NA.map { nu.toLocalDate().atTime(herinnering).plusMinutes(it) }.firstOrNull { it.isAfter(nu) }

    /** De geschatte eindtijd voor een periode die open bleef staan. Nooit vóór het begin. */
    fun geschatEind(begin: LocalDateTime, herinnering: LocalTime): LocalDateTime =
        maxOf(begin, begin.toLocalDate().atTime(herinnering.minusMinutes(EIND_VOOR_HERINNERING)))
}

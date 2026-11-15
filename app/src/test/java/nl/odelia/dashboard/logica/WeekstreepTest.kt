package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekstreepTest {
    // Woensdag 11 november 2026, 10:00.
    private val nu = LocalDateTime.of(2026, 11, 11, 10, 0)
    private val ma = LocalDate.of(2026, 11, 9)
    private val di = LocalDate.of(2026, 11, 10)
    private val wo = LocalDate.of(2026, 11, 11)

    @Test
    fun bolletjesVanDeWeek() {
        val perioden = listOf(Periode(ma.atTime(8, 0), ma.atTime(15, 45)), Periode(wo.atTime(8, 0), null))
        val klok = KlokStaat.maak(perioden, emptyMap(), nu)
        assertEquals(
            listOf(Bolletje.Gewerkt, Bolletje.Gemist, Bolletje.Bezig, Bolletje.Komt, Bolletje.Komt),
            Weekstreep.bolletjes(klok.week, wo),
        )
    }

    @Test
    fun vrijEnZiekKrijgenEenEigenBolletje() {
        val klok = KlokStaat.maak(emptyList(), mapOf(ma to DagSoort.Vrij, di to DagSoort.Ziek), nu)
        assertEquals(listOf(Bolletje.Vrij, Bolletje.Ziek), Weekstreep.bolletjes(klok.week, wo).take(2))
    }

    @Test
    fun vrijOfZiekKanAlleenOpEenLegeWerkdag() {
        assertTrue(Weekstreep.kanVrijOfZiek(KlokStaat.maak(emptyList(), emptyMap(), nu)))
        // Al ingeklokt.
        assertFalse(Weekstreep.kanVrijOfZiek(KlokStaat.maak(listOf(Periode(wo.atTime(8, 0), null)), emptyMap(), nu)))
        // Al gewerkt en uitgeklokt.
        assertFalse(Weekstreep.kanVrijOfZiek(KlokStaat.maak(listOf(Periode(wo.atTime(8, 0), wo.atTime(9, 0))), emptyMap(), nu)))
        // Zaterdag.
        assertFalse(Weekstreep.kanVrijOfZiek(KlokStaat.maak(emptyList(), emptyMap(), LocalDateTime.of(2026, 11, 14, 10, 0))))
    }

    @Test
    fun terugKanAlleenAlsVandaagVrijOfZiekIs() {
        assertTrue(Weekstreep.kanTerug(KlokStaat.maak(emptyList(), mapOf(wo to DagSoort.Ziek), nu)))
        assertFalse(Weekstreep.kanTerug(KlokStaat.maak(emptyList(), emptyMap(), nu)))
    }
}

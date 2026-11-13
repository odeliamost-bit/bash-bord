package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class UrenStatsTest {
    private val nu = LocalDate.of(2026, 11, 13).atTime(20, 0)
    private fun dag(d: LocalDate) = Periode(d.atTime(7, 45), d.atTime(15, 45))
    private val perioden = listOf(
        dag(LocalDate.of(2026, 10, 30)),
        dag(LocalDate.of(2026, 11, 9)),
        dag(LocalDate.of(2026, 11, 10)),
        dag(LocalDate.of(2026, 11, 12)),
    )
    private val soorten = mapOf(LocalDate.of(2026, 11, 11) to DagSoort.Ziek)
    private val dagen = UrenStats.dagen(perioden, soorten, nu)

    @Test
    fun dagen_nieuwsteEerst_metZiek() {
        assertEquals(5, dagen.size)
        assertEquals(LocalDate.of(2026, 11, 12), dagen.first().datum)
        assertEquals(DagSoort.Ziek, dagen[1].soort)
    }

    @Test
    fun week_teltAlleenGewerkteDagen() {
        val w = UrenStats.week(dagen, nu.toLocalDate())
        assertEquals(3, w.dagen)
        assertEquals(21.75f, w.uren, 0.001f)
        assertEquals(7.25f, w.gemiddeld, 0.001f)
    }

    @Test
    fun maand_enJaar() {
        assertEquals(1, UrenStats.maand(dagen, YearMonth.of(2026, 10)).dagen)
        assertEquals(4, UrenStats.jaar(dagen, 2026).dagen)
        assertEquals(0f, UrenStats.jaar(dagen, 2025).gemiddeld, 0f)
    }

    @Test
    fun perMaand_oudsteEerst() {
        val m = UrenStats.perMaand(dagen, YearMonth.of(2026, 11), aantal = 3)
        assertEquals(listOf(9, 10, 11), m.map { it.first.monthValue })
        assertEquals("november 2026", UrenStats.naam(m.last().first))
    }
}

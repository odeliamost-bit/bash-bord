package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class UrenStatsTest {
    private val nu = LocalDateTime.of(2026, 11, 13, 12, 0)
    private fun dag(d: LocalDate) = Periode(d.atTime(8, 0), d.atTime(15, 45)) // 7 uur netto
    private val perioden = listOf(
        dag(LocalDate.of(2026, 11, 9)), dag(LocalDate.of(2026, 11, 10)), dag(LocalDate.of(2026, 11, 2)), dag(LocalDate.of(2026, 10, 30)),
    )
    private val soorten = mapOf(LocalDate.of(2026, 11, 11) to DagSoort.Ziek)
    private val dagen = UrenStats.dagen(perioden, soorten, nu)

    @Test
    fun dagenNieuwsteEerstMetZiek() {
        assertEquals(5, dagen.size)
        assertEquals(LocalDate.of(2026, 11, 11), dagen.first().datum)
        assertEquals(DagSoort.Ziek, dagen.first().soort)
    }

    @Test
    fun teltWeekMaandEnJaar() {
        assertEquals(UrenStats.Som(14f, 2), UrenStats.week(dagen, nu.toLocalDate()))
        assertEquals(UrenStats.Som(21f, 3), UrenStats.maand(dagen, YearMonth.of(2026, 11)))
        assertEquals(UrenStats.Som(28f, 4), UrenStats.jaar(dagen, 2026))
        assertEquals(7f, UrenStats.jaar(dagen, 2026).gemiddeld, 0.001f)
        assertEquals(0f, UrenStats.jaar(dagen, 2025).gemiddeld, 0.001f)
        assertEquals("november 2026", UrenStats.naam(YearMonth.of(2026, 11)))
        assertEquals(4, UrenStats.vanMaand(dagen, YearMonth.of(2026, 11)).size)
    }
}

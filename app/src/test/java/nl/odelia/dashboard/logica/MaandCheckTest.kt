package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MaandCheckTest {
    private val nu = LocalDateTime.of(2026, 11, 13, 12, 0)
    private val nov = YearMonth.of(2026, 11)
    private fun dag(d: LocalDate) = Periode(d.atTime(8, 0), d.atTime(15, 45)) // 7 uur netto
    private val dagen = UrenStats.dagen(
        listOf(dag(LocalDate.of(2026, 11, 9)), dag(LocalDate.of(2026, 11, 10)), dag(LocalDate.of(2026, 10, 30))),
        mapOf(LocalDate.of(2026, 11, 11) to DagSoort.Ziek), nu,
    )
    private fun loon(cent: Long, datum: LocalDate, soort: BoekSoort = BoekSoort.In, cat: String = "loon") =
        Boeking("b$cent$datum$soort$cat", soort, cent, "inkomsten", cat, datum)

    @Test
    fun teltAlleenLoonVanDieMaand() {
        val b = listOf(
            loon(100_00, LocalDate.of(2026, 11, 6)), loon(50_00, LocalDate.of(2026, 11, 20)),
            loon(999_00, LocalDate.of(2026, 10, 30)), // andere maand
            loon(25_00, LocalDate.of(2026, 11, 7), cat = "oma"), // geen loon
            loon(70_00, LocalDate.of(2026, 11, 8), soort = BoekSoort.Over), // overboeken
        )
        assertEquals(150_00, MaandCheck.loon(b, nov))
    }

    @Test
    fun zonderUurloonGeenBedrag() {
        val u = MaandCheck.van(dagen, emptyList(), nov, null)
        assertEquals(14f, u.uren, 0.001f)
        assertEquals(2, u.dagen)
        assertNull(u.verwachtCent)
        assertEquals(MaandCheck.Oordeel.GeenUurloon, u.oordeel)
    }

    @Test
    fun vergelijktUrenKeerUurloonMetLoon() {
        // 14 uur × € 15,00 = € 210,00
        fun met(cent: Long) = MaandCheck.van(dagen, listOf(loon(cent, LocalDate.of(2026, 11, 20))), nov, 15_00)
        assertEquals(210_00L, met(210_00).verwachtCent)
        assertEquals(MaandCheck.Oordeel.Klopt, met(205_00).oordeel)
        assertEquals(MaandCheck.Oordeel.Minder, met(150_00).oordeel)
        assertEquals(-60_00L, met(150_00).verschilCent)
        assertEquals(MaandCheck.Oordeel.Meer, met(300_00).oordeel)
        assertEquals(MaandCheck.Oordeel.GeenLoon, MaandCheck.van(dagen, emptyList(), nov, 15_00).oordeel)
        assertEquals(MaandCheck.Oordeel.GeenUren, MaandCheck.van(dagen, emptyList(), YearMonth.of(2026, 9), 15_00).oordeel)
    }

    @Test
    fun uurloonBewarenEnLezen() {
        assertEquals(UurloonData(14_53), UurloonJson.lees(UurloonJson.schrijf(UurloonData(14_53))))
        assertEquals(UurloonData(), UurloonJson.lees(UurloonJson.schrijf(UurloonData())))
        assertEquals(UurloonData(), UurloonJson.lees("kapot"))
    }

    /** Uren en uurloon gaan nooit online: ze mogen niet in de sync-lijst staan. */
    @Test
    fun uurloonStaatNietInDeSync() {
        assertEquals(emptyList<String>(), SyncRegels.ONLINE.filter { it.contains("uur") || it.contains("uren") || it.contains("loon") })
    }
}

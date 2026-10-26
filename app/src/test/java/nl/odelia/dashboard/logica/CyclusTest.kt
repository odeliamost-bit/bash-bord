package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CyclusTest {
    private fun d(maand: Int, dag: Int) = LocalDate.of(2026, maand, dag)
    private fun b(begin: LocalDate, dagen: Long? = 5) = Bloeding(begin, dagen?.let { begin.plusDays(it - 1) })

    @Test
    fun nogNiets_geenDagEnBeginLengte() {
        val s = Cyclus.stand(emptyList(), d(10, 25))
        assertNull(s.dag)
        assertNull(s.volgende)
        assertEquals(28, s.lengte)
        assertTrue(s.nogAanHetLeren)
    }

    @Test
    fun eenKeer_gebruiktZelfIngevuldeLengte() {
        val s = Cyclus.stand(listOf(b(d(10, 14))), d(10, 25), beginLengte = 30)
        assertEquals(12, s.dag)
        assertEquals(30, s.lengte)
        assertEquals(d(11, 13), s.volgende)
        assertEquals(19L, s.dagenTot)
        assertTrue(s.nogAanHetLeren)
        assertFalse(s.ongesteld)
    }

    @Test
    fun gemiddeldeVanLaatsteCycli() {
        val s = Cyclus.stand(listOf(b(d(7, 1)), b(d(7, 28)), b(d(8, 26)), b(d(9, 23))), d(9, 30))
        // 27, 29, 28 dagen → gemiddeld 28
        assertEquals(28, s.lengte)
        assertEquals(d(10, 21), s.volgende)
        assertEquals(d(10, 20), s.vroegst)
        assertEquals(d(10, 22), s.laatst)
        assertFalse(s.nogAanHetLeren)
    }

    @Test
    fun minderDanDrieCycli_geenVenster() {
        val s = Cyclus.stand(listOf(b(d(8, 1)), b(d(8, 29))), d(9, 5))
        assertEquals(28, s.lengte)
        assertNull(s.vroegst)
        assertNull(s.laatst)
    }

    @Test
    fun uitschietersTellenNietMee() {
        // 10 dagen (te kort) en 90 dagen (vergeten) tellen niet mee; alleen 30 blijft.
        val starts = listOf(d(1, 1), d(1, 11), d(4, 11), d(5, 11))
        assertEquals(listOf(30), Cyclus.lengtes(starts.map { b(it) }))
    }

    @Test
    fun ongesteld_totEindDag() {
        val bl = listOf(b(d(10, 20), dagen = 4))
        assertTrue(Cyclus.stand(bl, d(10, 23)).ongesteld)
        assertFalse(Cyclus.stand(bl, d(10, 24)).ongesteld)
    }

    @Test
    fun klaarVergeten_naTienDagenNietMeerOngesteld() {
        val bl = listOf(b(d(10, 1), dagen = null))
        assertTrue(Cyclus.stand(bl, d(10, 10)).ongesteld)
        assertFalse(Cyclus.stand(bl, d(10, 11)).ongesteld)
    }

    @Test
    fun duurIsGemiddeldeVanAfgeslotenKeren() {
        val s = Cyclus.stand(listOf(b(d(8, 1), 4), b(d(8, 29), 6), b(d(9, 26), null)), d(9, 27))
        assertEquals(5, s.duur)
    }

    @Test
    fun telaat_dagenTotWordtNul() {
        val s = Cyclus.stand(listOf(b(d(9, 1))), d(9, 29))
        assertEquals(0L, s.dagenTot)
    }

    @Test
    fun startInDeToekomst_telNietMee() {
        val s = Cyclus.stand(listOf(b(d(12, 1))), d(10, 25))
        assertNull(s.dag)
    }

    @Test
    fun seintje_tweeDagenErvoor() {
        val s = Cyclus.stand(listOf(b(d(10, 1))), d(10, 10))
        assertEquals(d(10, 27), Cyclus.seintjeDag(s, d(10, 10)))
        val laat = Cyclus.stand(listOf(b(d(10, 1))), d(10, 28))
        assertNull(Cyclus.seintjeDag(laat, d(10, 28)))
        val bezig = Cyclus.stand(listOf(b(d(10, 1))), d(10, 2))
        assertNull(Cyclus.seintjeDag(bezig, d(10, 2)))
    }
}

package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CyclusDataTest {
    private fun d(m: Int, dag: Int) = LocalDate.of(2026, m, dag)

    @Test
    fun beginEnKlaar() {
        val c = CyclusData().begin(d(10, 1)).begin(d(10, 1)).klaar(d(10, 6))
        assertEquals(listOf(Bloeding(d(10, 1), d(10, 5))), c.bloedingen)
    }

    @Test
    fun klaarOpDezelfdeDag_haaltWeg() {
        assertTrue(CyclusData().begin(d(10, 1)).klaar(d(10, 1)).bloedingen.isEmpty())
    }

    @Test
    fun gevoelAanEnUit() {
        val c = CyclusData().wisselGevoel(d(10, 2), Gevoel.Kramp)
        assertEquals(setOf(Gevoel.Kramp), c.gevoel[d(10, 2)])
        assertNull(c.wisselGevoel(d(10, 2), Gevoel.Kramp).gevoel[d(10, 2)])
    }

    @Test
    fun seintjeOmKwartOverVier_enUitAlsUit() {
        val c = CyclusData(listOf(Bloeding(d(10, 1), d(10, 5))))
        assertEquals(d(10, 27).atTime(Instellingen.SEINTJE_OM), c.seintjeOm(d(10, 10).atTime(9, 0)))
        assertNull(c.copy(seintje = false).seintjeOm(d(10, 10).atTime(9, 0)))
    }

    @Test
    fun jsonHeenEnTerug() {
        val c = CyclusData(listOf(Bloeding(d(9, 3), d(9, 7)), Bloeding(d(10, 1), null)), mapOf(d(10, 2) to setOf(Gevoel.Moe, Gevoel.Goed)), seintje = false)
        assertEquals(c, CyclusJson.lees(CyclusJson.schrijf(c)))
        assertEquals(CyclusData(), CyclusJson.lees("kapot"))
    }
}

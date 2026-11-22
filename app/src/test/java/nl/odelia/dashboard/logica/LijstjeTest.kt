package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LijstjeTest {
    @Test
    fun bolletjesEnNummersWeg() {
        val t = "Boodschappen\n- melk halen\n• brief posten\n1. huiswerk NL\n2) Melk halen\n☐ fiets plakken\n\nx"
        assertEquals(listOf("Boodschappen", "melk halen", "brief posten", "huiswerk NL", "fiets plakken"), Lijstje.regels(t))
        assertFalse(Lijstje.isLijstje("Alleen één ding"))
    }

    @Test
    fun regelWordtOnthoud() {
        val o = Lijstje.naarOnthoud("verslag inleveren vrijdag", LocalDate.of(2026, 11, 23), emptyList(), listOf("School"))
        assertEquals("verslag inleveren vrijdag", o.tekst)
        assertEquals(LocalDate.of(2026, 11, 27), o.wanneer)
        assertEquals("Inleveren", o.wat)
    }
}

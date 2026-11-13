package nl.odelia.dashboard.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VertalenTest {
    @Test
    fun legeTekstGeeftGeenVraag() {
        assertNull(Vertalen.vraag("   \n", Vertalen.Taal.Engels))
    }

    @Test
    fun vraagHeeftTaalEnTekst() {
        val v = Vertalen.vraag("  Hoe laat begint de les?  ", Vertalen.Taal.Duits)!!
        assertTrue(v.startsWith("Vertaal deze tekst naar het Duits. Geef alleen de vertaling"))
        assertTrue(v.endsWith("\n\nHoe laat begint de les?"))
    }

    @Test
    fun appjeKrijgtEigenStijl() {
        val v = Vertalen.vraag("Hoi! Kom je ook? 😊", Vertalen.Taal.Engels, soort = Vertalen.Soort.Appje)!!
        assertTrue(v.contains("appje") && v.contains("spreektaal"))
        assertTrue(!Vertalen.vraag("Hoi", Vertalen.Taal.Engels)!!.contains("appje"))
    }

    @Test
    fun uitlegEnMaximum() {
        assertTrue(Vertalen.vraag("hallo", Vertalen.Taal.Auto, uitleg = true)!!.contains("lastige woorden"))
        val lang = Vertalen.vraag("a".repeat(9_000), Vertalen.Taal.Engels)!!
        assertEquals(Vertalen.MAX, lang.substringAfter("\n\n").length)
    }
}

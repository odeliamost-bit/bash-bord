package nl.odelia.dashboard.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WensenDataTest {
    @Test
    fun nieuweWensBovenaanMetWinkel() {
        val w = maakWens("Mok", WensSoort.Huis, "kijk https://www.etsy.com/x?utm_source=a", null, 5)
        val d = WensenData(emptyList()).bewaar(w)
        assertEquals("Etsy", d.lijst.first().winkel)
        assertEquals(listOf("https://www.etsy.com/x"), d.lijst.first().links)
        assertEquals("🏠", d.lijst.first().emoji)
    }

    @Test
    fun gekochtEnHartjes() {
        val id = WensenStart.lijst.first().id
        val d = WensenData().wisselGekocht(id, 1).volgendHartje(id, 2)
        val w = d.lijst.first { it.id == id }
        assertTrue(w.gekocht)
        assertEquals(1, w.hartjes)
    }

    @Test
    fun jsonHeenEnTerug() {
        val d = WensenData().volgendHartje(WensenStart.lijst[3].id, 9)
        assertEquals(d, WensenJson.lees(WensenJson.schrijf(d)))
        assertEquals(WensenData(), WensenJson.lees("{}"))
    }

    @Test
    fun beschrijvingBlijftBewaard() {
        val w = maakWens("Mok", WensSoort.Huis, "", null, 5, beschrijving = "  blauw, 300 ml ")
        assertEquals("blauw, 300 ml", w.beschrijving)
        val d = WensenData(emptyList()).bewaar(w)
        assertEquals("blauw, 300 ml", WensenJson.lees(WensenJson.schrijf(d)).lijst.first().beschrijving)
        // aanpassen zonder nieuwe tekst houdt de oude beschrijving
        assertEquals("blauw, 300 ml", maakWens("Mok 2", WensSoort.Huis, "", w, 6).beschrijving)
        // oud bestand zonder beschrijving
        assertEquals("", WensenJson.lees("{\"wensen\":[{\"id\":\"a\",\"naam\":\"X\"}]}").lijst.first().beschrijving)
    }

    @Test
    fun tweeKolommenZonderGaten() {
        val lijst = WensenStart.lijst
        val k = Wensen.tweeKolommen(lijst)
        assertEquals(2, k.size)
        assertEquals(lijst.size, k[0].size + k[1].size)
        assertTrue(kotlin.math.abs(k[0].size - k[1].size) <= 2)
    }
}

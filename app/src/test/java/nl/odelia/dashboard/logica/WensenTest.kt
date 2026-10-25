package nl.odelia.dashboard.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WensenTest {
    private fun wens(id: String, soort: WensSoort, winkel: String?, hartjes: Int = 0, gekocht: Boolean = false) =
        Wens(id, id, "✨", soort, winkel, emptyList(), hartjes, gekocht)

    @Test
    fun volgStukjesEruit() {
        assertEquals(
            "https://www.amazon.nl/dp/B0GR21FNKM",
            Wensen.schoon("https://www.amazon.nl/dp/B0GR21FNKM/ref=sr_1_1?tag=iemand-21&psc=1&utm_source=x"),
        )
        assertEquals(
            "https://vaistoa.com/p?variant=5537",
            Wensen.schoon("https://vaistoa.com/p?utm_medium=mail&variant=5537&fbclid=abc#boven"),
        )
        assertEquals("https://garbie.com/", Wensen.schoon("https://garbie.com/"))
    }

    @Test
    fun linksUitTekst() {
        val tekst = "Kijk deze! https://www.etsy.com/nl/listing/1/draak?click_key=x, en https://amzn.eu/d/abc."
        assertEquals(
            listOf("https://www.etsy.com/nl/listing/1/draak", "https://amzn.eu/d/abc"),
            Wensen.linksUit(tekst),
        )
        assertTrue(Wensen.linksUit("geen link hier").isEmpty())
    }

    @Test
    fun winkelnaam() {
        assertEquals("Etsy", Wensen.winkel("https://www.etsy.com/nl/listing/1"))
        assertEquals("Amazon", Wensen.winkel("https://amzn.eu/d/abc"))
        assertEquals("Amazon", Wensen.winkel("https://www.amazon.nl/gp/product/B0"))
        assertEquals("Nordicnest", Wensen.winkel("https://www.nordicnest.nl/merken/x"))
        assertNull(Wensen.winkel("geen link"))
    }

    @Test
    fun gedeeldeTekst() {
        val n = Wensen.uitTekst("Glazen mok - Nordic Nest\nhttps://www.nordicnest.nl/mok?utm_source=app")
        assertEquals("Glazen mok - Nordic Nest", n.naam)
        assertEquals(listOf("https://www.nordicnest.nl/mok"), n.links)
        assertEquals("Nordicnest", n.winkel)
        assertNull(Wensen.uitTekst("https://amzn.eu/d/abc").naam)
    }

    @Test
    fun hartjesGaanRond() {
        assertEquals(listOf(1, 2, 3, 0), (0..3).map(Wensen::volgendHartje))
    }

    @Test
    fun filterSorteerTel() {
        val lijst = listOf(
            wens("a", WensSoort.Haken, "Etsy", gekocht = true),
            wens("b", WensSoort.Haken, "Etsy", hartjes = 1),
            wens("c", WensSoort.Huis, "Amazon", hartjes = 3),
            wens("d", WensSoort.Huis, null),
        )
        assertEquals(listOf("a", "b"), Wensen.filter(lijst, soort = WensSoort.Haken).map { it.id })
        assertEquals(listOf("a", "b"), Wensen.filter(lijst, winkel = "etsy").map { it.id })
        assertEquals(listOf("c"), Wensen.filter(lijst, WensSoort.Huis, "Amazon").map { it.id })
        assertEquals(listOf("c", "b", "d", "a"), Wensen.sorteer(lijst).map { it.id })
        assertEquals(WensTelling(open = 3, gekocht = 1), Wensen.tel(lijst))
        assertEquals(listOf("Amazon", "Etsy"), Wensen.winkels(lijst))
    }

    @Test
    fun startlijst() {
        val lijst = WensenStart.lijst
        assertEquals(29, lijst.size)
        assertEquals(29, lijst.map { it.id }.toSet().size)
        lijst.flatMap { it.links }.forEach { link ->
            assertEquals(link, Wensen.schoon(link))
            assertFalse(link, link.contains("utm_") || link.contains("ref="))
        }
        assertEquals(WensTelling(open = 29, gekocht = 0), Wensen.tel(lijst))
        assertEquals("Etsy", lijst.first { it.naam == "Patroon: roze draak" }.winkel)
        assertNull(lijst.first { it.naam == "Gordijnbeugel" }.winkel)
    }
}

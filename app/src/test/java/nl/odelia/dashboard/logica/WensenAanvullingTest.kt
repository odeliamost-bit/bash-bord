package nl.odelia.dashboard.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WensenAanvullingTest {
    @Test
    fun alleLinksBijElkaarEnPrijs() {
        val lijst = WensenAanvulling.pas(WensenStart.lijst)
        val horloge = lijst.single { it.naam.contains("Horloge") }
        assertEquals(4, horloge.links.size)
        assertTrue(horloge.prijs!!.startsWith("±"))
        assertEquals(2, lijst.single { it.naam == "Barefoot schoenen" }.links.size)
        assertEquals(8, lijst.single { it.naam.startsWith("Werkrugzak") }.links.size)
        assertTrue(lijst.all { it.prijs != null })
    }

    @Test
    fun dubbeleWensWordtEen() {
        val dubbel = WensenStart.lijst + maakWens("horlogebandje", WensSoort.Overig, "https://amzn.eu/d/extra", null, 1)
        val lijst = WensenAanvulling.pas(dubbel)
        val horloge = lijst.filter { it.naam.lowercase().contains("horloge") }
        assertEquals(1, horloge.size)
        assertEquals(5, horloge.single().links.size)
    }

    @Test
    fun maarEenKeer() {
        val d = WensenData().verwijder(WensenData().lijst.first { it.naam.contains("perliet") }.id)
        val terug = WensenJson.lees(WensenJson.schrijf(d))
        assertTrue(terug.lijst.none { it.naam.contains("perliet") })
        val oud = WensenJson.lees("""{"versie":1,"wensen":[]}""")
        assertTrue(oud.lijst.any { it.naam.contains("perliet") })
    }
}

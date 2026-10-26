package nl.odelia.dashboard.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceptenTest {
    @Test
    fun recepten_zoekenOpIngredientEnSnel() {
        val d = ReceptenData(ReceptenStart.recepten)
        assertEquals(listOf("start-pesto"), Recepten.zoek(d, null, "pesto").map { it.id })
        assertTrue(Recepten.zoek(d, null, "snel").all { (Recepten.minuten(it.tijd) ?: 99) <= 15 })
        assertEquals(2, Recepten.zoek(d, ReceptSoort.Lunch, "").size)
        assertEquals(60, Recepten.minuten("1 uur"))
    }

    @Test
    fun nieuwRecept_regelsEnStappen() {
        val r = Recepten.nieuw(ReceptSoort.Avond, " Soep ", "20 min", "- ui\n\nwortel", "🔪 Snij de ui.\nKook alles.", "http://niet-veilig", id = "x", nu = 1)
        assertEquals("Soep", r.naam)
        assertEquals(listOf("ui", "wortel"), r.ingredienten)
        assertEquals(Stap("🔪", "Snij de ui."), r.stappen[0])
        assertEquals("Kook alles.", r.stappen[1].tekst)
        assertEquals("", r.link)
        assertEquals(listOf("wortel"), Recepten.mis(r, setOf("ui")))
    }

    @Test
    fun receptenJson_heenEnTerug() {
        val d = ReceptenData(ReceptenStart.recepten)
        assertEquals(d, ReceptenJson.lees(ReceptenJson.schrijf(d)))
        assertEquals("https://voorbeeld.nl/recept", Recepten.linkUit("Kijk: https://voorbeeld.nl/recept."))
    }
}

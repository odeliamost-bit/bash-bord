package nl.odelia.dashboard.logica

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Alleen verzonnen voorbeelden, geen echte wachtwoorden. */
class WachtwoordenReceptenTest {
    private val a = Inlog("a1", WachtwoordGroep.School, "Schoolportaal", "voorbeeld@school.nl", "Voorbeeld123")
    private val b = Inlog("a4", WachtwoordGroep.Thuis, "Streaming", "voorbeeld@mail.nl", "Popcorn42")
    private val data = WachtwoordenData(listOf(b, a))

    @Test
    fun zoek_perGroep_enZoekwoordInAlleGroepen() {
        assertEquals(listOf(a), Wachtwoorden.zoek(data, WachtwoordGroep.School, ""))
        assertEquals(listOf(b), Wachtwoorden.zoek(data, WachtwoordGroep.School, "stream"))
        assertEquals(listOf(a, b), Wachtwoorden.zoek(data, WachtwoordGroep.Werk, "voorbeeld"))
    }

    @Test
    fun opSlot_naDrieMinuten() {
        val t = Instant.parse("2026-10-25T10:00:00Z")
        assertFalse(Wachtwoorden.moetOpSlot(t, t.plusSeconds(170)))
        assertTrue(Wachtwoorden.moetOpSlot(t, t.plusSeconds(180)))
    }

    @Test
    fun wachtwoordenJson_heenEnTerug() {
        assertEquals(data, WachtwoordenJson.lees(WachtwoordenJson.schrijf(data)))
        assertEquals(WachtwoordenData(), WachtwoordenJson.lees("kapot"))
    }

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

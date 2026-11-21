package nl.odelia.dashboard.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VersleutelTest {
    private val zin = "verzonnen zin voor test"
    private val sleutel = Versleutel.sleutel(zin, "test-id")

    @Test
    fun heenEnTerug() {
        val dicht = Versleutel.dicht("""{"geld":1}""", sleutel)
        assertTrue(Versleutel.isDicht(dicht))
        assertFalse(dicht.contains("geld"))
        assertEquals("""{"geld":1}""", Versleutel.open(dicht, sleutel))
    }

    @Test
    fun zelfdeZinZelfdeSleutelOpElkApparaat() {
        assertTrue(sleutel.contentEquals(Versleutel.sleutel(" $zin ", "test-id")))
        assertFalse(sleutel.contentEquals(Versleutel.sleutel(zin, "ander-id")))
    }

    @Test(expected = Versleutel.VerkeerdeZin::class)
    fun verkeerdeZinGaatNiet() {
        Versleutel.open(Versleutel.dicht("geheim", sleutel), Versleutel.sleutel("andere verzonnen zin", "test-id"))
    }

    @Test
    fun ouderTekstBlijftLeesbaar() {
        assertEquals("{}", Versleutel.open("{}", sleutel))
        assertNotEquals(Versleutel.dicht("a", sleutel), Versleutel.dicht("a", sleutel))
        assertFalse(Versleutel.zinOk("kort"))
    }
}

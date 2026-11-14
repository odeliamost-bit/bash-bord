package nl.odelia.dashboard.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatorUitlegTest {
    private val tabjes = listOf("Tekenen", "Loop", "Kast", "Kring", "Motor", "Aders")

    @Test
    fun elkTabjeHeeftKorteStappen() {
        tabjes.forEach { t ->
            val stappen = SimulatorUitleg.voor(t)
            assertTrue("$t heeft 3 tot 5 stappen", stappen.size in 3..5)
            stappen.forEach { assertTrue("stap te lang: $it", it.length <= 100) }
        }
    }

    @Test
    fun onbekendTabjeGeeftNiks() {
        assertEquals(emptyList<String>(), SimulatorUitleg.voor("Formules"))
        assertEquals("", SimulatorUitleg.voorlees("Formules"))
    }

    @Test
    fun voorlezenNoemtDeStappen() {
        assertTrue(SimulatorUitleg.voorlees("Kast").startsWith("Stap 1. Tik op een schakelaar"))
    }
}

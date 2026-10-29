package nl.odelia.dashboard.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatorTest {
    @Test
    fun aardlekUitMaaktZijnGroepenDonker() {
        val k = KastStaat().wisselAardlek(0)
        assertFalse(k.heeftSpanning(0, 1))
        assertTrue(k.heeftSpanning(1, 1))
        assertFalse(k.wisselHoofd().heeftSpanning(1, 1))
    }

    @Test
    fun wetVanOhmInDeKring() {
        assertEquals(0.0, KringStaat().stroom, 0.0001)
        val k = KringStaat(dicht = true)
        assertEquals(0.5, k.stroom, 0.0001)
        assertEquals(2.0, k.spanningR1, 0.0001)
        assertFalse(k.copy(zekeringHeel = false).stroomt)
    }

    @Test
    fun zelfhoudingEnBeveiliging() {
        val aan = MotorStaat().start()
        assertTrue(aan.draait)
        assertFalse(aan.stop().draait)
        val fout = aan.overbelasting()
        assertFalse(fout.draait)
        assertFalse(fout.start().draait)
        assertTrue(fout.herstel().start().draait)
        assertFalse(MotorStaat(q1 = false).start().draait)
    }

    @Test
    fun alleSchemasHebbenOnderdelen() {
        listOf(Simulator.Kast, Simulator.Kring, Simulator.Motor, Simulator.Stuur).forEach { assertTrue(it.onderdelen.isNotEmpty()) }
        assertEquals(10, Simulator.formules.size)
    }
}

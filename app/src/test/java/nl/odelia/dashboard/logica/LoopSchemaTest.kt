package nl.odelia.dashboard.logica

import nl.odelia.dashboard.logica.LoopSchema.Staat
import nl.odelia.dashboard.logica.LoopSchema.Storing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoopSchemaTest {
    @Test
    fun zonderStoring() {
        val dicht = LoopSchema.reken(Staat())
        assertTrue(dicht.ingangDicht && !dicht.ingangOpen && !dicht.storing)
        val open = LoopSchema.reken(Staat(sturen = true))
        assertTrue(open.klepOpen && open.ingangOpen && !open.ingangDicht && !open.storing)
    }

    @Test
    fun draadbreukZieJePasBijOpenSturen() {
        assertFalse(LoopSchema.reken(Staat(false, Storing.Draadbreuk)).storing)
        val s = LoopSchema.reken(Staat(true, Storing.Draadbreuk))
        assertTrue(s.klepOpen && s.schakelaarOpen && !s.ingangOpen && !s.ingangDicht && s.storing)
    }

    @Test
    fun geenLuchtBlijftDicht() {
        val s = LoopSchema.reken(Staat(true, Storing.GeenLucht))
        assertTrue(s.ventiel && !s.klepOpen && s.ingangDicht && s.storing)
        LoopSchema.vragen.forEach { assertTrue(it.goed in it.antwoorden.indices) }
        assertEquals(8, LoopSchema.onderdelen.map { it.id }.toSet().size)
    }
}

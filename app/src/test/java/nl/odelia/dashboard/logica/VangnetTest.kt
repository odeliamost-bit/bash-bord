package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VangnetTest {
    private val dag = LocalDate.of(2026, 11, 13)
    private val herinnering = LocalTime.of(15, 50)

    @Test
    fun nogEens_eerstNaEenKwartier_danNaEenHalfUur_danNiets() {
        assertEquals(dag.atTime(16, 5), Vangnet.nogEens(herinnering, dag.atTime(15, 51)))
        assertEquals(dag.atTime(16, 20), Vangnet.nogEens(herinnering, dag.atTime(16, 6)))
        assertNull(Vangnet.nogEens(herinnering, dag.atTime(16, 21)))
    }

    @Test
    fun geschatEind_isVijfMinutenVoorDeHerinnering() {
        assertEquals(dag.atTime(15, 45), Vangnet.geschatEind(dag.atTime(7, 40), herinnering))
    }

    @Test
    fun geschatEind_nooitVoorHetBegin() {
        assertEquals(dag.atTime(17, 0), Vangnet.geschatEind(dag.atTime(17, 0), herinnering))
    }
}

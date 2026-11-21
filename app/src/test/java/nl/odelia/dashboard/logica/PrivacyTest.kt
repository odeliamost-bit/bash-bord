package nl.odelia.dashboard.logica

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyTest {
    @Test
    fun urenEnCyclusAlleenOpTelefoon() {
        val r = Privacy.regels().associateBy { it.naam }
        assertFalse(r.getValue("Uren + uurloon").online)
        assertFalse(r.getValue("Cyclus").online)
        assertTrue(r.getValue("Geld").online)
        assertTrue(r.getValue("Advies (⭐)").online)
        assertTrue(r.getValue("Zeilweer (plek)").online)
    }
}

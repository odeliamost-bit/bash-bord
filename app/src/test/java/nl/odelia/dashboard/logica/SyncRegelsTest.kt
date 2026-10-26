package nl.odelia.dashboard.logica

import java.time.Instant
import nl.odelia.dashboard.logica.SyncRegels.Stand
import nl.odelia.dashboard.logica.SyncRegels.Stap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SyncRegelsTest {
    private val t1 = Instant.parse("2026-10-26T10:00:00Z")
    private val t2 = Instant.parse("2026-10-26T11:00:00Z")
    private val t3 = Instant.parse("2026-10-26T12:00:00Z")

    @Test
    fun nogNiksOnlineDanVersturen() {
        assertEquals(Stap.Verstuur, SyncRegels.kies(Stand(), null))
    }

    @Test
    fun eersteKeerWintOnline() {
        // Ook als hier iets veranderd is: de eerste keer wint wat online staat.
        assertEquals(Stap.Neem, SyncRegels.kies(Stand(vies = true, gewijzigd = t3), t1))
    }

    @Test
    fun nieuwereOnlineVersieOphalen() {
        assertEquals(Stap.Neem, SyncRegels.kies(Stand(versie = t1), t2))
    }

    @Test
    fun nieuwsteWijzigingWint() {
        assertEquals(Stap.Verstuur, SyncRegels.kies(Stand(versie = t1, vies = true, gewijzigd = t3), t2))
        assertEquals(Stap.Neem, SyncRegels.kies(Stand(versie = t1, vies = true, gewijzigd = t2), t3))
    }

    @Test
    fun eigenWijzigingVersturen() {
        assertEquals(Stap.Verstuur, SyncRegels.kies(Stand(versie = t2, vies = true, gewijzigd = t3), t2))
        assertEquals(Stap.Niks, SyncRegels.kies(Stand(versie = t2), t2))
    }

    @Test
    fun lokaleOnderdelenNooitOnline() {
        for (lokaal in listOf("uren", "werk", "cyclus", "mail", "advies", "weer", "zeilweer", "thuis")) {
            assertFalse(lokaal, lokaal in SyncRegels.ONLINE)
        }
    }
}

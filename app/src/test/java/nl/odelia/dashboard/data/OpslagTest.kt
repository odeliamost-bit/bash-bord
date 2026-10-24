package nl.odelia.dashboard.data

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.time.LocalDate
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.Bron
import nl.odelia.dashboard.onthoud.Groep
import nl.odelia.dashboard.onthoud.Soort
import nl.odelia.dashboard.uren.DagSoort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Echt opslaan in de (Room-)database en weer teruglezen, zonder telefoon. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OpslagTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `in- en uitklokken wordt bewaard`() = runBlocking {
        val klok = KlokOpslag(context)
        val ochtend = LocalDateTime.of(2026, 10, 23, 7, 45, 30)
        assertTrue(klok.wissel(ochtend))
        assertFalse(klok.wissel(LocalDateTime.of(2026, 10, 23, 15, 0)))
        val staat = klok.nu(ochtend.toLocalDate()).staat(LocalDateTime.of(2026, 10, 23, 16, 0), 35f)
        assertEquals(1, staat.vandaag.blokken.size)
        assertEquals(LocalDateTime.of(2026, 10, 23, 7, 45), staat.vandaag.blokken[0].begin) // seconden vallen weg
        assertEquals(6.5f, staat.nettoVandaag) // 7:45–15:00 = 7¼ uur, min 45 min pauze

        // Stap 4: 20 minuten na het eerste inklokken een ochtendmelding (wekker om 8:05).
        val wekkers = shadowOf(context.getSystemService(AlarmManager::class.java)).scheduledAlarms
        val tijden = wekkers.map { LocalDateTime.ofInstant(Instant.ofEpochMilli(it.triggerAtMs), ZoneId.systemDefault()).withSecond(0) }
        assertTrue(LocalDateTime.of(2026, 10, 23, 8, 5) in tijden)
        // Stap 5: het is vrijdag, uitgeklokt om 15:00 → om 15:30 "urenmail staat klaar".
        assertTrue(LocalDateTime.of(2026, 10, 23, 15, 30) in tijden)
    }

    @Test
    fun `vrij en ziek worden bewaard`() = runBlocking {
        val klok = KlokOpslag(context)
        val dag = LocalDate.of(2026, 10, 22)
        klok.zetSoort(dag, DagSoort.Ziek)
        assertEquals(DagSoort.Ziek, klok.nu(dag).soorten[dag])
        klok.zetSoort(dag, DagSoort.Werk)
        assertEquals(DagSoort.Werk, klok.nu(dag).soorten[dag])
    }

    @Test
    fun `afspraak met persoon bewaren, afvinken en weghalen`() = runBlocking {
        val onthoud = OnthoudOpslag(context)
        val p = onthoud.nieuwePersoon(" Mevr. de Boer ", Groep.School, "mentor")
        val a = Afspraak(
            "a1", "Gesprek plannen", notitie = "na de vakantie", persoonId = p.id, project = "PVB",
            soort = Soort.Vragen, deadline = LocalDate.of(2026, 10, 30), seintje = true,
            gemaakt = LocalDateTime.of(2026, 10, 23, 9, 0), bron = Bron.Ingesproken,
        )
        onthoud.bewaar(a)
        var staat = onthoud.staat(LocalDate.of(2026, 10, 23))
        assertEquals(listOf(a), staat.afspraken)
        assertEquals("Mevr. de Boer", staat.persoon(a.persoonId)?.naam)
        assertEquals(listOf("a1"), staat.zoek("boer gesprek").map { it.id })

        onthoud.zetKlaar("a1", true)
        staat = onthoud.staat(LocalDate.of(2026, 10, 23))
        assertTrue(staat.afspraken.single().klaar)

        onthoud.verwijder("a1")
        assertTrue(onthoud.staat().afspraken.isEmpty())
    }
}

package nl.odelia.dashboard.meldingen

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.time.LocalDateTime
import java.time.ZoneId
import nl.odelia.dashboard.ui.thuis.Voorbeeld
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HerinneringenTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `uitklok-melding heeft knoppen uitklokken en twee keer snooze`() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val staat = Voorbeeld.klok(LocalDateTime.of(2026, 10, 23, 16, 15))
        Herinneringen.toonUitklokken(context, staat)

        val meldingen = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications
        assertEquals(1, meldingen.size)
        val knoppen = meldingen[0].actions.map { it.title.toString() }
        assertEquals(listOf("Uitklokken", "Over 10 min", "Over 20 min"), knoppen)
    }

    @Test
    fun `zonder toestemming geen melding`() {
        Herinneringen.toonInklokken(context)
        assertEquals(0, shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.size)
    }

    @Test
    fun `wekkers staan op 8_15 en 16_15 op de volgende werkdag, en elke dag 10_00 als reserve voor morgen`() {
        val vrijdagAvond = LocalDateTime.of(2026, 10, 23, 18, 0)
        Herinneringen.plan(context, vrijdagAvond)

        val wekkers = shadowOf(context.getSystemService(AlarmManager::class.java)).scheduledAlarms
        val tijden = wekkers.map { LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it.triggerAtMs), ZoneId.systemDefault()) }.sorted()
        assertEquals(
            listOf(LocalDateTime.of(2026, 10, 24, 10, 0), LocalDateTime.of(2026, 10, 26, 8, 15), LocalDateTime.of(2026, 10, 26, 16, 15)),
            tijden,
        )
    }

    @Test
    fun `ochtendbericht met iets voor morgen heeft de knop vanavond nog een keer`() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        // Voorbeeld op vrijdag: "Boek meenemen" (Dhr. Jansen) is morgen, met seintje aan.
        Herinneringen.toonOchtend(context, Voorbeeld.onthoud(LocalDateTime.of(2026, 10, 23, 8, 5)))
        var melding = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.single()
        assertEquals(listOf("Vanavond nog een keer"), melding.actions.map { it.title.toString() })
        assertTrue(melding.extras.getCharSequence("android.text").toString().contains("📌 Morgen:\n• Boek meenemen – Dhr. Jansen"))

        // Na de knop: zelfde bericht zonder knop, met "vanavond om 19:00", en een wekker om 19:00.
        Herinneringen.planAvond(context, LocalDateTime.of(2026, 10, 23, 8, 10))
        Herinneringen.toonOchtend(context, Voorbeeld.onthoud(LocalDateTime.of(2026, 10, 23, 8, 10)), avondGepland = true)
        melding = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.single()
        assertTrue(melding.actions.isNullOrEmpty())
        assertTrue(melding.extras.getCharSequence("android.text").toString().endsWith("🌙 Vanavond om 19:00 krijg je het nog een keer."))
        val wekkers = shadowOf(context.getSystemService(AlarmManager::class.java)).scheduledAlarms
        val tijden = wekkers.map { LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it.triggerAtMs), ZoneId.systemDefault()) }
        assertTrue(LocalDateTime.of(2026, 10, 23, 19, 0) in tijden)
    }

    @Test
    fun `seintje voor morgen noemt de afspraak en de persoon`() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        // Voorbeeld: "Boek meenemen" bij Dhr. Jansen is morgen, met seintje aan.
        Herinneringen.toonMorgen(context, Voorbeeld.onthoud(LocalDateTime.of(2026, 10, 23, 16, 30)))
        val melding = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.single()
        assertEquals("Morgen: Boek meenemen 📌", melding.extras.getString("android.title"))
        assertEquals("• Boek meenemen (Dhr. Jansen)", melding.extras.getCharSequence("android.text").toString())
    }
}

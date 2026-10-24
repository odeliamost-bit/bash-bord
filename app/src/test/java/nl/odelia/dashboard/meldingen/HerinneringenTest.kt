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
    fun `wekkers staan op 8_15 en 16_15 op de volgende werkdag`() {
        val vrijdagAvond = LocalDateTime.of(2026, 10, 23, 18, 0)
        Herinneringen.plan(context, vrijdagAvond)

        val wekkers = shadowOf(context.getSystemService(AlarmManager::class.java)).scheduledAlarms
        val tijden = wekkers.map { LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it.triggerAtMs), ZoneId.systemDefault()) }.sorted()
        assertEquals(listOf(LocalDateTime.of(2026, 10, 26, 8, 15), LocalDateTime.of(2026, 10, 26, 16, 15)), tijden)
    }
}

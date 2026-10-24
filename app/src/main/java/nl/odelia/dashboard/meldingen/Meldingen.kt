package nl.odelia.dashboard.meldingen

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import java.time.ZoneId
import nl.odelia.dashboard.MainActivity
import nl.odelia.dashboard.R

/** Vaste nummers voor de wekkers en de meldingen, zodat een nieuwe de oude vervangt. */
enum class Seintje(val nummer: Int) {
    Inklokken(1),
    Uitklokken(2),
    UitklokkenLater(3),
    MorgenTeDoen(4),
    Ochtend(5),
}

/** Meldingen tonen en wekkers zetten. */
object Meldingen {
    const val KANAAL_KLOKKEN = "klokken"
    const val KANAAL_ONTHOUD = "onthoud"

    /** Moet één keer gebeuren voordat je een melding kunt tonen (Android 8+). */
    fun maakKanalen(context: Context) {
        val beheer = context.getSystemService(NotificationManager::class.java)
        beheer.createNotificationChannel(
            NotificationChannel(KANAAL_KLOKKEN, "In- en uitklokken", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Herinnering om in of uit te klokken"
            },
        )
        beheer.createNotificationChannel(
            NotificationChannel(KANAAL_ONTHOUD, "Onthoud", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Seintje de dag voor een afspraak, en wat er vandaag staat"
            },
        )
    }

    /** Toestemming voor meldingen (pas nodig vanaf Android 13). */
    fun magTonen(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Een melding met maximaal drie knoppen. Tik op de melding = app openen. */
    fun toon(
        context: Context,
        seintje: Seintje,
        kanaal: String,
        titel: String,
        tekst: String,
        knoppen: List<Pair<String, String>> = emptyList(),
    ) {
        if (!magTonen(context)) return
        maakKanalen(context)
        val open = PendingIntent.getActivity(
            context, seintje.nummer,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val melding = NotificationCompat.Builder(context, kanaal)
            .setSmallIcon(R.drawable.ic_melding)
            .setColor(0xFFD9694F.toInt())
            .setContentTitle(titel)
            .setContentText(tekst)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tekst))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .apply {
                knoppen.forEachIndexed { i, (label, actie) ->
                    addAction(0, label, actieIntent(context, actie, seintje.nummer * 10 + i))
                }
            }
            .build()
        try {
            NotificationManagerCompat.from(context).notify(seintje.nummer, melding)
        } catch (_: SecurityException) {
            // Toestemming net ingetrokken: dan geen melding.
        }
    }

    fun weg(context: Context, seintje: Seintje) = NotificationManagerCompat.from(context).cancel(seintje.nummer)

    /**
     * Wekker op [wanneer]. Niet op de seconde precies (Android mag een paar minuten schuiven),
     * maar hij gaat ook af als de telefoon in de slaapstand staat.
     */
    fun zetWekker(context: Context, seintje: Seintje, actie: String, wanneer: LocalDateTime) {
        val millis = wanneer.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val wekkers = context.getSystemService(AlarmManager::class.java)
        wekkers.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, actieIntent(context, actie, seintje.nummer + 1000))
    }

    fun haalWekkerWeg(context: Context, seintje: Seintje, actie: String) {
        context.getSystemService(AlarmManager::class.java).cancel(actieIntent(context, actie, seintje.nummer + 1000))
    }

    private fun actieIntent(context: Context, actie: String, code: Int): PendingIntent = PendingIntent.getBroadcast(
        context, code,
        Intent(context, MeldingOntvanger::class.java).setAction(actie),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}

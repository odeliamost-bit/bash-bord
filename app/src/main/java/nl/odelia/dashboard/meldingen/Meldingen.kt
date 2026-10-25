package nl.odelia.dashboard.meldingen

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import nl.odelia.dashboard.MainActivity
import nl.odelia.dashboard.R

/** Meldingen tonen. Elke soort heeft een eigen kanaal (aan/uit te zetten in de Android-instellingen). */
object Meldingen {
    const val KANAAL_KLOK = "klok"
    const val KANAAL_ONTHOUD = "onthoud"
    const val KANAAL_OCHTEND = "ochtend"
    const val KANAAL_URENMAIL = "urenmail"

    const val ID_KLOK = 1
    const val ID_OCHTEND = 2
    const val ID_URENMAIL = 3
    private const val ID_SEINTJE_BASIS = 1000

    /** Extra bij het openen van de app: welk scherm. */
    const val EXTRA_SCHERM = "scherm"

    fun maakKanalen(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(KANAAL_KLOK, "In- en uitklokken", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(KANAAL_ONTHOUD, "Onthoud: seintje de dag ervoor", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(KANAAL_OCHTEND, "Ochtendmelding", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(KANAAL_URENMAIL, "Urenmail klaar", NotificationManager.IMPORTANCE_HIGH),
            ),
        )
    }

    /** Tik op de melding: open de app (eventueel op een bepaald scherm). */
    fun openApp(context: Context, scherm: String?, code: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_SCHERM, scherm)
        return PendingIntent.getActivity(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    @SuppressLint("MissingPermission")
    fun toon(
        context: Context,
        id: Int,
        kanaal: String,
        titel: String,
        tekst: String,
        scherm: String? = null,
        knop: Pair<String, PendingIntent>? = null,
    ) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        val melding = NotificationCompat.Builder(context, kanaal)
            .setSmallIcon(R.drawable.ic_melding)
            .setColor(0xFFD9694F.toInt())
            .setContentTitle(titel)
            .setContentText(tekst)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tekst))
            .setContentIntent(openApp(context, scherm, id))
            .setAutoCancel(true)
            .apply { knop?.let { addAction(0, it.first, it.second) } }
            .build()
        try {
            nm.notify(id, melding)
        } catch (_: SecurityException) {
            // Geen toestemming voor meldingen: dan maar niet.
        }
    }

    fun weg(context: Context, id: Int) = NotificationManagerCompat.from(context).cancel(id)

    fun seintjeId(onthoudId: String) = ID_SEINTJE_BASIS + (onthoudId.hashCode() and 0xFFFFF)
}

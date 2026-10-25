package nl.odelia.dashboard.meldingen

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.logica.Instellingen

/**
 * Zet de "wekkers" voor alle meldingen. Wordt opnieuw gedaan na elke wijziging,
 * na het opstarten van de telefoon en nadat een wekker is afgegaan.
 *
 * We gebruiken gewone (niet-exacte) wekkers: die mogen een paar minuten later afgaan,
 * maar hebben geen extra toestemming nodig.
 */
object Planner {
    const val HERINNER_IN = "nl.odelia.dashboard.HERINNER_IN"
    const val HERINNER_UIT = "nl.odelia.dashboard.HERINNER_UIT"
    const val OCHTEND = "nl.odelia.dashboard.OCHTEND"
    const val URENMAIL = "nl.odelia.dashboard.URENMAIL"
    const val SEINTJE = "nl.odelia.dashboard.SEINTJE"
    const val EXTRA_ID = "id"

    suspend fun plan(context: Context, nu: LocalDateTime = LocalDateTime.now()) {
        val opslag = Opslag.van(context)
        val klok = opslag.klokNu(nu)

        zet(context, HERINNER_IN, 1, volgendeWerkdagOm(Instellingen.HERINNER_INKLOKKEN, nu))
        zet(context, HERINNER_UIT, 2, volgendeWerkdagOm(Instellingen.HERINNER_UITKLOKKEN, nu))

        // Ochtendmelding: 20 minuten na inklokken (alleen de eerste keer vandaag).
        val ochtend = klok.vandaag.begin?.let { nu.toLocalDate().atTime(it).plusMinutes(Instellingen.OCHTEND_NA_MINUTEN) }
        if (klok.isIn && ochtend != null && ochtend.isAfter(nu)) zet(context, OCHTEND, 3, ochtend) else stop(context, OCHTEND, 3)

        // Urenmail: vrijdag, 30 minuten na uitklokken.
        val uit = klok.vandaag.eind
        val mail = uit?.let { nu.toLocalDate().atTime(it).plusMinutes(Instellingen.URENMAIL_NA_MINUTEN) }
        if (nu.dayOfWeek == DayOfWeek.FRIDAY && !klok.isIn && mail != null && mail.isAfter(nu)) {
            zet(context, URENMAIL, 4, mail)
        } else {
            stop(context, URENMAIL, 4)
        }

        // Seintje de dag ervoor, voor alles met een datum.
        opslag.onthoudNu()
            .filter { it.seintje && !it.klaar && it.wanneer != null }
            .forEach { o ->
                val moment = o.wanneer!!.minusDays(1).atTime(Instellingen.SEINTJE_OM)
                val code = Meldingen.seintjeId(o.id)
                if (moment.isAfter(nu)) zet(context, SEINTJE, code, moment, o.id) else stop(context, SEINTJE, code, o.id)
            }
    }

    /** Eerstvolgende werkdag (ma–vr) om [tijd], vanaf nu. */
    fun volgendeWerkdagOm(tijd: LocalTime, nu: LocalDateTime): LocalDateTime {
        var dag: LocalDate = nu.toLocalDate()
        while (!Instellingen.isWerkdag(dag) || !dag.atTime(tijd).isAfter(nu)) dag = dag.plusDays(1)
        return dag.atTime(tijd)
    }

    private fun intent(context: Context, actie: String, code: Int, id: String?): PendingIntent {
        val intent = Intent(context, Ontvanger::class.java).setAction(actie).putExtra(EXTRA_ID, id)
        return PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun zet(context: Context, actie: String, code: Int, moment: LocalDateTime, id: String? = null) {
        val millis = moment.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        context.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent(context, actie, code, id))
    }

    private fun stop(context: Context, actie: String, code: Int, id: String? = null) {
        context.getSystemService(AlarmManager::class.java).cancel(intent(context, actie, code, id))
    }
}

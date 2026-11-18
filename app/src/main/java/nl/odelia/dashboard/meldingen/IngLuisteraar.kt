package nl.odelia.dashboard.meldingen

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.time.Instant
import java.time.ZoneId
import nl.odelia.dashboard.data.GeldOpslag
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.IngImport
import nl.odelia.dashboard.logica.IngLees
import nl.odelia.dashboard.logica.IngUitkomst
import nl.odelia.dashboard.online.Sync

/**
 * Leest meldingen van de ING-app en zet uitgaven in Geld (zie [IngImport]).
 *
 * - Werkt pas als Odelia "Toegang tot meldingen" aanzet voor Mijn dashboard (Android-instellingen).
 * - Android geeft deze luisteraar álle meldingen, maar alles wat niet van ING ([IngImport.PAKKETTEN]) komt,
 *   wordt meteen genegeerd: niet gelezen, niet bewaard.
 * - Gaat zelf niet online. Geld gaat wel mee met de sync, alleen als Odelia is ingelogd.
 */
class IngLuisteraar : NotificationListenerService() {

    override fun onListenerConnected() {
        // Zodat een ING-uitgave ook gesynct wordt als de app zelf niet open is.
        Sync.start(applicationContext)
        Meldingen.maakKanalen(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName !in IngImport.PAKKETTEN) return
        val n = sbn.notification ?: return
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val e = n.extras ?: return
        val titel = e.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val tekst = (e.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: e.getCharSequence(Notification.EXTRA_TEXT))?.toString()

        val lees = IngImport.lees(titel, tekst)
        if (lees == IngLees.Geen) return

        val tijd = if (n.`when` > 0) n.`when` else sbn.postTime
        // Dezelfde melding (ook als ING hem bijwerkt) geeft dezelfde code.
        val bron = "ing:$tijd:${"$titel|$tekst".hashCode()}"
        val datum = Instant.ofEpochMilli(tijd).atZone(ZoneId.systemDefault()).toLocalDate()

        val app = applicationContext
        Sync.start(app)
        GeldOpslag.van(app).wijzig { d ->
            val (nieuw, uitkomst) = IngImport.verwerk(d, lees, bron, datum)
            if (uitkomst is IngUitkomst.Gevraagd) {
                val wacht = nieuw.ingWacht.size
                Meldingen.toon(
                    app,
                    Meldingen.ID_GELD,
                    Meldingen.KANAAL_GELD,
                    titel = "🏦 Nieuwe uitgave: welk potje?",
                    tekst = "${Geld.bedrag(uitkomst.wacht.cent)} bij ${uitkomst.wacht.winkel}." +
                        if (wacht > 1) " ($wacht wachten op een potje)" else "",
                    scherm = "geld",
                    geheim = true,
                )
            }
            nieuw
        }
    }
}

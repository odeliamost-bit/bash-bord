package nl.odelia.dashboard.meldingen

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.odelia.dashboard.R
import nl.odelia.dashboard.data.Bestanden
import nl.odelia.dashboard.data.GeldOpslag
import nl.odelia.dashboard.logica.Bank
import nl.odelia.dashboard.logica.BankMelding
import nl.odelia.dashboard.logica.BankWacht
import nl.odelia.dashboard.logica.BoekSoort
import nl.odelia.dashboard.logica.Boeking
import nl.odelia.dashboard.logica.GeldBoek
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.online.Sync

/**
 * Leest mee met meldingen, maar alleen die van de ING-app ([BankMelding.ING]). Al het andere wordt
 * meteen overgeslagen en nergens bewaard. Werkt alleen als Odelia het bij Geld aanzet én in de
 * Android-instellingen toestemming geeft ("Meldingen lezen"). De tekst blijft op de telefoon.
 */
class BankLuisteraar : NotificationListenerService() {
    private val werk = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName != BankMelding.ING) return
        val n = sbn.notification ?: return
        // De samenvatting van een groepje ("3 meldingen") is geen betaling.
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val extra = n.extras ?: return
        val titel = extra.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val tekst = (extra.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: extra.getCharSequence(Notification.EXTRA_TEXT))?.toString()
        val app = applicationContext
        val sleutel = sbn.key
        val tijd = sbn.postTime
        werk.launch {
            try {
                BankInbox.binnen(app, sleutel, tijd, titel, tekst)
            } catch (_: Exception) {
                // Lukt het een keer niet, dan mag de telefoon daar niks van merken.
            }
        }
    }
}

/** Wat er met een ING-melding gebeurt: vanzelf boeken, of vragen in welk potje. Ook voor de knoppen in die vraag. */
object BankInbox {
    const val KIES = "nl.odelia.dashboard.BANK_KIES"
    const val EXTRA_WACHT = "wacht"
    const val EXTRA_KEUZE = "keuze"
    private const val ID_BASIS = 2_000_000
    private val slot = Mutex()

    fun meldingId(wachtId: String) = ID_BASIS + (wachtId.hashCode() and 0xFFFFF)

    /** Heeft de app in de Android-instellingen toestemming om meldingen te lezen? */
    fun heeftToegang(context: Context): Boolean =
        context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)

    suspend fun binnen(context: Context, sleutel: String, tijd: Long, titel: String?, tekst: String?) = slot.withLock {
        val opslag = Bestanden.bank(context)
        val d = opslag.nu()
        if (!d.aan) return@withLock
        val merk = "$sleutel|${(titel.orEmpty() + "\n" + tekst.orEmpty()).hashCode()}"
        if (Bank.alGezien(d, merk, tijd)) return@withLock

        val bericht = BankMelding.lees(titel, tekst)
        if (bericht == null) {
            // Niet begrepen maar wel een bedrag: bewaren (alleen hier), zodat we het later kunnen verbeteren.
            val raar = if (BankMelding.nietBegrepen(titel, tekst)) listOfNotNull(titel, tekst).joinToString(" — ") else null
            opslag.vervang { x -> Bank.zetGezien(x, merk, tijd).let { if (raar != null) Bank.onthoudNietHerkend(it, raar) else it } }
            return@withLock
        }

        val geld = GeldOpslag.van(context).nu()
        val vandaag = LocalDate.now()
        val regel = Bank.regelVoor(d, geld, bericht)
        if (regel != null) {
            opslag.vervang { Bank.zetGezien(it, merk, tijd) }
            boek(context, Bank.boeking(GeldBoek.nieuwId(), bericht.soort, bericht.cent, bericht.naam, vandaag, regel.potjeId, regel.categorie))
        } else {
            val wacht = BankWacht(GeldBoek.nieuwId(), bericht.soort, bericht.cent, bericht.naam, vandaag)
            opslag.vervang { x -> Bank.zetGezien(x, merk, tijd).let { it.copy(wachtrij = it.wachtrij + wacht) } }
            vraag(context, wacht, geld)
        }
    }

    /** Odelia koos een potje (of soort inkomen), in de melding of in de app. */
    suspend fun kies(context: Context, wachtId: String, keuze: String) = slot.withLock {
        val geld = GeldOpslag.van(context).nu()
        var boeking: Boeking? = null
        Bestanden.bank(context).vervang { d ->
            val (nieuw, b) = Bank.kies(d, geld, wachtId, keuze, GeldBoek.nieuwId())
            boeking = b
            nieuw
        }
        boeking?.let { boek(context, it) }
        Meldingen.weg(context, meldingId(wachtId))
    }

    /** "Niet boeken": de wachtende betaling weggooien. */
    suspend fun gooiWeg(context: Context, wachtId: String) = slot.withLock {
        Bestanden.bank(context).vervang { Bank.gooiWeg(it, wachtId) }
        Meldingen.weg(context, meldingId(wachtId))
    }

    private fun boek(context: Context, b: Boeking) {
        // Ook als de app niet open is: de sync moet weten dat Geld veranderd is, anders raakt de boeking zoek.
        Sync.start(context)
        GeldOpslag.van(context).wijzig { GeldBoek.voegToe(it, b) }
        val pot = GeldOpslag.van(context).data.value.potje(b.potjeId)
        Bestanden.log(context, "🏦", "ING: ${nl.odelia.dashboard.logica.Geld.bedrag(b.cent)} ${if (b.soort == BoekSoort.In) "erbij in" else "uit"} ${pot?.naam ?: "potje"}")
    }

    /** Onbekende winkel: melding met maximaal 3 knoppen (meer laat Android niet toe). De rest kiest ze in de app. */
    @SuppressLint("MissingPermission")
    private fun vraag(context: Context, w: BankWacht, geld: GeldData) {
        Meldingen.maakKanalen(context)
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        val id = meldingId(w.id)
        val tekst = if (w.soort == BoekSoort.In) "Wat voor geld is dit? Tik op een knop. Iets anders: tik hier."
        else "Uit welk potje? Tik op een knop. Ander potje: tik hier."
        val melding = NotificationCompat.Builder(context, Meldingen.KANAAL_GELD)
            .setSmallIcon(R.drawable.ic_melding)
            .setColor(0xFFD9694F.toInt())
            .setContentTitle(Bank.vraagTitel(w))
            .setContentText(tekst)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tekst))
            .setContentIntent(Meldingen.openApp(context, "geld", id))
            .setAutoCancel(true)
        Bank.keuzes(geld, w.soort).take(3).forEach { k -> melding.addAction(0, k.label, knop(context, w.id, k.id)) }
        try {
            nm.notify(id, melding.build())
        } catch (_: SecurityException) {
            // Geen toestemming voor meldingen: dan staat hij alleen in de app bij Geld.
        }
    }

    private fun knop(context: Context, wachtId: String, keuze: String): PendingIntent = PendingIntent.getBroadcast(
        context, (wachtId + keuze).hashCode(),
        Intent(context, Ontvanger::class.java).setAction(KIES).putExtra(EXTRA_WACHT, wachtId).putExtra(EXTRA_KEUZE, keuze),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

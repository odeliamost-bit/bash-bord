package nl.odelia.dashboard.meldingen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.odelia.dashboard.MainActivity
import nl.odelia.dashboard.data.Instellingen
import nl.odelia.dashboard.data.KlokOpslag
import nl.odelia.dashboard.data.OnthoudOpslag
import nl.odelia.dashboard.onthoud.Ochtend
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.ui.thuis.tijdTekst
import nl.odelia.dashboard.uren.DagSoort
import nl.odelia.dashboard.uren.KlokStaat
import nl.odelia.dashboard.uren.Planning
import nl.odelia.dashboard.uren.Urenmail

/** Herinneringen om in en uit te klokken (ma–vr 8:15 en 16:15). */
object Herinneringen {
    const val CHECK_IN = "nl.odelia.dashboard.CHECK_IN"
    const val CHECK_UIT = "nl.odelia.dashboard.CHECK_UIT"
    const val INKLOKKEN = "nl.odelia.dashboard.INKLOKKEN"
    const val UITKLOKKEN = "nl.odelia.dashboard.UITKLOKKEN"
    const val VRIJ = "nl.odelia.dashboard.VRIJ"
    const val ZIEK = "nl.odelia.dashboard.ZIEK"
    const val LATER_10 = "nl.odelia.dashboard.LATER_10"
    const val LATER_20 = "nl.odelia.dashboard.LATER_20"
    const val CHECK_MORGEN = "nl.odelia.dashboard.CHECK_MORGEN"
    const val OCHTEND = "nl.odelia.dashboard.OCHTEND"
    const val AVOND = "nl.odelia.dashboard.AVOND"
    const val AVOND_SEINTJE = "nl.odelia.dashboard.AVOND_SEINTJE"
    const val URENMAIL = "nl.odelia.dashboard.URENMAIL"

    /** Stap 5: vrijdag 30 minuten na het uitklokken: "urenmail staat klaar". */
    fun planUrenmail(context: Context, uitgeklokt: LocalDateTime) {
        if (uitgeklokt.dayOfWeek == DayOfWeek.FRIDAY) {
            Meldingen.zetWekker(context, Seintje.Urenmail, URENMAIL, uitgeklokt.plus(Urenmail.NA_UITKLOKKEN))
        }
    }

    /** Toch weer ingeklokt: dan nog niet. Bij het volgende uitklokken komt een nieuwe. */
    fun stopUrenmail(context: Context) = Meldingen.haalWekkerWeg(context, Seintje.Urenmail, URENMAIL)

    /** Stap 4: 20 minuten na het eerste inklokken van de dag een overzicht. */
    fun planOchtend(context: Context, ingeklokt: LocalDateTime) =
        Meldingen.zetWekker(context, Seintje.Ochtend, OCHTEND, ingeklokt.plus(Ochtend.NA_INKLOKKEN))

    /** De volgende controles inplannen. Mag vaak gebeuren: een nieuwe wekker vervangt de oude. */
    fun plan(context: Context, nu: LocalDateTime = LocalDateTime.now()) {
        Meldingen.zetWekker(context, Seintje.Inklokken, CHECK_IN, Planning.volgende(nu, Planning.INKLOKKEN))
        Meldingen.zetWekker(context, Seintje.Uitklokken, CHECK_UIT, Planning.volgende(nu, Planning.UITKLOKKEN))
        Meldingen.zetWekker(context, Seintje.MorgenTeDoen, CHECK_MORGEN, Planning.volgendeDag(nu, Planning.SEINTJE_RESERVE))
    }

    /**
     * Het ochtendbericht (stap 4), met het seintje voor morgen erin.
     * Staat er iets voor morgen? Dan de knop "Vanavond nog een keer". Na die knop: [avondGepland] = true.
     */
    fun toonOchtend(context: Context, staat: OnthoudStaat, avondGepland: Boolean = false) {
        val b = Ochtend.bericht(staat)
        val tekst = if (avondGepland) "${b.tekst}\n\n🌙 Vanavond om ${tijdTekst(Planning.SEINTJE_AVOND)} krijg je het nog een keer." else b.tekst
        val knoppen = if (b.morgen && !avondGepland) listOf("Vanavond nog een keer" to AVOND) else emptyList()
        Meldingen.toon(context, Seintje.Ochtend, Meldingen.KANAAL_ONTHOUD, b.titel, tekst, knoppen)
        Instellingen.van(context).laatsteOchtendbericht = staat.vandaag.toString()
    }

    /** Knop "Vanavond nog een keer": om 19:00 (of over een uur, als het al later is). */
    fun planAvond(context: Context, nu: LocalDateTime) {
        val avond = nu.toLocalDate().atTime(Planning.SEINTJE_AVOND)
        Meldingen.zetWekker(context, Seintje.Avond, AVOND_SEINTJE, if (avond.isAfter(nu)) avond else nu.plusHours(1))
    }

    /** "Morgen: Verslag inleveren (Dhr. Jansen)" voor alles met "seintje de dag ervoor" ('s avonds). */
    fun toonMorgen(context: Context, staat: OnthoudStaat) {
        val lijst = staat.seintjeVoorMorgen
        if (lijst.isEmpty()) return
        val regels = lijst.joinToString("\n") { a -> "• ${a.wat}" + (staat.persoon(a.persoonId)?.let { " (${it.naam})" } ?: "") }
        Meldingen.toon(
            context, Seintje.MorgenTeDoen, Meldingen.KANAAL_ONTHOUD,
            if (lijst.size == 1) "Morgen: ${lijst[0].wat} 📌" else "Morgen ${lijst.size} dingen 📌",
            regels,
        )
    }

    /** Meldingen die niet meer kloppen weghalen (bv. "nog niet ingeklokt" terwijl je net bent ingeklokt). */
    fun ruimOp(context: Context, staat: KlokStaat) {
        if (staat.gewerktVandaag || staat.vandaag.soort != DagSoort.Werk) Meldingen.weg(context, Seintje.Inklokken)
        if (!staat.isIn) {
            Meldingen.weg(context, Seintje.Uitklokken)
            Meldingen.haalWekkerWeg(context, Seintje.UitklokkenLater, CHECK_UIT)
        }
    }

    fun toonInklokken(context: Context) = Meldingen.toon(
        context, Seintje.Inklokken, Meldingen.KANAAL_KLOKKEN,
        "Goedemorgen! ☀️",
        "Je bent nog niet ingeklokt.",
        listOf("Inklokken" to INKLOKKEN, "Vandaag vrij" to VRIJ, "Ziek" to ZIEK),
    )

    fun toonUitklokken(context: Context, staat: KlokStaat) = Meldingen.toon(
        context, Seintje.Uitklokken, Meldingen.KANAAL_KLOKKEN,
        "Nog aan het werk?",
        "Je bent ingeklokt sinds ${staat.sinds?.let(::tijdTekst) ?: "vanochtend"}.",
        listOf("Uitklokken" to UITKLOKKEN, "Over 10 min" to LATER_10, "Over 20 min" to LATER_20),
    )
}

/** Ontvangt de wekkers en de knoppen in de meldingen. */
class MeldingOntvanger : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val klaar = goAsync()
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                verwerk(app, intent.action ?: return@launch)
            } finally {
                klaar.finish()
            }
        }
    }

    private suspend fun verwerk(context: Context, actie: String) {
        val opslag = KlokOpslag(context)
        val nu = LocalDateTime.now()
        when (actie) {
            Herinneringen.CHECK_IN -> {
                val s = opslag.nu().staat(nu, Instellingen.van(context).weekdoel.value)
                if (Planning.isWerkdag(nu.dayOfWeek) && !s.gewerktVandaag && s.vandaag.soort == DagSoort.Werk) {
                    Herinneringen.toonInklokken(context)
                }
                Herinneringen.plan(context, nu)
            }
            Herinneringen.CHECK_UIT -> {
                val s = opslag.nu().staat(nu, Instellingen.van(context).weekdoel.value)
                if (s.isIn) Herinneringen.toonUitklokken(context, s)
                Herinneringen.plan(context, nu)
            }
            Herinneringen.OCHTEND -> Herinneringen.toonOchtend(context, OnthoudOpslag(context).staat(nu.toLocalDate()))
            Herinneringen.AVOND -> {
                Herinneringen.planAvond(context, nu)
                // Zelfde bericht nog eens, zonder knop en met "vanavond om 19:00": dan zie je dat het gelukt is.
                Herinneringen.toonOchtend(context, OnthoudOpslag(context).staat(nu.toLocalDate()), avondGepland = true)
            }
            Herinneringen.AVOND_SEINTJE -> Herinneringen.toonMorgen(context, OnthoudOpslag(context).staat(nu.toLocalDate()))
            Herinneringen.URENMAIL -> Meldingen.toon(
                context, Seintje.Urenmail, Meldingen.KANAAL_ONTHOUD,
                "📧 Urenmail voor Kim staat klaar",
                "Tik om hem na te kijken en in Gmail te zetten. De app verstuurt niets zelf.",
                openActie = MainActivity.ACTIE_URENMAIL,
            )
            Herinneringen.CHECK_MORGEN -> {
                // Reserve om 10:00: nog geen ochtendbericht vandaag (niet ingeklokt) en wel iets voor morgen.
                val staat = OnthoudOpslag(context).staat(nu.toLocalDate())
                if (Instellingen.van(context).laatsteOchtendbericht != staat.vandaag.toString() && staat.seintjeVoorMorgen.isNotEmpty()) {
                    Herinneringen.toonOchtend(context, staat)
                }
                Herinneringen.plan(context, nu)
            }
            Herinneringen.LATER_10, Herinneringen.LATER_20 -> {
                Meldingen.weg(context, Seintje.Uitklokken)
                val minuten = if (actie == Herinneringen.LATER_10) 10L else 20L
                Meldingen.zetWekker(context, Seintje.UitklokkenLater, Herinneringen.CHECK_UIT, nu.plusMinutes(minuten))
            }
            Herinneringen.INKLOKKEN -> opslag.klokIn(nu)
            Herinneringen.UITKLOKKEN -> opslag.klokUit(nu)
            Herinneringen.VRIJ -> opslag.zetSoort(nu.toLocalDate(), DagSoort.Vrij)
            Herinneringen.ZIEK -> opslag.zetSoort(nu.toLocalDate(), DagSoort.Ziek)
        }
    }
}

/** Na het opnieuw opstarten van de telefoon zijn alle wekkers weg: opnieuw zetten. */
class OpstartOntvanger : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Herinneringen.plan(context.applicationContext)
        }
    }
}

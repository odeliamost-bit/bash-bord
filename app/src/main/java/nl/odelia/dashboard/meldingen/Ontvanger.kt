package nl.odelia.dashboard.meldingen

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.odelia.dashboard.Acties
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.Instellingen
import nl.odelia.dashboard.logica.ochtendTekst

/** Hier komen de wekkers van [Planner] en de knoppen in meldingen binnen. */
class Ontvanger : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bezig = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                verwerk(context.applicationContext, intent)
                Planner.plan(context.applicationContext)
            } finally {
                bezig.finish()
            }
        }
    }

    private suspend fun verwerk(context: Context, intent: Intent) {
        val opslag = Opslag.van(context)
        val klok = opslag.klokNu()
        when (intent.action) {
            Planner.HERINNER_IN ->
                if (Instellingen.isWerkdag(LocalDate.now()) && !klok.isIn && klok.vandaag.soort == DagSoort.Leeg) {
                    Meldingen.toon(
                        context, Meldingen.ID_KLOK, Meldingen.KANAAL_KLOK,
                        "Vergeet niet in te klokken", "Tik op Inklokken als je begonnen bent.",
                        knop = "Inklokken" to knop(context, INKLOKKEN),
                    )
                }

            Planner.HERINNER_UIT ->
                if (klok.isIn) {
                    Meldingen.toon(
                        context, Meldingen.ID_KLOK, Meldingen.KANAAL_KLOK,
                        "Vergeet niet uit te klokken", "Klaar met werken? Tik op Uitklokken.",
                        knop = "Uitklokken" to knop(context, UITKLOKKEN),
                    )
                }

            INKLOKKEN -> {
                if (!klok.isIn) Acties.wisselKlok(context)
                Meldingen.weg(context, Meldingen.ID_KLOK)
            }

            UITKLOKKEN -> {
                if (klok.isIn) Acties.wisselKlok(context)
                Meldingen.weg(context, Meldingen.ID_KLOK)
            }

            Planner.OCHTEND -> Meldingen.toon(
                context, Meldingen.ID_OCHTEND, Meldingen.KANAAL_OCHTEND,
                "Goedemorgen! Dit staat er", ochtendTekst(opslag.onthoudNu(), LocalDate.now()),
                scherm = "onthoud",
            )

            Planner.URENMAIL -> Meldingen.toon(
                context, Meldingen.ID_URENMAIL, Meldingen.KANAAL_URENMAIL,
                "Urenmail voor Kim staat klaar", "Tik om de mail te bekijken en in Gmail te zetten.",
                scherm = "urenmail",
            )

            Planner.SEINTJE -> {
                val id = intent.getStringExtra(Planner.EXTRA_ID) ?: return
                val o = opslag.onthoudMet(id) ?: return
                if (o.klaar || !o.seintje) return
                Meldingen.toon(
                    context, Meldingen.seintjeId(id), Meldingen.KANAAL_ONTHOUD,
                    "Morgen: ${o.titel}", listOfNotNull(o.wie, o.waar).joinToString(" · ").ifBlank { "Niet vergeten!" },
                    scherm = "onthoud",
                )
            }
        }
    }

    private fun knop(context: Context, actie: String): PendingIntent = PendingIntent.getBroadcast(
        context, actie.hashCode(), Intent(context, Ontvanger::class.java).setAction(actie),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val INKLOKKEN = "nl.odelia.dashboard.INKLOKKEN"
        const val UITKLOKKEN = "nl.odelia.dashboard.UITKLOKKEN"
    }
}

/** Na het opstarten van de telefoon (of na een update) zijn alle wekkers weg: opnieuw zetten. */
class OpstartOntvanger : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bezig = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Planner.plan(context.applicationContext)
            } finally {
                bezig.finish()
            }
        }
    }
}

package nl.odelia.dashboard.ui.geld

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import nl.odelia.dashboard.logica.Bank
import nl.odelia.dashboard.logica.BoekSoort
import nl.odelia.dashboard.logica.GeldBoek
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.Tekst

// Geld uit ING-meldingen (nov 2026). De regels staan in logica/BankMelding.kt, het meelezen in meldingen/BankLuisteraar.kt.

/** Bovenaan Geld, alleen als er iets wacht: betalingen van een winkel die de app nog niet kent. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BankWachtKaart(staat: AppStaat, acties: AppActies, onMelding: (String) -> Unit) {
    val wachtrij = staat.bank.wachtrij
    if (wachtrij.isEmpty()) return
    val vandaag = staat.nu.toLocalDate()
    Paneel {
        GeldKopje(if (wachtrij.size == 1) "🔔 1 betaling wacht op jou" else "🔔 ${wachtrij.size} betalingen wachten op jou")
        // Nieuwste eerst, en niet meer dan 5 tegelijk: anders wordt het een lange lap.
        wachtrij.takeLast(5).reversed().forEach { w ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Tekst(Bank.vraagTitel(w), 18.sp, kleur = Bruin)
                Tekst(
                    (if (w.soort == BoekSoort.In) "Wat voor geld is dit?" else "Uit welk potje?") + " · " + GeldBoek.dagTekst(w.datum, vandaag),
                    14.sp, dikte = FontWeight.ExtraBold, kleur = Grijs,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Bank.keuzes(staat.geld, w.soort).forEach { k ->
                        Chip(k.label, aan = false, onClick = {
                            acties.kiesBank(w.id, k.id)
                            onMelding("Geboekt. Ik onthoud het voor de volgende keer.")
                        })
                    }
                    Chip("🗑️ Niet boeken", aan = false, onClick = { acties.gooiBankWeg(w.id) })
                }
            }
        }
        if (wachtrij.size > 5) Tekst("En nog ${wachtrij.size - 5} oudere.", 14.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
    }
}

/** Onderaan Geld: aan/uit, toestemming, onthouden winkels en wat niet begrepen is. */
@Composable
internal fun BankKaart(staat: AppStaat, acties: AppActies, onMelding: (String) -> Unit) {
    val b = staat.bank
    var regelsOpen by remember { mutableStateOf(false) }
    var raarOpen by remember { mutableStateOf(false) }

    // Net terug uit de Android-instellingen? Even blijven kijken of de toestemming er nu is.
    LaunchedEffect(b.aan, staat.bankToegang) {
        while (b.aan && !staat.bankToegang) {
            acties.kijkBankToegang()
            delay(2_000)
        }
    }

    Paneel {
        GeldKopje("🔔 Geld uit ING-meldingen")
        Tekst("Betaal je met ING? Dan zet ik het bedrag vanzelf in het goede potje.", 15.sp, dikte = FontWeight.ExtraBold, kleur = BruinZacht)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Chip("Uit", aan = !b.aan, onClick = { acties.zetBankAan(false) })
            Chip("Aan", aan = b.aan, onClick = { acties.zetBankAan(true) })
        }

        if (b.aan && !staat.bankToegang) {
            Tekst("Nog één stap: geef toestemming.", 16.sp, kleur = Bruin)
            Tekst("1. Tik op de groene knop.\n2. Zet \"Mijn dashboard\" aan.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
            Tekst(
                "Is de knop grijs? Ga naar Instellingen → Apps → Mijn dashboard → ⋮ rechtsboven → \"Beperkte instellingen toestaan\". Probeer het dan opnieuw.",
                14.sp, dikte = FontWeight.Bold, kleur = Grijs,
            )
            GroeneKnop("Toestemming geven", onClick = {
                if (!acties.openMeldingToegang()) onMelding("Openen lukte niet. Zoek in Instellingen op \"Meldingen lezen\".")
            })
        } else if (b.aan) {
            Tekst("✅ Staat aan. Ik lees alleen meldingen van de ING-app.", 15.sp, dikte = FontWeight.ExtraBold, kleur = GroenDonker)
            Tekst("Zet in de ING-app de meldingen voor af- en bijschrijvingen aan.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }

        if (b.regels.isNotEmpty()) {
            Row { Chip("🧠 Onthouden winkels: ${b.regels.size}", aan = regelsOpen, onClick = { regelsOpen = !regelsOpen }) }
            if (regelsOpen) {
                Tekst("Tik op een regel om hem te vergeten. Dan vraag ik het weer.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
                b.regels.forEach { r ->
                    val doel = if (r.soort == BoekSoort.In) categorie(r.categorie).let { "${it.emoji} ${it.label}" }
                    else staat.geld.potje(r.potjeId)?.let { "${it.emoji} ${it.naam}" } ?: "potje is weg"
                    WitteRegel(
                        onClick = {
                            acties.vergeetBankRegel(r)
                            onMelding("Vergeten. Ik vraag het de volgende keer weer.")
                        },
                        uitleg = "Vergeet ${r.naam}",
                    ) {
                        Tekst("${r.naam} → $doel", 15.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = BruinZacht)
                        Tekst("✕", 17.sp, kleur = Grijs)
                    }
                }
            }
        }

        if (b.nietHerkend.isNotEmpty()) {
            Row { Chip("❓ Niet begrepen: ${b.nietHerkend.size}", aan = raarOpen, onClick = { raarOpen = !raarOpen }) }
            if (raarOpen) {
                Tekst("Deze ING-meldingen snapte ik niet. Laat ze aan Claude zien, dan kan het beter.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
                b.nietHerkend.forEach { t -> WitteRegel { Tekst(t, 14.sp, Modifier.weight(1f), dikte = FontWeight.Bold, kleur = BruinZacht) } }
                Row { Chip("🗑️ Wissen", aan = false, onClick = { acties.wisBankNietHerkend() }) }
            }
        }

        Tekst("🔒 De tekst van je meldingen blijft op dit apparaat. Alleen de boeking komt bij Geld.", 13.sp, dikte = FontWeight.ExtraBold, kleur = GroenDonker)
    }
}

package nl.odelia.dashboard.ui.werk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoerveld
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Knop
import nl.odelia.dashboard.ui.onderdelen.SchermKop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastels
import nl.odelia.dashboard.ui.thuis.GroteKnop
import nl.odelia.dashboard.ui.thuis.dagKort
import nl.odelia.dashboard.ui.thuis.tijdTekst
import nl.odelia.dashboard.uren.DagSoort
import nl.odelia.dashboard.uren.KlokStaat
import nl.odelia.dashboard.uren.MailActies
import nl.odelia.dashboard.uren.Uren
import nl.odelia.dashboard.uren.Urenmail

/**
 * De urenmail voor Kim klaarzetten. De app verstuurt NOOIT zelf:
 * "Open in Gmail" zet alleen een concept klaar, jij drukt zelf op verzenden.
 */
@Composable
fun UrenmailScherm(
    klok: KlokStaat,
    kimAdres: String,
    acties: MailActies,
    onMelding: (String) -> Unit,
    onTerug: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vandaag = klok.nu.toLocalDate()
    val dezeWeek = Uren.maandag(vandaag)
    var maandag by rememberSaveable { mutableStateOf(Urenmail.standaardWeek(vandaag)) }
    var opmerking by rememberSaveable { mutableStateOf("") }
    val regels = Urenmail.regels(maandag, klok)
    val tekst = Urenmail.tekst(regels, opmerking)
    val klembord = LocalClipboardManager.current

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SchermKop("Urenmail", onTerug)

            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Keuze("vorige week", aan = maandag == dezeWeek.minusWeeks(1), onClick = { maandag = dezeWeek.minusWeeks(1) }, modifier = Modifier.weight(1f))
                Keuze("deze week", aan = maandag == dezeWeek, onClick = { maandag = dezeWeek }, modifier = Modifier.weight(1f))
            }

            Invoerveld(kimAdres, acties.zetAdres, "Aan: e-mailadres van Kim (mag leeg)", eenRegel = true)
            Invoerveld(opmerking, { opmerking = it }, "Opmerking voor Kim (mag leeg)")

            // Voorbeeld van de mail
            Column(
                Modifier
                    .fillMaxWidth()
                    .klei(Color.White, RoundedCornerShape(26.dp), Kleur.KaartSchaduw)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Tekst("Onderwerp: ${Urenmail.ONDERWERP}", 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                // De brief zelf; het overzicht eronder als tabel (in de mail zijn het gewone regels).
                Tekst(tekst.substringBefore("\n\n" + regels.firstOrNull()?.let(Urenmail::regel)), 16.sp, dikte = FontWeight.Bold, kleur = Kleur.Inkt, regel = 22.sp)
                UrenTabel(regels)
            }

            GroteKnop("Open in Gmail", onClick = { acties.openMail(kimAdres, Urenmail.ONDERWERP, tekst) }, icoon = Icoontjes.mailWit)
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Knop(
                    "Kopiëren",
                    onClick = {
                        klembord.setText(AnnotatedString(tekst))
                        onMelding("Gekopieerd. Plak het in je antwoord.")
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icoon = Icoontjes.plakken,
                )
                Knop("Delen", onClick = { acties.deel(Urenmail.ONDERWERP, tekst) }, modifier = Modifier.weight(1f).fillMaxHeight())
            }

            Uitleg()
        }
    }
}

/** Overzicht ma–vr als tabel: datum, begin, eind, uren (of vrij/ziek). */
@Composable
private fun UrenTabel(regels: List<Urenmail.Regel>) {
    @Composable
    fun Rij(datum: String, begin: String, eind: String, uren: String, vet: Boolean = false) {
        val dikte = if (vet) FontWeight.Black else FontWeight.Bold
        val kleur = if (vet) Kleur.Inkt else Kleur.InktZacht
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Tekst(datum, 15.sp, Modifier.weight(1.4f), dikte = dikte, kleur = kleur, eenRegel = true)
            Tekst(begin, 15.sp, Modifier.weight(1f), dikte = dikte, kleur = kleur, eenRegel = true)
            Tekst(eind, 15.sp, Modifier.weight(1f), dikte = dikte, kleur = kleur, eenRegel = true)
            Tekst(uren, 15.sp, Modifier.weight(0.9f), dikte = dikte, kleur = kleur, uitlijning = TextAlign.End, eenRegel = true)
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .background(Kleur.Kaart, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Rij("Datum", "Begin", "Eind", "Uren", vet = true)
        regels.forEach { r ->
            val datum = "${dagKort(r.datum.dayOfWeek)} ${r.datum.dayOfMonth}-${r.datum.monthValue}"
            when {
                r.soort != DagSoort.Werk -> Rij(datum, r.soort.tekst, "", "")
                r.uren == null || r.begin == null -> Rij(datum, "–", "", "")
                else -> Rij(datum, tijdTekst(r.begin), r.eind?.let(::tijdTekst) ?: "…", Urenmail.uurMin(r.uren))
            }
        }
        Rij("Totaal", "", "", Urenmail.uurMin(regels.sumOf { (it.uren ?: 0f).toDouble() }.toFloat()), vet = true)
        // Vrije en zieke dagen tellen niet mee, maar staan er wel bij.
        Uren.vrijZiek(regels.map { it.soort })?.let {
            Tekst("+ $it", 15.sp, Modifier.fillMaxWidth(), dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht, uitlijning = TextAlign.End)
        }
    }
}

/** Korte uitleg: hoe kom je in de bestaande draad, en: de app verstuurt nooit zelf. */
@Composable
private fun Uitleg() {
    val p = Pastels.Blauw
    Column(
        Modifier
            .fillMaxWidth()
            .background(p.kaart, RoundedCornerShape(26.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tekst("💡 Zo werkt het", 18.sp, kleur = p.groot)
        listOf(
            "✉️ \"Open in Gmail\" zet een nieuwe mail klaar. Jij kijkt hem na en drukt zelf op verzenden.",
            "↩️ Liever antwoorden in de mail \"Urenregistratie\"? Tik Kopiëren. Open die mail in Gmail, tik Beantwoorden en plak.",
            "🔒 De app verstuurt nooit zelf een mail.",
        ).forEach { Tekst(it, 15.sp, dikte = FontWeight.Bold, kleur = p.label, regel = 20.sp) }
    }
}

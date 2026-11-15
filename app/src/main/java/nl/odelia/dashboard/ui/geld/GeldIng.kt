package nl.odelia.dashboard.ui.geld

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.GeldBoek
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.logica.IngImport
import nl.odelia.dashboard.logica.IngWacht
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Nunito

private val IngOranje = Color(0xFFFF6200)
private val IngLicht = Color(0xFFFFE3CF)

/**
 * Bovenaan Geld als er een ING-uitgave bij een onbekende winkel wacht:
 * "€ 4,50 bij Jumbo. Welk potje?" Na kiezen onthoudt de app de winkel.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IngVraag(data: GeldData, vandaag: java.time.LocalDate, acties: AppActies, onMelding: (String) -> Unit, lees: (String) -> Unit) {
    val w: IngWacht = data.ingWacht.firstOrNull() ?: return
    var naam by remember(w.id) { mutableStateOf(IngImport.mooi(w.winkel)) }
    var pot by remember(w.id) { mutableStateOf<String?>(null) }
    var cat by remember(w.id) { mutableStateOf(IngImport.gokCategorie(w.winkel)) }

    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .border(3.dp, IngOranje.copy(alpha = 0.6f), RoundedCornerShape(30.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EmojiBol("🏦", IngLicht, 48.dp)
            Column(Modifier.weight(1f)) {
                Tekst("Nieuw van ING", 15.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
                Tekst("${Geld.bedrag(w.cent)} bij ${w.winkel}", 20.sp, kleur = Bruin, regel = 24.sp)
                Tekst(GeldBoek.dagTekst(w.datum, vandaag), 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
            }
            VoorleesBol { lees("${GeldBoek.uitspraak(w.cent)} bij ${IngImport.mooi(w.winkel)}. Welk potje?") }
        }
        if (data.ingWacht.size > 1) {
            Tekst("Er wachten er nog ${data.ingWacht.size - 1}.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }

        GeldKopje("🫙 Welk potje?")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.potjes.forEach { p -> Chip("${p.emoji} ${p.naam}", pot == p.id, { pot = p.id }) }
        }

        GeldKopje("🏷️ Wat was het?")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val lijst = Geld.uitgaven.filter { it.vaak || it.id == cat }
            lijst.forEach { c -> Chip("${c.emoji} ${c.label}", cat == c.id, { cat = c.id }) }
        }

        GeldKopje("🏪 Onthoud als")
        NaamVeld(naam, { naam = it }, "Naam van de winkel")
        Tekst("Volgende keer bij ${naam.ifBlank { "deze winkel" }} kiest de app dit potje zelf.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
        if (naam.trim().contains(' ')) {
            Tekst("💡 Tip: maak het korter (bv. alleen \"${naam.trim().substringBefore(' ')}\"), dan werkt het voor elke winkel met die naam.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }

        GroeneKnop(if (pot == null) "Kies eerst een potje" else "✓ Bewaar", onClick = {
            val p = pot ?: return@GroeneKnop onMelding("Kies een potje")
            acties.wijzigGeld { IngImport.kies(it, w.id, naam, p, cat) }
            onMelding("✓ Bewaard. ${naam.trim().ifEmpty { "Deze winkel" }} gaat voortaan vanzelf.")
        })
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(CircleShape)
                .clickable(onClickLabel = "Niet boeken") {
                    acties.wijzigGeld { IngImport.slaOver(it, w.id) }
                    onMelding("Overgeslagen")
                },
            contentAlignment = Alignment.Center,
        ) { Tekst("Overslaan (niet boeken)", 15.sp, dikte = FontWeight.ExtraBold, kleur = Grijs) }
    }
}

/** Paneel "🏦 ING-import": aan/uit, de winkels die de app kent, en meldingen die hij niet snapte. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IngPaneel(data: GeldData, acties: AppActies, onMelding: (String) -> Unit) {
    val aan = acties.ingToegang()
    var hulp by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf<String?>(null) }
    var proef by remember { mutableStateOf("") }

    Paneel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tekst(if (aan) "✅" else "⚠️", 26.sp)
            Column(Modifier.weight(1f)) {
                Tekst(if (aan) "ING-meldingen lezen staat aan" else "ING-meldingen lezen staat uit", 17.sp, kleur = Bruin, regel = 21.sp)
                Tekst(
                    if (aan) "Betaal je met ING, dan komt de uitgave vanzelf in het goede potje."
                    else "Zet het één keer aan. Dan komen ING-uitgaven vanzelf in Geld.",
                    14.sp, dikte = FontWeight.Bold, kleur = Grijs,
                )
            }
        }
        if (!aan) {
            GroeneKnop("🔓 Aanzetten", onClick = {
                hulp = true
                if (!acties.openIngToegang()) onMelding("Dat scherm kon ik niet openen.")
            })
        }
        WitteRegel(onClick = { hulp = !hulp }, uitleg = "Uitleg") {
            Tekst("❓", 18.sp)
            Tekst("Hoe zet ik het aan?", 15.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Bruin)
            Tekst(if (hulp) "▲" else "▼", 14.sp, kleur = Grijs)
        }
        if (hulp) {
            Stap("1", "Tik op Aanzetten. Zet Mijn dashboard aan bij Toegang tot meldingen.")
            Stap("2", "Is het knopje grijs? Dan doet Android moeilijk, omdat de app niet uit de Play Store komt. Tik op App-info, dan ⋮ rechtsboven, dan Beperkte instellingen toestaan.")
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .klei(Color.White, CircleShape, Kleur.KaartSchaduw, hoogte = 4.dp)
                    .clip(CircleShape)
                    .clickable { if (!acties.openIngToegang(appInfo = true)) onMelding("App-info kon ik niet openen.") },
                contentAlignment = Alignment.Center,
            ) { Tekst("ℹ️ App-info openen", 15.sp, kleur = Bruin) }
            Stap("3", "Daarna weer stap 1.")
            Stap("🔒", "De app leest alleen meldingen van de ING-app. Andere meldingen worden niet gelezen of bewaard.")
        }

        GeldKopje("🏪 Winkels die ik ken (${data.winkels.size})")
        if (data.winkels.isEmpty()) {
            Tekst("Nog geen. Bij een nieuwe winkel vraagt de app één keer welk potje.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }
        data.winkels.entries.sortedBy { it.value.naam.lowercase() }.forEach { (sleutel, w) ->
            val p = data.potje(w.potjeId)
            WitteRegel(onClick = { open = if (open == sleutel) null else sleutel }, uitleg = "${w.naam} aanpassen") {
                Tekst(w.naam, 16.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Bruin)
                Tekst("→ ${p?.emoji ?: "❓"} ${p?.naam ?: "weg"}", 15.sp, dikte = FontWeight.Bold, kleur = Grijs)
            }
            if (open == sleutel) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.potjes.forEach { pot ->
                        Chip("${pot.emoji} ${pot.naam}", pot.id == w.potjeId, {
                            acties.wijzigGeld { IngImport.zetPot(it, sleutel, pot.id) }
                        })
                    }
                    Chip("🗑️ Vergeten", false, {
                        acties.wijzigGeld { IngImport.vergeet(it, sleutel) }
                        open = null
                        onMelding("${w.naam} vergeten. De volgende keer vraagt de app het weer.")
                    }, rand = Color(0xFFC2453A))
                }
            }
        }

        if (data.ingOnbegrepen.isNotEmpty()) {
            GeldKopje("🤔 Deze snapte ik niet")
            Tekst("Laat ze aan je broer zien, dan kan het lezen beter.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
            data.ingOnbegrepen.forEach { t ->
                WitteRegel {
                    Tekst(t, 14.sp, Modifier.weight(1f), dikte = FontWeight.Bold, kleur = Bruin)
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = "Weghalen") {
                            acties.wijzigGeld { IngImport.vergeetOnbegrepen(it, t) }
                        },
                        contentAlignment = Alignment.Center,
                    ) { Tekst("✕", 18.sp, kleur = Grijs) }
                }
            }
        }

        GeldKopje("🧪 Proberen")
        Tekst("Plak hier de tekst van een ING-melding. De app laat zien wat hij ervan maakt. Er wordt niks geboekt.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
        NaamVeld(proef, { proef = it.take(300) }, "Tekst van een ING-melding", leeg = "bv. € 4,50 betaald bij Jumbo")
        if (proef.isNotBlank()) {
            Tekst(IngImport.proef(data, null, proef), 15.sp, dikte = FontWeight.ExtraBold, kleur = Bruin)
        }
    }
}

@Composable
private fun Stap(nr: String, tekst: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(30.dp).klei(IngLicht, CircleShape, Kleur.KaartSchaduw, hoogte = 2.dp), contentAlignment = Alignment.Center) {
            Tekst(nr, 14.sp, kleur = Bruin)
        }
        Tekst(tekst, 15.sp, Modifier.weight(1f), dikte = FontWeight.Bold, kleur = Bruin, regel = 20.sp)
    }
}

@Composable
private fun VoorleesBol(onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .klei(IngLicht, CircleShape, Kleur.KaartSchaduw, hoogte = 3.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Voorlezen" },
        contentAlignment = Alignment.Center,
    ) { Tekst("🔊", 20.sp) }
}

/** Wit tekstveld met een rand, voor een winkelnaam of een geplakte melding. */
@Composable
private fun NaamVeld(tekst: String, onWijzig: (String) -> Unit, uitleg: String, leeg: String = "") {
    val vorm = RoundedCornerShape(18.dp)
    BasicTextField(
        value = tekst,
        onValueChange = { onWijzig(it.take(300)) },
        textStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Bruin),
        cursorBrush = SolidColor(Kleur.Koraal),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .klei(Color.White, vorm, Kleur.KaartSchaduw, hoogte = 3.dp)
            .border(2.dp, Rand, vorm)
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .semantics { contentDescription = uitleg },
        decorationBox = { veld ->
            Box {
                if (tekst.isEmpty() && leeg.isNotEmpty()) Tekst(leeg, 17.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFFD8C4B6))
                veld()
            }
        },
    )
}

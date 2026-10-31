package nl.odelia.dashboard.ui.instellingen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import nl.odelia.dashboard.logica.InstellingenData
import nl.odelia.dashboard.logica.TegelSoort
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.geld.SyncKaart
import nl.odelia.dashboard.ui.menu.MenuIcoon
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Schakelaar
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.TijdKiezer
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

private enum class InstTab(val label: String) { Instellingen("⚙️ Instellingen"), Backup("💾 Back-up") }

/** Instellingen en back-up, met twee tabjes. */
@Composable
fun InstellingenScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(InstTab.Instellingen) }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TerugBalk(Plek.Instellingen.label, onTerug)
            Tabjes(tab) { tab = it }
            when (tab) {
                InstTab.Instellingen -> {
                    SyncKaart(staat.sync, acties)
                    InstellingenTab(staat.instellingen, acties::wijzigInstellingen)
                }
                InstTab.Backup -> BackupTab(acties, onMelding)
            }
        }
    }
}

@Composable
private fun Tabjes(gekozen: InstTab, onKies: (InstTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Kleur.Kaart.copy(alpha = 0.7f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InstTab.entries.forEach { t ->
            val aan = t == gekozen
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .then(
                        if (aan) Modifier
                            .shadow(6.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                            .background(Kleur.Kaart, CircleShape)
                        else Modifier,
                    )
                    .clip(CircleShape)
                    .selectable(selected = aan, onClick = { onKies(t) }, role = Role.Tab),
                contentAlignment = Alignment.Center,
            ) {
                Tekst(t.label, 17.sp, kleur = if (aan) Kleur.Inkt else Kleur.InktZacht, eenRegel = true)
            }
        }
    }
}

// ---------- Instellingen ----------

@Composable
private fun InstellingenTab(ins: InstellingenData, wijzig: ((InstellingenData) -> InstellingenData) -> Unit) {
    var kiesTijd by remember { mutableStateOf<TijdVraag?>(null) }

    Kaart("🔠 Tekst") {
        AanUit("Grote tekst", "Alle letters groter (alleen op dit apparaat)", ins.groteTekst) { wijzig { it.copy(groteTekst = !it.groteTekst) } }
    }

    Kaart("⏰ Herinneringen") {
        AanUit("In- en uitklokken", "Seintje op werkdagen", ins.klokHerinnering) { wijzig { it.copy(klokHerinnering = !it.klokHerinnering) } }
        if (ins.klokHerinnering) {
            TijdRegel("Inklokken om", ins.inklokken) { kiesTijd = TijdVraag("Inklokken om", ins.inklokken) { t -> wijzig { it.copy(inklokken = t) } } }
            TijdRegel("Uitklokken om", ins.uitklokken) { kiesTijd = TijdVraag("Uitklokken om", ins.uitklokken) { t -> wijzig { it.copy(uitklokken = t) } } }
        }
        AanUit("Ochtendmelding", "20 min na inklokken: wat staat er vandaag", ins.ochtendMelding) { wijzig { it.copy(ochtendMelding = !it.ochtendMelding) } }
        AanUit("Urenmail-seintje", "Vrijdag na uitklokken", ins.urenmailSeintje) { wijzig { it.copy(urenmailSeintje = !it.urenmailSeintje) } }
        AanUit("Seintje voor afspraken", "De dag ervoor", ins.deadlineSeintje) { wijzig { it.copy(deadlineSeintje = !it.deadlineSeintje) } }
        if (ins.deadlineSeintje) {
            TijdRegel("Seintje om", ins.seintjeOm) { kiesTijd = TijdVraag("Seintje om", ins.seintjeOm) { t -> wijzig { it.copy(seintjeOm = t) } } }
        }
    }

    Kaart("📊 Google Sheet") {
        AanUit(
            "Uren naar de spreadsheet",
            "Na uitklokken: begin, eind en pauze in \"Cargill_Registratie Werktijden\". Alleen lege dagen.",
            ins.urenNaarSheet,
        ) { wijzig { it.copy(urenNaarSheet = !it.urenNaarSheet) } }
    }

    Kaart("🎯 Weekdoel") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ZachteKnop("−", onClick = { wijzig { it.copy(weekdoel = InstellingenData.weekdoelStap(it.weekdoel, -1)) } },
                modifier = Modifier.semantics { contentDescription = "Uur minder" })
            Tekst("${ins.weekdoel.toInt()} uur", 26.sp, modifier = Modifier.weight(1f))
            ZachteKnop("+", onClick = { wijzig { it.copy(weekdoel = InstellingenData.weekdoelStap(it.weekdoel, 1)) } },
                modifier = Modifier.semantics { contentDescription = "Uur meer" })
        }
    }

    Kaart("🧩 Tegels op Thuis") {
        Tekst("Aan/uit, en ⬆️⬇️ voor de volgorde. Alleen op dit apparaat.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        val zichtbaar = ins.tegels
        val verborgen = TegelSoort.entries.filterNot { it in zichtbaar }
        (zichtbaar + verborgen).forEach { t ->
            val aan = t in zichtbaar
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .toggleable(aan, role = Role.Switch) { wijzig { it.wisselTegel(t) } },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Schakelaar(aan)
                    Tekst(t.naam, 18.sp, kleur = if (aan) Kleur.Inkt else Kleur.InktGrijs)
                }
                if (aan) {
                    Pijl("⬆️", "${t.naam} omhoog", zichtbaar.first() != t) { wijzig { it.schuifTegel(t, -1) } }
                    Pijl("⬇️", "${t.naam} omlaag", zichtbaar.last() != t) { wijzig { it.schuifTegel(t, 1) } }
                }
            }
        }
    }

    Kaart("📋 Menu") {
        Tekst("Zet uit wat je niet gebruikt. Thuis en Instellingen blijven altijd.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        Plek.entries.filterNot { it in Plek.altijd }.forEach { p ->
            val aan = p.name !in ins.verborgenMenu
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .toggleable(aan, role = Role.Switch) { wijzig { it.wisselMenu(p.name) } },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Schakelaar(aan)
                MenuIcoon(p, 30.dp)
                Tekst(p.label, 18.sp, kleur = if (aan) Kleur.Inkt else Kleur.InktGrijs)
            }
        }
    }

    kiesTijd?.let { v ->
        TijdKiezer(v.titel, v.begin, onKies = { v.klaar(it); kiesTijd = null }, onSluit = { kiesTijd = null })
    }
}

private class TijdVraag(val titel: String, val begin: LocalTime, val klaar: (LocalTime) -> Unit)

@Composable
private fun AanUit(titel: String, uitleg: String, aan: Boolean, onWissel: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .toggleable(aan, role = Role.Switch) { onWissel() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Tekst(titel, 18.sp)
            Tekst(uitleg, 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        }
        Schakelaar(aan)
    }
}

@Composable
private fun TijdRegel(titel: String, tijd: LocalTime, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tekst("🕒 $titel", 17.sp, dikte = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Tekst("%d:%02d".format(tijd.hour, tijd.minute), 20.sp, kleur = Kleur.Koraal)
    }
}

@Composable
private fun Pijl(teken: String, uitleg: String, kan: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (kan) Kleur.Kaart else Color.Transparent)
            .clickable(enabled = kan, onClick = onClick)
            .semantics { contentDescription = uitleg },
        contentAlignment = Alignment.Center,
    ) {
        if (kan) Tekst(teken, 20.sp)
    }
}

// ---------- Back-up ----------

@Composable
private fun BackupTab(acties: AppActies, onMelding: (String) -> Unit) {
    var zekerTerug by remember { mutableStateOf(false) }

    Kaart("💾 Back-up", "Alles uit de app in één bestand. Bewaar het in Downloads of Google Drive.") {
        GroteKnop("Back-up maken", onClick = {
            if (!acties.maakBackup(onMelding)) onMelding("Opslaan lukt hier niet.")
        })
    }

    Kaart(
        "♻️ Terugzetten",
        if (zekerTerug) "Let op: alles wat nu in de app staat, wordt vervangen. Daarna start de app opnieuw."
        else "Een oude back-up terugzetten.",
    ) {
        if (!zekerTerug) {
            GroteKnop("Back-up terugzetten", onClick = { zekerTerug = true })
        } else {
            GroteKnop("Ja, kies het bestand", onClick = {
                zekerTerug = false
                if (!acties.zetBackupTerug(onMelding)) onMelding("Bestand kiezen lukt hier niet.")
            })
            GroteKnop("Nee, toch niet", onClick = { zekerTerug = false })
        }
    }

    Kaart(
        "🧭 Uit Kompas halen",
        "Kies je kompas-backup-….json. Open taken en ideeën gaan naar Onthoud (groep Kompas), wensen naar Wensen. Twee keer inlezen geeft geen dubbele.",
    ) {
        GroteKnop("Kompas-bestand kiezen", onClick = {
            if (!acties.leesKompas(onMelding)) onMelding("Bestand kiezen lukt hier niet.")
        })
    }
}

@Composable
private fun Kaart(titel: String, uitleg: String? = null, inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tekst(titel, 22.sp)
        if (uitleg != null) Tekst(uitleg, 17.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        inhoud()
    }
}

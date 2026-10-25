package nl.odelia.dashboard.ui.wachtwoorden

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import kotlinx.coroutines.delay
import nl.odelia.dashboard.logica.Inlog
import nl.odelia.dashboard.logica.WachtwoordGroep
import nl.odelia.dashboard.logica.Wachtwoorden
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Kopje
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.rondVerloop
import nl.odelia.dashboard.ui.theme.Kleur

private val Bruin = Color(0xFF5A3426)
private val Grijs = Color(0xFF8A6A5A)
private val TealLicht = Color(0xFF9AD1D4)
private val Teal = Color(0xFF5FA8AD)

private val vinger = listOf(
    Vorm(
        "M12 4a7 7 0 0 0-7 7v2M19 11a7 7 0 0 0-3-5.7M12 7.5a3.5 3.5 0 0 0-3.5 3.5v3c0 2 .6 3.8 1.6 5.3M15.5 11v2.5c0 2.6-.8 5-2.2 7M12 11v3c0 2.6-.9 5-2.5 6.8M19 14c0 2.3-.6 4.5-1.6 6.3",
        lijn = Color.White, dikte = 1.7f,
    ),
)

private fun groepKleur(g: WachtwoordGroep) = when (g) {
    WachtwoordGroep.School -> Color(0xFFFBE0E8)
    WachtwoordGroep.Werk -> Color(0xFFE3EDDF)
    WachtwoordGroep.Thuis -> Color(0xFFFBEBC4)
}

/**
 * Wachtwoorden (ontwerp/Wachtwoorden.dc.html en WachtwoordenOpen.dc.html).
 * Op slot tot je ontgrendelt met de vingerafdruk, het patroon of de pincode van de telefoon.
 * Na 3 minuten niks doen, of als je het scherm verlaat, gaat het weer op slot.
 * Geen schermafdrukken zolang dit scherm open is.
 */
@Composable
fun WachtwoordenScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val kluis = staat.kluis
    var laatste by remember { mutableStateOf(Instant.now()) }
    fun bezig() {
        laatste = Instant.now()
    }

    DisposableEffect(Unit) {
        acties.geheimScherm(true)
        onDispose {
            acties.opSlot()
            acties.geheimScherm(false)
        }
    }
    LaunchedEffect(kluis.open) {
        if (!kluis.open) return@LaunchedEffect
        laatste = Instant.now()
        while (true) {
            delay(10_000)
            if (Wachtwoorden.moetOpSlot(laatste, Instant.now())) {
                acties.opSlot()
                onMelding("Weer op slot 🔒")
                break
            }
        }
    }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk("Wachtwoorden", onTerug) {
                Box(
                    Modifier
                        .size(52.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Teal, spotColor = Teal)
                        .clip(CircleShape)
                        .background(hoekVerloop(TealLicht, Teal))
                        .clickable { if (kluis.open) acties.opSlot() else acties.ontgrendel() }
                        .semantics { contentDescription = if (kluis.open) "Op slot doen" else "Ontgrendelen" },
                    contentAlignment = Alignment.Center,
                ) { Tekst(if (kluis.open) "🔓" else "🔒", 24.sp) }
            }
            if (kluis.open) {
                Open(kluis.data.inlogs.isEmpty(), staat, acties, onMelding, ::bezig)
            } else {
                OpSlot(kluis.fout, onOntgrendel = acties::ontgrendel)
            }
        }
    }
}

@Composable
private fun OpSlot(fout: String?, onOntgrendel: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(horizontal = 20.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .size(84.dp)
                .shadow(10.dp, RoundedCornerShape(28.dp), ambientColor = Teal, spotColor = Teal)
                .background(hoekVerloop(TealLicht, Teal), RoundedCornerShape(28.dp))
                .glans(RoundedCornerShape(28.dp), licht = 0.45f, donker = 0f),
            contentAlignment = Alignment.Center,
        ) { Tekst("🔒", 42.sp) }
        Tekst("Op slot", 21.sp, kleur = Bruin)
        Box(
            Modifier
                .size(130.dp)
                .shadow(12.dp, CircleShape, ambientColor = Teal, spotColor = Teal)
                .clip(CircleShape)
                .background(rondVerloop(0f to Color(0xFFC9ECEE), 1f to Teal, x = 0.4f, y = 0.35f))
                .clickable(onClick = onOntgrendel)
                .semantics { contentDescription = "Ontgrendelen met vingerafdruk, patroon of pincode" },
            contentAlignment = Alignment.Center,
        ) { Icoon(vinger, 72.dp) }
        Tekst("Tik en leg je vinger op de sensor", 16.sp, dikte = FontWeight.ExtraBold, kleur = Grijs, uitlijning = TextAlign.Center)
        Tekst("Of gebruik je patroon of pincode van de telefoon.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs, uitlijning = TextAlign.Center)
        if (fout != null) Tekst(fout, 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.KoraalDiep, uitlijning = TextAlign.Center)
    }
}

@Composable
private fun Open(leeg: Boolean, staat: AppStaat, acties: AppActies, onMelding: (String) -> Unit, bezig: () -> Unit) {
    val data = staat.kluis.data
    var groep by remember { mutableStateOf(WachtwoordGroep.School) }
    var zoek by remember { mutableStateOf("") }
    var nieuw by remember { mutableStateOf(false) }
    val zichtbaar = remember { mutableStateMapOf<String, Boolean>() }

    Invoer(zoek, { zoek = it.take(60); bezig() }, hint = "Zoek een website of app...") {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Icoon(Icoontjes.zoek, 22.dp) }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(Kleur.Kaart.copy(alpha = 0.7f), CircleShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WachtwoordGroep.entries.forEach { g ->
            val aan = g == groep && zoek.isBlank()
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(if (aan) hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal) else hoekVerloop(Color.Transparent, Color.Transparent))
                    .selectable(selected = aan, role = Role.Tab, onClick = { groep = g; zoek = ""; bezig() }),
                contentAlignment = Alignment.Center,
            ) { Tekst("${g.emoji} ${g.label}", 15.sp, kleur = if (aan) Color.White else Color(0xFF7A4A38), eenRegel = true) }
        }
    }

    val lijst = Wachtwoorden.zoek(data, groep, zoek)
    if (lijst.isEmpty()) {
        WKaart {
            Tekst(
                when {
                    leeg -> "Nog geen wachtwoorden. Tik op \"+ Nieuw wachtwoord\"."
                    zoek.isNotBlank() -> "Niks gevonden."
                    else -> "Nog niks bij ${groep.label}."
                },
                16.sp, dikte = FontWeight.ExtraBold, kleur = Grijs,
            )
        }
    }
    lijst.forEach { i ->
        key(i.id) {
            InlogKaart(
                i,
                zichtbaar = zichtbaar[i.id] == true,
                onOog = { zichtbaar[i.id] = zichtbaar[i.id] != true; bezig() },
                onKopieer = {
                    bezig()
                    onMelding(if (acties.kopieerGeheim(i.wachtwoord)) "Gekopieerd. Over 1 minuut weer weg." else "Kopiëren lukte niet.")
                },
                onWis = { acties.wijzigWachtwoorden { Wachtwoorden.wis(it, i.id) }; onMelding("Gewist") },
            )
        }
    }

    if (nieuw) {
        NieuwKaart(groep, onBewaar = { inlog ->
            acties.wijzigWachtwoorden { Wachtwoorden.bewaar(it, inlog) }
            groep = inlog.groep
            zoek = ""
            nieuw = false
            onMelding("Bewaard 🔒")
        }, onStop = { nieuw = false }, bezig = bezig)
    } else {
        GroteKnop("Nieuw wachtwoord", { nieuw = true; bezig() })
    }
    Tekst(
        "Alleen op deze telefoon, versleuteld. Gaat nooit online.",
        13.sp, Modifier.fillMaxWidth(), dikte = FontWeight.Bold, kleur = Grijs, uitlijning = TextAlign.Center,
    )
}

@Composable
private fun InlogKaart(i: Inlog, zichtbaar: Boolean, onOog: () -> Unit, onKopieer: () -> Unit, onWis: () -> Unit) {
    var zeker by remember { mutableStateOf(false) }
    LaunchedEffect(zeker) {
        if (zeker) {
            delay(3_000)
            zeker = false
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(26.dp), Kleur.KaartSchaduw, hoogte = 8.dp)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(48.dp).background(groepKleur(i.groep), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Tekst(i.emoji, 24.sp)
            }
            Column(Modifier.weight(1f)) {
                Tekst(i.site, 18.sp, kleur = Bruin)
                if (i.gebruiker.isNotBlank()) Tekst(i.gebruiker, 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { if (zeker) onWis() else zeker = true }
                    .semantics { contentDescription = "Wissen" },
                contentAlignment = Alignment.Center,
            ) {
                if (zeker) Tekst("Zeker?", 12.sp, kleur = Kleur.KoraalDiep) else Icoon(Icoontjes.prullenbak, 22.dp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) { Tekst(if (zichtbaar) i.wachtwoord else Wachtwoorden.VERBORGEN, 18.sp, kleur = Kleur.Inkt) }
            KleinKnop(if (zichtbaar) "🙈" else "👁️", if (zichtbaar) "Verbergen" else "Laten zien", Color.White, onOog)
            KleinKnop("📋", "Kopiëren", null, onKopieer)
        }
    }
}

@Composable
private fun KleinKnop(emoji: String, uitleg: String, kleur: Color?, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .size(48.dp)
            .shadow(3.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .clip(vorm)
            .background(if (kleur != null) hoekVerloop(kleur, kleur) else hoekVerloop(TealLicht, Teal))
            .clickable(onClick = onClick)
            .semantics { contentDescription = uitleg },
        contentAlignment = Alignment.Center,
    ) { Tekst(emoji, 20.sp) }
}

@Composable
private fun NieuwKaart(begin: WachtwoordGroep, onBewaar: (Inlog) -> Unit, onStop: () -> Unit, bezig: () -> Unit) {
    var groep by remember { mutableStateOf(begin) }
    var site by remember { mutableStateOf("") }
    var gebruiker by remember { mutableStateOf("") }
    var wachtwoord by remember { mutableStateOf("") }
    var fout by remember { mutableStateOf(false) }
    WKaart {
        Tekst("➕ Nieuw wachtwoord", 20.sp, kleur = Bruin)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WachtwoordGroep.entries.forEach { g ->
                Keuze("${g.emoji} ${g.label}", g == groep, { groep = g; bezig() }, Modifier.weight(1f))
            }
        }
        Kopje("🌐 Website of app")
        Invoer(site, { site = it.take(80); bezig() }, hint = "Bijv. Schoolportaal")
        Kopje("👤 Gebruikersnaam")
        Invoer(gebruiker, { gebruiker = it.take(120); bezig() }, hint = "Mail of naam")
        Kopje("🔑 Wachtwoord")
        Invoer(wachtwoord, { wachtwoord = it.take(200); bezig() }, hint = "Wachtwoord")
        if (fout) Tekst("Vul een website en een wachtwoord in.", 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.KoraalDiep)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ZachteKnop("Terug", onStop, Modifier.weight(1f))
            GroteKnop("Bewaar", {
                if (site.isBlank() || wachtwoord.isEmpty()) fout = true
                else onBewaar(Inlog.nieuw(groep, site, gebruiker, wachtwoord))
            }, Modifier.weight(1f), icoon = Icoontjes.vinkje)
        }
    }
}

@Composable
private fun WKaart(inhoud: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = inhoud,
    )
}

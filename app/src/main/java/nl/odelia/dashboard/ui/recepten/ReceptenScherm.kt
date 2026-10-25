package nl.odelia.dashboard.ui.recepten

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.Recept
import nl.odelia.dashboard.logica.ReceptSoort
import nl.odelia.dashboard.logica.Recepten
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Kopje
import nl.odelia.dashboard.ui.onderdelen.MicKnop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

private val Bruin = Color(0xFF5A3426)
private val Grijs = Color(0xFF8A6A5A)
private val Groen = Color(0xFF6FAE84)

private fun soortKleur(s: ReceptSoort) = when (s) {
    ReceptSoort.Avond -> Color(0xFFFDE6DA)
    ReceptSoort.Lunch -> Color(0xFFE3EDDF)
    ReceptSoort.Bakken -> Color(0xFFFBE0E8)
    ReceptSoort.Idee -> Color(0xFFEFE7FB)
}

/**
 * Recepten & ideeën (ontwerp/Recepten.dc.html). Begint met de recepten uit het ontwerp;
 * zelf recepten en ideeën toevoegen kan ook. Bewaard in recepten.json op de telefoon.
 * Geen AI: recepten laten bedenken komt later (met een backend).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReceptenScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
    begin: String? = null,
) {
    val alle = staat.recepten.recepten
    var soort by remember { mutableStateOf<ReceptSoort?>(null) }
    var zoek by remember { mutableStateOf("") }
    var gekozen by remember { mutableStateOf(begin ?: alle.firstOrNull()?.id) }
    var maken by remember { mutableStateOf<Maken?>(null) }
    val klembord = LocalClipboardManager.current
    val lijst = Recepten.zoek(staat.recepten, soort, zoek)
    val huidig = alle.firstOrNull { it.id == gekozen }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk("Recepten", onTerug) {
                Box(
                    Modifier
                        .size(52.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Color(0xFFE5B54E), spotColor = Color(0xFFE5B54E))
                        .background(hoekVerloop(Color(0xFFF7DA8A), Color(0xFFE5B54E)), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Tekst("🍳", 24.sp) }
            }

            Invoer(zoek, { zoek = it.take(60) }, hint = "Zoek: pasta, kip, snel...") {
                MicKnop({ if (!acties.inspreken { zoek = it.take(60) }) onMelding("Inspreken lukt niet op deze telefoon.") }, maat = 44.dp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GroteKnop("Recept", { maken = Maken() }, Modifier.weight(1f))
                ZachteKnop("📋 Link plakken", {
                    val tekst = klembord.getText()?.text.orEmpty()
                    val link = Recepten.linkUit(tekst)
                    if (link == null) onMelding("Kopieer eerst een link (https://...).")
                    else maken = Maken(soort = ReceptSoort.Idee, link = link)
                }, Modifier.weight(1f))
            }

            maken?.let { m ->
                MaakKaart(m, acties, onMelding, onKlaar = { r ->
                    if (r != null) {
                        acties.wijzigRecepten { Recepten.bewaar(it, r) }
                        gekozen = r.id
                        soort = null
                        zoek = ""
                        onMelding("Bewaard ✓")
                    }
                    maken = null
                })
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (listOf<ReceptSoort?>(null) + ReceptSoort.entries).forEach { s ->
                    val aan = s == soort
                    Box(
                        Modifier
                            .heightIn(min = 48.dp)
                            .shadow(if (aan) 6.dp else 3.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                            .clip(CircleShape)
                            .background(if (aan) hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal) else hoekVerloop(Kleur.Kaart, Kleur.Kaart))
                            .selectable(selected = aan, role = Role.Tab, onClick = { soort = s })
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Tekst(s?.let { "${it.emoji} ${it.label}" } ?: "Alles", 15.sp, kleur = if (aan) Color.White else Color(0xFF7A4A38), eenRegel = true)
                    }
                }
            }

            if (lijst.isEmpty()) Tekst("Niks gevonden.", 16.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
            lijst.chunked(2).forEach { rij ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rij.forEach { r -> ReceptTegel(r, r.id == gekozen, Modifier.weight(1f)) { gekozen = r.id } }
                    if (rij.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            if (huidig != null) {
                ReceptOpen(huidig, acties, onMelding, onBewerk = { maken = Maken.van(huidig) }, onWis = {
                    acties.wijzigRecepten { Recepten.wis(it, huidig.id) }
                    gekozen = null
                    onMelding("Gewist")
                })
            }

            Tekst("💡 Recepten laten bedenken (AI) komt later.", 13.sp, Modifier.fillMaxWidth(), dikte = FontWeight.Bold, kleur = Grijs)
        }
    }
}

@Composable
private fun ReceptTegel(r: Recept, aan: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(26.dp)
    Column(
        modifier
            .klei(soortKleur(r.soort), vorm, Kleur.KaartSchaduw, hoogte = 8.dp)
            .border(3.dp, if (aan) Kleur.Koraal else Color.Transparent, vorm)
            .clip(vorm)
            .selectable(selected = aan, onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(58.dp).background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Tekst(r.emoji, 32.sp)
            }
            Tekst(
                if (r.isIdee) "💡 idee" else "✓ recept", 12.sp,
                Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape).padding(horizontal = 8.dp, vertical = 4.dp),
                kleur = if (r.isIdee) Color(0xFF6A4BC4) else Color(0xFF3F7A58), eenRegel = true,
            )
        }
        Tekst(r.naam, 16.sp, kleur = Bruin, regel = 19.sp)
        if (r.tijd.isNotBlank()) Tekst("⏱ ${r.tijd}", 13.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
    }
}

@Composable
private fun ReceptOpen(r: Recept, acties: AppActies, onMelding: (String) -> Unit, onBewerk: () -> Unit, onWis: () -> Unit) {
    var stap by remember(r.id) { mutableStateOf(0) }
    var heb by remember(r.id) { mutableStateOf(emptySet<String>()) }
    var zeker by remember(r.id) { mutableStateOf(false) }
    LaunchedEffect(zeker) {
        if (zeker) {
            delay(3_000)
            zeker = false
        }
    }
    val n = r.stappen.size
    val nu = stap.coerceIn(0, (n - 1).coerceAtLeast(0))

    Column(
        Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(30.dp), ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .background(Color.White, RoundedCornerShape(30.dp))
            .border(3.dp, Color(0xFFF7DA8A), RoundedCornerShape(30.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(64.dp).background(soortKleur(r.soort), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Tekst(r.emoji, 36.sp)
            }
            Column(Modifier.weight(1f)) {
                Tekst(r.naam, 21.sp, kleur = Bruin, regel = 24.sp)
                val onder = listOf(r.tijd.takeIf { it.isNotBlank() }?.let { "⏱ $it" }, r.personen.takeIf { it.isNotBlank() }).filterNotNull()
                if (onder.isNotEmpty()) Tekst(onder.joinToString(" · "), 14.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal))
                    .clickable {
                        if (!acties.voorlezen(Recepten.voorlezen(r, nu))) onMelding("Voorlezen lukt nog niet. Staat er een Nederlandse stem op je telefoon?")
                    }
                    .semantics { contentDescription = "Voorlezen" },
                contentAlignment = Alignment.Center,
            ) { Tekst("🔊", 20.sp) }
        }

        if (r.ingredienten.isNotEmpty()) {
            Tekst("🧺 Wat heb je nodig?", 17.sp, kleur = Bruin)
            r.ingredienten.forEach { x ->
                val aan = x in heb
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Kleur.Kaart)
                        .toggleable(aan, role = Role.Checkbox) { heb = if (it) heb + x else heb - x }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(26.dp)
                            .background(if (aan) Groen else Color.White, RoundedCornerShape(8.dp))
                            .border(3.dp, if (aan) Groen else Color(0xFFE3C3B0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) { if (aan) Icoon(Icoontjes.vinkje, 18.dp) }
                    Tekst(x, 16.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt)
                }
            }
            val vorm = CircleShape
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(vorm)
                    .border(2.dp, Groen, vorm)
                    .clickable {
                        val mis = Recepten.mis(r, heb)
                        if (mis.isEmpty()) {
                            onMelding("Je hebt alles al 🎉")
                        } else {
                            acties.bewaar(Onthoud(tekst = Recepten.boodschappen(r, mis), wat = "Boodschappen", groep = "Boodschappen", seintje = false))
                            onMelding("Op je lijstje gezet (Onthoud) ✓")
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) { Tekst("🛒 Wat ik mis op mijn boodschappenlijst", 15.sp, kleur = Color(0xFF3F7A58)) }
        }

        if (n > 0) {
            val s = r.stappen[nu]
            Tekst("👩‍🍳 Stap ${nu + 1} van $n", 17.sp, kleur = Bruin)
            Row(
                Modifier.fillMaxWidth().background(Color(0xFFFBEBC4), RoundedCornerShape(20.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Tekst(s.emoji, 34.sp)
                Tekst(s.tekst, 18.sp, Modifier.weight(1f), regel = 23.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .width(56.dp)
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Kleur.Kaart)
                        .clickable(enabled = nu > 0) { stap = nu - 1 }
                        .semantics { contentDescription = "Vorige stap" },
                    contentAlignment = Alignment.Center,
                ) { Tekst("‹", 22.sp, kleur = Color(0xFF7A4A38).copy(alpha = if (nu == 0) 0.4f else 1f)) }
                val laatst = nu >= n - 1
                Box(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(hoekVerloop(Color(0xFFA8D6B4), Groen))
                        .clickable { stap = if (laatst) 0 else nu + 1 },
                    contentAlignment = Alignment.Center,
                ) { Tekst(if (laatst) "🎉 Klaar!" else "Volgende stap ›", 18.sp, kleur = Color.White) }
            }
        }

        if (r.link.isNotBlank()) {
            ZachteKnop("🌐 Recept openen", { if (!acties.openLink(r.link)) onMelding("Openen lukte niet.") }, Modifier.fillMaxWidth())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ZachteKnop("✏️ Aanpassen", onBewerk, Modifier.weight(1f))
            ZachteKnop(if (zeker) "Zeker?" else "Wis", { if (zeker) onWis() else zeker = true }, Modifier.weight(1f), kleur = Kleur.KoraalDiep)
        }
    }
}

/** Wat er in het "nieuw recept"-formulier staat. Met [id] = bestaand recept aanpassen. */
private data class Maken(
    val id: String? = null,
    val soort: ReceptSoort = ReceptSoort.Avond,
    val naam: String = "",
    val tijd: String = "",
    val ingredienten: String = "",
    val stappen: String = "",
    val link: String = "",
) {
    companion object {
        fun van(r: Recept) = Maken(
            r.id, r.soort, r.naam, r.tijd,
            r.ingredienten.joinToString("\n"),
            r.stappen.joinToString("\n") { "${it.emoji} ${it.tekst}" },
            r.link,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MaakKaart(begin: Maken, acties: AppActies, onMelding: (String) -> Unit, onKlaar: (Recept?) -> Unit) {
    var m by remember(begin) { mutableStateOf(begin) }
    fun spreekIn(klaar: (String) -> Unit) {
        if (!acties.inspreken(klaar)) onMelding("Inspreken lukt niet op deze telefoon.")
    }
    RKaart {
        Tekst(if (m.id == null) "➕ Nieuw recept of idee" else "✏️ Recept aanpassen", 20.sp, kleur = Bruin)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ReceptSoort.entries.forEach { s -> Keuze("${s.emoji} ${s.label}", s == m.soort, { m = m.copy(soort = s) }) }
        }
        Kopje("🍽️ Naam")
        Invoer(m.naam, { m = m.copy(naam = it.take(80)) }, hint = "Bijv. Pasta pesto") {
            MicKnop({ spreekIn { t -> m = m.copy(naam = t.take(80)) } }, maat = 44.dp)
        }
        Kopje("⏱ Hoe lang?")
        Invoer(m.tijd, { m = m.copy(tijd = it.take(30)) }, hint = "Bijv. 15 min")
        Kopje("🧺 Wat heb je nodig? (1 per regel)")
        Invoer(m.ingredienten, { m = m.copy(ingredienten = it.take(1000)) }, hint = "pasta\npesto", regels = 3)
        Kopje("👩‍🍳 Stappen (1 per regel)")
        Invoer(m.stappen, { m = m.copy(stappen = it.take(3000)) }, hint = "Kook water.\nDoe de pasta erin.", regels = 3) {
            MicKnop({ spreekIn { t -> m = m.copy(stappen = if (m.stappen.isBlank()) t else m.stappen + "\n" + t) } }, maat = 44.dp)
        }
        Kopje("🌐 Link (mag leeg)")
        Invoer(m.link, { m = m.copy(link = it.take(500)) }, hint = "https://...")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ZachteKnop("Terug", { onKlaar(null) }, Modifier.weight(1f))
            GroteKnop("Bewaar", {
                if (m.naam.isBlank()) {
                    onMelding("Geef het recept een naam.")
                } else {
                    val id = m.id
                    onKlaar(
                        if (id == null) Recepten.nieuw(m.soort, m.naam, m.tijd, m.ingredienten, m.stappen, m.link)
                        else Recepten.nieuw(m.soort, m.naam, m.tijd, m.ingredienten, m.stappen, m.link, id = id),
                    )
                }
            }, Modifier.weight(1f), icoon = Icoontjes.vinkje)
        }
    }
}

@Composable
private fun RKaart(inhoud: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = inhoud,
    )
}

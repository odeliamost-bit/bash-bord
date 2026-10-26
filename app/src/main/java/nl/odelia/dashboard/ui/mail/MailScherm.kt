package nl.odelia.dashboard.ui.mail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.MailHulp
import nl.odelia.dashboard.logica.Uren
import nl.odelia.dashboard.logica.Urenmail
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.Bubbel
import nl.odelia.dashboard.ui.onderdelen.EmojiRondje
import nl.odelia.dashboard.ui.onderdelen.KopBalk
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TypBalk
import nl.odelia.dashboard.ui.onderdelen.VolleKnop
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.theme.Kleur

/** Hoe het scherm opent. De extra's zijn voor de voorbeeld-plaatjes. */
data class MailBegin(val onderwerp: String? = null, val toon: String? = null, val eigen: String = "")

private val Paars = Color(0xFF3E2F66)
private val PaarsRand = Color(0xFFEFE3F7)

/**
 * Mail-hulp (ontwerp/Mail.dc.html): kies onderwerp en toon, dan staat er een voorbeeldmail.
 * Vaste voorbeelden (geen AI). De app verstuurt nooit zelf: "In Gmail zetten" opent alleen een concept.
 */
@Composable
fun MailScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
    begin: MailBegin = MailBegin(),
) {
    var onderwerp by remember { mutableStateOf(begin.onderwerp) }
    var toon by remember { mutableStateOf(begin.toon) }
    var korter by remember { mutableStateOf(false) }
    var eigen by remember { mutableStateOf(begin.eigen) }
    var typ by remember { mutableStateOf("") }
    val klembord = LocalClipboardManager.current
    val scroll = rememberScrollState()

    val o = onderwerp
    val t = toon
    val concept = if (o != null && t != null) {
        val uren = if (o == "uren") {
            val maandag = Urenmail.maandag(staat.nu.toLocalDate())
            Urenmail.tekst(Uren.werkweek(staat.perioden, staat.soorten, maandag, staat.nu), eigen)
        } else {
            null
        }
        MailHulp.concept(o, t, korter, eigen, uren)
    } else {
        null
    }

    fun stuur(tekst: String) {
        val schoon = tekst.trim()
        if (schoon.isEmpty()) {
            onMelding("Typ of spreek eerst wat je wilt zeggen.")
            return
        }
        eigen = schoon
        typ = ""
        if (onderwerp == null) onderwerp = "anders"
        if (toon == null) toon = "gewoon"
    }

    fun opnieuw() {
        onderwerp = null
        toon = null
        korter = false
        eigen = ""
        typ = ""
    }

    Box(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().verticalScroll(scroll), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                KopBalk("Mail-hulp", "ik schrijf, jij verstuurt zelf", onTerug) {
                    EmojiRondje("↺", Color(0xFFD2BDF0), Color(0xFFA88AD8), uitleg = "Opnieuw beginnen", onClick = ::opnieuw)
                }

                Bubbel("Hoi! ✉️ Waar gaat je mail over?", vanMij = false)
                Raster(MailHulp.onderwerpen.size) { i, m ->
                    val k = MailHulp.onderwerpen[i]
                    KiesKnop(k.emoji, k.label, onderwerp == k.id, rij = true, modifier = m) {
                        if (onderwerp != k.id) {
                            onderwerp = k.id
                            toon = null
                            korter = false
                            eigen = ""
                        }
                    }
                }

                if (o != null) {
                    val gekozen = MailHulp.onderwerpen.first { it.id == o }
                    Bubbel("${gekozen.emoji} ${gekozen.label}", vanMij = true)
                    Bubbel("Hoe moet het klinken?", vanMij = false)
                    Raster(MailHulp.tonen.size) { i, m ->
                        val k = MailHulp.tonen[i]
                        KiesKnop(k.emoji, k.label, toon == k.id, rij = false, modifier = m) { toon = k.id }
                    }
                }

                if (eigen.isNotEmpty()) Bubbel(eigen, vanMij = true)

                if (concept != null) {
                    val uitleg = MailHulp.isUitleg(concept)
                    if (uitleg) {
                        Bubbel(concept.tekst, vanMij = false)
                    } else {
                        Bubbel("Hier is je mail! 👇", vanMij = false)
                        ConceptKaart(concept)
                        Raster(4) { i, m ->
                            when (i) {
                                0 -> VolleKnop("📋 Kopiëren", onClick = {
                                    klembord.setText(AnnotatedString("${concept.onderwerp}\n\n${concept.tekst}"))
                                    onMelding("Gekopieerd ✓")
                                }, m, hoekVerloop(Color(0xFFA8D6B4), Color(0xFF6FAE84)), Color.White, Color(0xFF6FAE84))
                                1 -> VolleKnop("🔊 Voorlezen", onClick = {
                                    if (!acties.voorlezen("Onderwerp: ${concept.onderwerp}. ${concept.tekst}")) {
                                        onMelding("Voorlezen lukt nog niet. Staat er een Nederlandse stem op je telefoon?")
                                    }
                                }, m, hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal), Color.White, Kleur.Koraal)
                                2 -> VolleKnop(if (korter) "↔️ Langer" else "✂️ Korter", onClick = {
                                    if (o == "uren") onMelding("Deze mail is al kort.") else korter = !korter
                                }, m)
                                else -> VolleKnop("📨 In Gmail zetten", onClick = {
                                    if (!acties.openMail(concept.onderwerp, concept.tekst)) onMelding("Geen mail-app gevonden.")
                                }, m)
                            }
                        }
                        Tekst(
                            "Ik verstuur nooit zelf. Jij kijkt en drukt op versturen.",
                            14.sp, Modifier.fillMaxWidth(), dikte = FontWeight.Bold, kleur = Color(0xFF8A5A48), uitlijning = TextAlign.Center,
                        )
                        if (o == "uren") {
                            Tekst(
                                "Gmail opent een nieuwe mail. Kies zelf Kim. Antwoorden in de draad \"Urenregistratie\" kan nog niet.",
                                14.sp, Modifier.fillMaxWidth(), dikte = FontWeight.Bold, kleur = Kleur.InktGrijs, uitlijning = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        TypBalk(
            waarde = typ,
            onWijzig = { typ = it },
            hint = if (o == "uren") "Opmerking voor Kim..." else "Of typ zelf wat je wilt zeggen...",
            onStuur = { stuur(typ) },
            onInspreken = { if (!acties.inspreken { stuur(it) }) onMelding("Inspreken lukt niet op deze telefoon.") },
            stuurUitleg = "Versturen naar mail-hulp",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 560.dp)
                .padding(start = 4.dp, end = 12.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun ConceptKaart(c: MailHulp.Concept) {
    val vorm = RoundedCornerShape(26.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(12.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .background(Color.White, vorm)
            .border(2.dp, PaarsRand, vorm)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tekst("Onderwerp", 14.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF8A6A5A))
        Tekst(c.onderwerp, 17.sp, kleur = Paars)
        Box(Modifier.fillMaxWidth().height(1.dp).background(PaarsRand))
        Tekst(c.tekst, 16.sp, dikte = FontWeight.SemiBold, kleur = Kleur.Inkt, regel = 23.sp)
    }
}

/** Twee knoppen naast elkaar, zo veel rijen als nodig. */
@Composable
private fun Raster(aantal: Int, knop: @Composable (Int, Modifier) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (0 until aantal).chunked(2).forEach { rij ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rij.forEach { i -> knop(i, Modifier.weight(1f)) }
                if (rij.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

/** Keuzeknop met emoji. [rij] = emoji naast de tekst; anders erboven. */
@Composable
private fun KiesKnop(emoji: String, tekst: String, aan: Boolean, rij: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(20.dp)
    val basis = modifier
        .heightIn(min = if (rij) 56.dp else 70.dp)
        .shadow(6.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
        .clip(vorm)
        .background(Color.White)
        .border(3.dp, if (aan) Kleur.Koraal else Color.Transparent, vorm)
        .selectable(selected = aan, role = Role.RadioButton, onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 8.dp)
    if (rij) {
        Row(basis, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tekst(emoji, 24.sp)
            Tekst(tekst, 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt)
        }
    } else {
        Column(basis, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)) {
            Tekst(emoji, 26.sp)
            Tekst(tekst, 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt)
        }
    }
}

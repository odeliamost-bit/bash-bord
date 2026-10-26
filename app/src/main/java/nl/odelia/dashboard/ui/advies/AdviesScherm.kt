package nl.odelia.dashboard.ui.advies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.Advies
import nl.odelia.dashboard.logica.Links
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.Bubbel
import nl.odelia.dashboard.ui.onderdelen.EmojiRondje
import nl.odelia.dashboard.ui.onderdelen.KopBalk
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TypBalk
import nl.odelia.dashboard.ui.onderdelen.VolleKnop
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.opzoekVraag
import nl.odelia.dashboard.ui.onderdelen.rememberVraag
import nl.odelia.dashboard.ui.theme.Kleur

/** Hoe het scherm opent. De extra's zijn voor de voorbeeld-plaatjes. */
data class AdviesBegin(val uitleg: String = "fietsband", val stap: Int = 0, val vraag: String? = null)

private val Bruin = Color(0xFF5A3426)
private val Oranje = Color(0xFFE9956A)
private val OranjeLicht = Color(0xFFF8C29C)

private fun kleurVan(soort: String): Color = when (soort) {
    "repareren" -> Color(0xFFFDE6DA)
    "maken" -> Color(0xFFFBE0E8)
    "koken" -> Color(0xFFFBEBC4)
    else -> Color(0xFFE3EDDF)
}

/** Wat er na een eigen vraag staat: wat je vroeg en wat de app vond. */
private data class Antwoord(val vraag: String, val gevonden: List<Advies.Uitleg>)

/**
 * Advies (ontwerp/Advies.dc.html): stap-voor-stap uitleg, vorige/volgende, voorlezen en ⭐ bewaren.
 * Vaste uitleg (geen AI). Een eigen vraag zoekt in die uitleg, of opent Google in Chrome (na "ja").
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdviesScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
    begin: AdviesBegin = AdviesBegin(),
) {
    var id by remember { mutableStateOf(begin.uitleg) }
    var stap by remember { mutableStateOf(begin.stap) }
    var typ by remember { mutableStateOf("") }
    var antwoord by remember { mutableStateOf(begin.vraag?.let { Antwoord(it, Advies.zoek(it)) }) }
    val vraag = rememberVraag()
    val u = Advies.van(id) ?: Advies.uitleg.first()
    val n = u.stappen.size
    val s = stap.coerceIn(0, n - 1)
    val laatste = s >= n - 1
    val bewaard = id in staat.advies.favorieten

    fun kies(nieuw: String) {
        id = nieuw
        stap = 0
    }

    fun lees(tekst: String) {
        if (!acties.voorlezen(tekst)) onMelding("Voorlezen lukt nog niet. Staat er een Nederlandse stem op je telefoon?")
    }

    fun googlen(term: String) {
        vraag.stel(opzoekVraag(term) { if (!acties.openLink(Links.zoek(term))) onMelding("Chrome openen lukte niet.") })
    }

    fun stuur(tekst: String) {
        val t = tekst.trim()
        if (t.isEmpty()) {
            onMelding("Typ of spreek eerst je vraag.")
            return
        }
        typ = ""
        val gevonden = Advies.zoek(t)
        antwoord = Antwoord(t, gevonden)
        gevonden.firstOrNull()?.let { kies(it.id) }
    }

    Box(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                KopBalk("Advies", "hoe maak of repareer ik iets?", onTerug) {
                    EmojiRondje("🔧", OranjeLicht, Oranje)
                }

                // Onderwerpen
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Advies.soorten.forEach { k ->
                        SoortKnop(k, u.soort == k.id, Modifier.weight(1f)) { kies(Advies.eersteVan(k.id).id) }
                    }
                }

                // Eigen vraag en wat de app vond
                antwoord?.let { a ->
                    Bubbel(a.vraag, vanMij = true)
                    if (a.gevonden.isEmpty()) {
                        Bubbel("Daar heb ik nog geen uitleg voor. 🤔 Wil je het opzoeken op internet?", vanMij = false)
                        ZachteKnop("🔎 Zoek op Google", onClick = { googlen(a.vraag) }, Modifier.fillMaxWidth())
                    } else {
                        Bubbel("Ik heb dit gevonden. 👇", vanMij = false)
                        a.gevonden.drop(1).forEach { ander ->
                            ZachteKnop("Of: ${ander.vraag}", onClick = { kies(ander.id) }, Modifier.fillMaxWidth())
                        }
                    }
                }

                Bubbel(u.vraag, vanMij = true)
                Bubbel(u.intro, vanMij = false)

                // Dit heb je nodig
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(26.dp), ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                        .background(Kleur.Kaart, RoundedCornerShape(26.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Tekst("🧰 Dit heb je nodig", 17.sp, kleur = Bruin)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        u.nodig.forEach { d ->
                            Tekst(
                                "${d.emoji} ${d.label}", 15.sp,
                                Modifier.background(Color.White, CircleShape).padding(horizontal = 12.dp, vertical = 8.dp),
                                dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt,
                            )
                        }
                    }
                }

                // Stap voor stap
                val vorm = RoundedCornerShape(30.dp)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                        .background(Color.White, vorm)
                        .border(3.dp, OranjeLicht, vorm)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Tekst("stap ${s + 1} van $n", 16.sp, Modifier.weight(1f), kleur = Color(0xFFB0581A))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            repeat(n) { i -> Box(Modifier.size(12.dp).background(if (i <= s) Oranje else Color(0xFFF1E4D6), CircleShape)) }
                        }
                    }
                    val st = u.stappen[s]
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            Modifier.size(74.dp).background(hoekVerloop(Color(0xFFFDE6DA), OranjeLicht), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Tekst(st.emoji, 40.sp) }
                        Tekst(st.tekst, 20.sp, Modifier.weight(1f), kleur = Kleur.Inkt, regel = 25.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VolleKnop(
                            "‹", onClick = { if (s > 0) stap = s - 1 },
                            Modifier.width(56.dp).heightIn(min = 56.dp).alpha(if (s == 0) 0.4f else 1f),
                            vulling = androidx.compose.ui.graphics.SolidColor(Kleur.Kaart), tekstKleur = Color(0xFF7A4A38),
                        )
                        VolleKnop(
                            if (laatste) "🎉 Gelukt!" else "Volgende stap ›",
                            onClick = {
                                if (laatste) {
                                    stap = 0
                                    onMelding("Goed gedaan! 🎉")
                                } else {
                                    stap = s + 1
                                }
                            },
                            Modifier.weight(1f).heightIn(min = 56.dp),
                            vulling = hoekVerloop(Color(0xFFA8D6B4), Color(0xFF6FAE84)), tekstKleur = Color.White, schaduw = Color(0xFF6FAE84),
                        )
                        VolleKnop(
                            "🔊", onClick = { lees("Stap ${s + 1}. ${st.tekst}") },
                            Modifier.width(56.dp).heightIn(min = 56.dp),
                            vulling = hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal), schaduw = Kleur.Koraal,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VolleKnop("📷 Foto sturen", onClick = { onMelding("Foto sturen komt later.") }, Modifier.weight(1f), androidx.compose.ui.graphics.SolidColor(Kleur.Kaart))
                    VolleKnop(
                        if (bewaard) "⭐ Bewaard" else "☆ Bewaren",
                        onClick = {
                            acties.wijzigAdvies { it.wissel(id) }
                            onMelding(if (bewaard) "Niet meer bewaard." else "Bewaard ⭐")
                        },
                        Modifier.weight(1f),
                        androidx.compose.ui.graphics.SolidColor(if (bewaard) Color(0xFFFBEBC4) else Kleur.Kaart),
                    )
                }
                ZachteKnop("🔊 Alles voorlezen", onClick = { lees(Advies.voorleesTekst(u)) }, Modifier.fillMaxWidth())

                // Bewaarde uitleg
                val favorieten = staat.advies.favorieten.mapNotNull { Advies.van(it) }
                if (favorieten.isNotEmpty()) {
                    Tekst("⭐ Bewaard", 17.sp, kleur = Bruin)
                    favorieten.forEach { f -> ZachteKnop(f.vraag, onClick = { kies(f.id) }, Modifier.fillMaxWidth()) }
                }
            }
        }

        TypBalk(
            waarde = typ,
            onWijzig = { typ = it },
            hint = "Vraag het maar...",
            onStuur = { stuur(typ) },
            onInspreken = { if (!acties.inspreken { stuur(it) }) onMelding("Inspreken lukt niet op deze telefoon.") },
            stuurUitleg = "Vraag versturen",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 560.dp)
                .padding(start = 4.dp, end = 12.dp, bottom = 16.dp),
        )
    }
    vraag.Toon()
}

@Composable
private fun SoortKnop(k: Advies.Soort, aan: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(20.dp)
    Column(
        modifier
            .heightIn(min = 80.dp)
            .shadow(6.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .clip(vorm)
            .background(kleurVan(k.id))
            .border(3.dp, if (aan) Kleur.Koraal else Color.Transparent, vorm)
            .selectable(selected = aan, role = Role.Tab, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Tekst(k.emoji, 28.sp)
        Tekst(k.label, 13.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt, eenRegel = true)
    }
}

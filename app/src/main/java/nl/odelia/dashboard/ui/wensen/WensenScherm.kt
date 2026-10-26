package nl.odelia.dashboard.ui.wensen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import nl.odelia.dashboard.logica.Wens
import nl.odelia.dashboard.logica.WensSoort
import nl.odelia.dashboard.logica.Wensen
import nl.odelia.dashboard.logica.maakWens
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
import nl.odelia.dashboard.ui.theme.Kleur

private val Bruin = Color(0xFF5A3426)
private val BruinPijl = Color(0xFF7A4A38)
private val Grijs = Color(0xFF8A6A5A)
private val Zalm = Color(0xFFF7B7AE)
private val ZalmDiep = Color(0xFFE8877A)
private val Groen = Color(0xFF6FAE84)
private val hart = "M20.8 5.6a5.5 5.5 0 0 0-7.8 0L12 6.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 22l8.8-8.6a5.5 5.5 0 0 0 0-7.8z"

/** Kaartkleur per soort, uit het ontwerp. */
fun kaartKleur(s: WensSoort): Color = when (s) {
    WensSoort.Kleding -> Color(0xFFDCE7F2)
    WensSoort.Haken -> Color(0xFFFBE0E8)
    WensSoort.Huis -> Color(0xFFFBEBC4)
    WensSoort.Verzorging -> Color(0xFFE3EDDF)
    WensSoort.Overig -> Color(0xFFEFE7FB)
}

/** Wat er in het venster "Nieuwe wens" staat. [oud] = een bestaande wens aanpassen. */
data class WensVenster(
    val naam: String = "",
    val soort: WensSoort = WensSoort.Overig,
    val links: String = "",
    val oud: Wens? = null,
)

/** Hoe het scherm opent (handig voor de voorbeeld-plaatjes). */
data class WensenBegin(val soort: WensSoort? = null, val venster: WensVenster? = null)

/**
 * Wensen (ontwerp/Wensen.dc.html): een verlanglijst met links, zodat je niet meteen koopt.
 * Links gaan open in Chrome; de app zelf gaat niet online.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WensenScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
    begin: WensenBegin = WensenBegin(),
) {
    val alle = staat.wensen.lijst
    var soort by remember { mutableStateOf(begin.soort) }
    var venster by remember { mutableStateOf(begin.venster) }
    var kiesLink by remember { mutableStateOf<Wens?>(null) }
    val klembord = LocalClipboardManager.current
    val telling = Wensen.tel(alle)
    val lijst = Wensen.sorteer(Wensen.filter(alle, soort))

    fun nu() = System.currentTimeMillis()

    fun uitTekst(tekst: String) {
        val n = Wensen.uitTekst(tekst)
        venster = WensVenster(naam = n.naam.orEmpty(), soort = soort ?: WensSoort.Overig, links = n.links.joinToString("\n"))
    }

    fun open(link: String) {
        if (!acties.openLink(link)) onMelding("Deze link openen lukte niet.")
    }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk("Wensen", onTerug) {
                Box(
                    Modifier
                        .size(52.dp)
                        .shadow(6.dp, CircleShape, ambientColor = ZalmDiep, spotColor = ZalmDiep)
                        .background(hoekVerloop(Zalm, ZalmDiep), CircleShape)
                        .glans(CircleShape, licht = 0.5f, donker = 0f),
                    contentAlignment = Alignment.Center,
                ) { Icoon(listOf(Vorm(hart, vul = Color.White)), 26.dp) }
            }

            // Totaal
            Row(
                Modifier
                    .fillMaxWidth()
                    .klei(hoekVerloop(Zalm, ZalmDiep), RoundedCornerShape(30.dp), ZalmDiep)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Tekst("🛍️", 40.sp)
                Column(Modifier.weight(1f)) {
                    Tekst("mijn verlanglijst", 15.sp, dikte = FontWeight.ExtraBold, kleur = Color.White)
                    Tekst("${telling.open} wensen", 28.sp, kleur = Color.White, regel = 31.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Tekst("al gekocht", 13.sp, dikte = FontWeight.ExtraBold, kleur = Color.White)
                    Tekst("${telling.gekocht} ✓", 22.sp, kleur = Color.White)
                }
            }

            // Toevoegen
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                GroteKnop("Nieuwe wens", { venster = WensVenster(soort = soort ?: WensSoort.Overig) }, Modifier.weight(1f))
                ZachteKnop("📋 Plakken", {
                    val geplakt = klembord.getText()?.text.orEmpty()
                    if (geplakt.isBlank()) onMelding("Er staat niks om te plakken.") else uitTekst(geplakt)
                }, Modifier.weight(1f).heightIn(min = 60.dp), kleur = Kleur.Inkt)
                Box(
                    Modifier
                        .size(60.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Kleur.Kaart)
                        .clickable {
                            val gelukt = acties.inspreken { gehoord -> uitTekst(gehoord) }
                            if (!gelukt) onMelding("Inspreken lukt niet op deze telefoon.")
                        }
                        .semantics { contentDescription = "Inspreken" },
                    contentAlignment = Alignment.Center,
                ) { Tekst("🎤", 22.sp) }
            }

            // Soorten
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SoortKnop("Alles", soort == null) { soort = null }
                WensSoort.entries.forEach { s -> SoortKnop("${s.emoji} ${s.label}", soort == s) { soort = s } }
            }

            if (lijst.isEmpty()) {
                Tekst("Nog geen wensen hier. Tik op Nieuwe wens.", 17.sp, Modifier.padding(8.dp), dikte = FontWeight.ExtraBold, kleur = Grijs)
            }

            // Lijst: twee naast elkaar
            lijst.chunked(2).forEach { rij ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rij.forEach { w ->
                        WensKaart(
                            w,
                            Modifier.weight(1f),
                            onHartje = { acties.wijzigWensen { it.volgendHartje(w.id, nu()) } },
                            onBekijk = { if (w.links.size == 1) open(w.links.first()) else kiesLink = w },
                            onGekocht = { acties.wijzigWensen { it.wisselGekocht(w.id, nu()) } },
                            onAanpassen = { venster = WensVenster(w.naam, w.soort, w.links.joinToString("\n"), w) },
                        )
                    }
                    if (rij.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Tekst(
                "💡 Twijfel je? Zet het hier eerst neer en wacht een paar dagen.",
                15.sp, Modifier.padding(horizontal = 8.dp), dikte = FontWeight.Bold, kleur = Grijs,
            )
        }
    }

    venster?.let { v ->
        WensBewerken(
            v,
            onSluit = { venster = null },
            onBewaar = { naam, s, links ->
                acties.wijzigWensen { it.bewaar(maakWens(naam, s, links, v.oud, nu())) }
                venster = null
                onMelding(if (v.oud == null) "Wens bewaard" else "Aangepast")
            },
            onWeg = v.oud?.let { oud ->
                {
                    acties.wijzigWensen { it.verwijder(oud.id) }
                    venster = null
                    onMelding("Weggehaald")
                }
            },
        )
    }

    kiesLink?.let { w ->
        Dialog(onDismissRequest = { kiesLink = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Column(
                Modifier
                    .padding(16.dp)
                    .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Tekst(w.naam, 22.sp, kleur = Bruin)
                w.links.forEachIndexed { i, link ->
                    ZachteKnop("🔗 ${Wensen.winkel(link) ?: "Link ${i + 1}"}", { open(link); kiesLink = null }, Modifier.fillMaxWidth(), kleur = BruinPijl)
                }
                ZachteKnop("Terug", { kiesLink = null }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SoortKnop(tekst: String, aan: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .heightIn(min = 48.dp)
            .shadow(if (aan) 6.dp else 3.dp, CircleShape, ambientColor = if (aan) Kleur.Koraal else Kleur.KaartSchaduw, spotColor = if (aan) Kleur.Koraal else Kleur.KaartSchaduw)
            .clip(CircleShape)
            .background(if (aan) hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal) else androidx.compose.ui.graphics.SolidColor(Kleur.Kaart.copy(alpha = 0.85f)))
            .selectable(selected = aan, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 15.sp, kleur = if (aan) Color.White else BruinPijl)
    }
}

@Composable
private fun WensKaart(
    w: Wens,
    modifier: Modifier,
    onHartje: () -> Unit,
    onBekijk: () -> Unit,
    onGekocht: () -> Unit,
    onAanpassen: () -> Unit,
) {
    Column(
        modifier
            .alpha(if (w.gekocht) 0.55f else 1f)
            .klei(kaartKleur(w.soort), RoundedCornerShape(26.dp), Kleur.KaartSchaduw, hoogte = 10.dp)
            .clip(RoundedCornerShape(26.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(58.dp)
                    .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
                    .clickable(onClickLabel = "Aanpassen", onClick = onAanpassen),
                contentAlignment = Alignment.Center,
            ) { Tekst(w.emoji, 30.sp) }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onHartje)
                    .semantics { contentDescription = "${w.hartjes} van 3 hartjes, tik om te veranderen" }
                    .padding(4.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                Tekst("❤️".repeat(w.hartjes) + "🤍".repeat(Wensen.MAX_HARTJES - w.hartjes), 12.sp)
            }
        }
        Column(Modifier.clickable(onClickLabel = "Aanpassen", onClick = onAanpassen)) {
            androidx.compose.material3.Text(
                w.naam,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Bruin,
                lineHeight = 19.sp,
                textDecoration = if (w.gekocht) TextDecoration.LineThrough else null,
            )
            Tekst(w.winkel ?: "${w.soort.emoji} ${w.soort.label}", 13.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }
        if (w.links.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .clickable(onClick = onBekijk),
                contentAlignment = Alignment.Center,
            ) {
                Tekst("🔗 bekijken" + if (w.links.size > 1) " (+${w.links.size - 1})" else "", 14.sp, kleur = BruinPijl)
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clip(CircleShape)
                .background(if (w.gekocht) Groen else Color.White)
                .toggleable(value = w.gekocht, role = Role.Checkbox, onValueChange = { onGekocht() }),
            contentAlignment = Alignment.Center,
        ) {
            Tekst(if (w.gekocht) "✓ gekocht" else "Gekocht?", 14.sp, kleur = if (w.gekocht) Color.White else BruinPijl)
        }
    }
}

/** Venster om een wens te maken of aan te passen. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WensBewerken(
    begin: WensVenster,
    onSluit: () -> Unit,
    onBewaar: (String, WensSoort, String) -> Unit,
    onWeg: (() -> Unit)?,
) {
    var naam by remember(begin) { mutableStateOf(begin.naam) }
    var soort by remember(begin) { mutableStateOf(begin.soort) }
    var links by remember(begin) { mutableStateOf(begin.links) }
    Dialog(onDismissRequest = onSluit, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .padding(16.dp)
                .widthIn(max = 520.dp)
                .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Tekst(if (begin.oud == null) "Nieuwe wens" else "Wens aanpassen", 22.sp, kleur = Bruin)
            Kopje("✨ Wat wil je?")
            Invoer(naam, { naam = it }, hint = "Bijv. Glazen mok")
            Kopje("🗂️ Soort")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WensSoort.entries.forEach { s -> Keuze("${s.emoji} ${s.label}", soort == s, { soort = s }, achter = kaartKleur(s)) }
            }
            Kopje("🔗 Link (mag leeg)")
            Invoer(links, { links = it }, hint = "https://…", regels = 2)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ZachteKnop("Terug", onSluit, Modifier.weight(1f))
                GroteKnop("Bewaar", { if (naam.isNotBlank()) onBewaar(naam, soort, links) }, Modifier.weight(1f), icoon = Icoontjes.vinkje)
            }
            if (onWeg != null) ZachteKnop("🗑️ Weghalen", onWeg, Modifier.fillMaxWidth(), kleur = Kleur.KoraalDiep)
        }
    }
}

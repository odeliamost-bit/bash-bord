package nl.odelia.dashboard.ui.mensen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import nl.odelia.dashboard.logica.Links
import nl.odelia.dashboard.logica.Mensen
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.Persoon
import nl.odelia.dashboard.logica.korteDatum
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.MicKnop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.opzoekVraag
import nl.odelia.dashboard.ui.onderdelen.rememberVraag
import nl.odelia.dashboard.ui.onthoud.zoeken
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Nunito

private val Bruin = Color(0xFF5A3426)
private val Grijs = Color(0xFF8A6A5A)
private val Rand = Color(0xFFE3C3B0)
private val Groen = Color(0xFF6FAE84)

private val persoonKleuren = listOf(
    Color(0xFFF6B3C8) to Color(0xFFDE7FA1), Color(0xFFD2BDF0) to Color(0xFFA88AD8),
    Color(0xFFAFCBEA) to Color(0xFF7FA5D2), Color(0xFFA8D6B4) to Color(0xFF6FAE84),
    Color(0xFFF8C29C) to Color(0xFFE9956A),
)

/** Vaste kleur per naam, zodat iemand overal dezelfde kleur heeft. */
private fun kleurVan(naam: String) = persoonKleuren[Math.floorMod(naam.lowercase().hashCode(), persoonKleuren.size)]

private fun groepLabel(g: String) = when (g) {
    "School" -> "🎒 School"
    "Werk" -> "🏭 Werk"
    "Thuis" -> "🏠 Thuis"
    Mensen.OVERIG -> "👥 Overig"
    else -> "📁 $g"
}

/** Hoe het scherm opent. De extra's zijn voor de voorbeeld-plaatjes. */
data class MensenBegin(val zoek: String = "", val groep: String? = null)

/**
 * Mensen (ontwerp/Mensen.dc.html): per groep de mensen, per persoon wat je hebt afgesproken, en zoeken.
 * Alles komt uit Onthoud (het veld "wie"), er is geen aparte opslag.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MensenScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onGaNaar: (Scherm) -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
    begin: MensenBegin = MensenBegin(),
) {
    val vandaag = staat.nu.toLocalDate()
    val personen = remember(staat.onthoud) { Mensen.personen(staat.onthoud) }
    val groepen = Mensen.groepen(personen)
    var zoek by remember { mutableStateOf(begin.zoek) }
    var groep by remember { mutableStateOf(begin.groep ?: groepen.firstOrNull { g -> personen.any { it.groep == g } } ?: groepen.first()) }
    var gekozen by remember { mutableStateOf<String?>(null) }
    val vraag = rememberVraag()

    val inGroep = personen.filter { it.groep == groep }
    val cur = inGroep.firstOrNull { it.naam == gekozen } ?: inGroep.firstOrNull()

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk("Mensen", onTerug) {
                Box(
                    Modifier
                        .size(52.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Color(0xFF7FA5D2), spotColor = Color(0xFF7FA5D2))
                        .background(hoekVerloop(Color(0xFFAFCBEA), Color(0xFF7FA5D2)), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icoon(Icoontjes.mensen, 28.dp) }
            }

            // Zoekbalk
            Invoer(zoek, onWijzig = { zoek = it }, hint = "Zoek: naam, opdracht, woord…") {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Icoon(Icoontjes.zoek, 24.dp) }
                MicKnop(onClick = {
                    if (!acties.inspreken { zoek = it.trim() }) onMelding("Inspreken lukt niet op deze telefoon.")
                }, maat = 44.dp)
            }
            val tips = Mensen.probeer(staat.onthoud)
            if (tips.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tekst("probeer:", 14.sp, Modifier.align(Alignment.CenterVertically), dikte = FontWeight.ExtraBold, kleur = Color(0xFF8A5A48))
                    tips.forEach { t ->
                        Box(
                            Modifier
                                .heightIn(min = 48.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button) { zoek = t }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Tekst(
                                t, 14.sp,
                                Modifier
                                    .background(Color.White.copy(alpha = 0.6f), CircleShape)
                                    .border(2.dp, Rand, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                dikte = FontWeight.ExtraBold, kleur = Color(0xFF7A4A38),
                            )
                        }
                    }
                }
            }

            // Zoekresultaten
            if (zoek.isNotBlank()) {
                val gevonden = Mensen.afspraken(zoeken(staat.onthoud, zoek))
                Kaart {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Tekst("🔍 ${gevonden.size} gevonden", 19.sp, Modifier.weight(1f), kleur = Bruin)
                        ZachteKnop("✕ wissen", onClick = { zoek = "" })
                    }
                    if (gevonden.isEmpty()) Tekst("Niks gevonden. Probeer een ander woord.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                    gevonden.forEach { o -> Treffer(o, onKlik = { onGaNaar(Scherm.Toevoegen(id = o.id)) }) }
                    ZachteKnop("🔎 Op internet zoeken", onClick = {
                        vraag.stel(opzoekVraag(zoek) { if (!acties.openLink(Links.zoek(zoek))) onMelding("Chrome openen lukte niet.") })
                    }, Modifier.fillMaxWidth())
                }
            }

            // Groepjes
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Kleur.Kaart.copy(alpha = 0.7f), CircleShape)
                    .padding(6.dp)
                    .then(if (groepen.size > 3) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                groepen.forEach { g ->
                    GroepKnop(groepLabel(g), groep == g, if (groepen.size > 3) Modifier else Modifier.weight(1f)) {
                        groep = g
                        gekozen = null
                    }
                }
            }

            // Personen
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                inGroep.forEach { p -> PersoonRondje(p.naam, p.naam == cur?.naam) { gekozen = p.naam } }
                NieuwRondje { onGaNaar(Scherm.Toevoegen(groep = groep.takeIf { it != Mensen.OVERIG })) }
            }

            // Persoonkaart
            if (cur == null) {
                Kaart {
                    Tekst("Nog niemand bij ${groep}.", 19.sp, kleur = Bruin)
                    Tekst("Zet bij iets onthouden een naam bij 'Wie?'. Dan staat die persoon hier.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                }
            } else {
                PersoonKaart(cur, vandaag, acties, onGaNaar, groep)
            }
        }
    }
    vraag.Toon()
}

@Composable
private fun PersoonKaart(p: Persoon, vandaag: LocalDate, acties: AppActies, onGaNaar: (Scherm) -> Unit, groep: String) {
    val (licht, donker) = kleurVan(p.naam)
    val rol = p.afspraken.firstNotNullOfOrNull { it.waar?.takeIf { w -> w.isNotBlank() } }
    val groepVoorNieuw = groep.takeIf { it != Mensen.OVERIG }
    Kaart(padding = 18) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(58.dp).background(hoekVerloop(licht, donker), CircleShape), contentAlignment = Alignment.Center) {
                Tekst(Mensen.letter(p.naam), 24.sp, kleur = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Tekst(p.naam, 22.sp, kleur = Bruin, regel = 25.sp)
                Tekst(listOfNotNull(p.groep.takeIf { it != Mensen.OVERIG }, rol).joinToString(" · ").ifEmpty { "${p.afspraken.size} afspraken" }, 15.sp, dikte = FontWeight.Bold, kleur = Grijs)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Tekst("${p.nogDoen}", 28.sp, kleur = Kleur.Koraal, regel = 30.sp)
                Tekst("nog doen", 12.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
            }
        }
        Tekst("🗣️ Wat hebben we afgesproken?", 17.sp, kleur = Bruin)
        p.afspraken.forEach { o ->
            Afspraak(o, vandaag, onVink = { acties.bewaar(o.copy(klaar = it)) }, onKlik = { onGaNaar(Scherm.Toevoegen(id = o.id)) })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GroteKnop("Nieuwe afspraak", onClick = { onGaNaar(Scherm.Toevoegen(wie = p.naam, groep = groepVoorNieuw)) }, Modifier.weight(1f))
            MicKnop(onClick = { onGaNaar(Scherm.Toevoegen(inspreken = true, wie = p.naam, groep = groepVoorNieuw)) }, maat = 56.dp)
        }
    }
}

/** Eén afspraak: vierkant vinkvak, wat, "gezegd op", en een pilletje met de datum. */
@Composable
private fun Afspraak(o: Onthoud, vandaag: LocalDate, onVink: (Boolean) -> Unit, onKlik: () -> Unit) {
    val haast = Mensen.binnenkort(o, vandaag)
    val vorm = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(4.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .clip(vorm)
            .background(Color.White)
            .clickable(onClickLabel = "Aanpassen", onClick = onKlik)
            .padding(start = 2.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
            .alpha(if (o.klaar) 0.6f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(48.dp)
                .toggleable(value = o.klaar, role = Role.Checkbox, onValueChange = onVink)
                .semantics { contentDescription = if (o.klaar) "Klaar" else "Nog doen" },
            contentAlignment = Alignment.Center,
        ) {
            val v = RoundedCornerShape(10.dp)
            Box(
                Modifier.size(30.dp).background(if (o.klaar) Groen else Color.White, v).border(3.dp, if (o.klaar) Groen else Rand, v),
                contentAlignment = Alignment.Center,
            ) { if (o.klaar) Icoon(Icoontjes.vinkje, 18.dp) }
        }
        Column(Modifier.weight(1f)) {
            Text(
                o.titel,
                fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 17.sp, color = Kleur.Inkt,
                textDecoration = if (o.klaar) TextDecoration.LineThrough else null,
            )
            Tekst("gezegd op ${Mensen.gezegdOp(o.gemaakt.toLocalDate())}", 13.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }
        val (achter, inkt) = when {
            o.klaar -> Color(0xFFE3EDDF) to Color(0xFF3F7A58)
            haast -> Color(0xFFFBD9D6) to Color(0xFFC2453A)
            else -> Kleur.Kaart to Color(0xFF7A4A38)
        }
        val pil = when {
            o.klaar -> "klaar"
            o.wanneer == null -> null
            o.wanneer == vandaag -> "vandaag"
            o.wanneer == vandaag.plusDays(1) -> "morgen"
            else -> korteDatum(o.wanneer)
        }
        if (pil != null) {
            Tekst(
                pil, 14.sp,
                Modifier.background(achter, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                kleur = inkt, uitlijning = TextAlign.Center, regel = 16.sp,
            )
        }
    }
}

/** Zoekresultaat: rondje met letter, wat, wie · gezegd op. */
@Composable
private fun Treffer(o: Onthoud, onKlik: () -> Unit) {
    val naam = o.wie?.trim().orEmpty()
    val (licht, donker) = if (naam.isEmpty()) Color(0xFFDCE6F1) to Color(0xFF9DBBDE) else kleurVan(naam)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClickLabel = "Aanpassen", onClick = onKlik)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .alpha(if (o.klaar) 0.6f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(40.dp).background(hoekVerloop(licht, donker), CircleShape), contentAlignment = Alignment.Center) {
            Tekst(if (naam.isEmpty()) "•" else Mensen.letter(naam), 17.sp, kleur = Color.White)
        }
        Column(Modifier.weight(1f)) {
            Tekst(o.titel, 16.sp)
            val onder = listOfNotNull(naam.ifEmpty { o.groep }, "gezegd op ${Mensen.gezegdOp(o.gemaakt.toLocalDate())}").joinToString(" · ")
            Tekst(onder, 13.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }
    }
}

@Composable
private fun Kaart(padding: Int = 16, inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(padding.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { inhoud() }
}

@Composable
private fun GroepKnop(tekst: String, aan: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .heightIn(min = 46.dp)
            .then(if (aan) Modifier.shadow(6.dp, CircleShape, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal) else Modifier)
            .clip(CircleShape)
            .then(if (aan) Modifier.background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal)) else Modifier)
            .selectable(selected = aan, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 16.sp, kleur = if (aan) Color.White else Color(0xFF7A4A38), eenRegel = true)
    }
}

@Composable
private fun PersoonRondje(naam: String, aan: Boolean, onClick: () -> Unit) {
    val (licht, donker) = kleurVan(naam)
    Column(
        Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = aan, role = Role.Tab, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(64.dp)
                .then(if (aan) Modifier.border(3.dp, Kleur.Koraal, CircleShape).padding(5.dp) else Modifier)
                .shadow(6.dp, CircleShape, ambientColor = donker, spotColor = donker)
                .background(hoekVerloop(licht, donker), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Tekst(Mensen.letter(naam), 24.sp, kleur = Color.White) }
        Tekst(naam, 13.sp, dikte = FontWeight.ExtraBold, kleur = Bruin, uitlijning = TextAlign.Center, regel = 15.sp)
    }
}

/** Gestippeld rondje met "+": nieuwe persoon (via iets onthouden met een nieuwe naam). */
@Composable
private fun NieuwRondje(onClick: () -> Unit) {
    val stip = Color(0xFFD9A08A)
    Column(
        Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClickLabel = "Persoon toevoegen", onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(64.dp)
                .drawBehind {
                    val d = 3.dp.toPx()
                    drawCircle(
                        stip, radius = size.minDimension / 2 - d / 2,
                        style = Stroke(width = d, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
                    )
                },
            contentAlignment = Alignment.Center,
        ) { Tekst("+", 30.sp, kleur = Kleur.Koraal) }
        Tekst("nieuw", 13.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF8A5A48))
    }
}

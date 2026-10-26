package nl.odelia.dashboard.ui.geld

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import nl.odelia.dashboard.logica.BoekSoort
import nl.odelia.dashboard.logica.Boeking
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.GeldBoek
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.theme.Kleur

/** "Ik heb iets gekocht" (ontwerp: GeldGekocht + GeldKalender). Ook inkomen en sparen/overboeken. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InvoerPaneel(
    data: GeldData,
    vandaag: LocalDate,
    beginSoort: BoekSoort,
    beginKalender: Boolean,
    acties: AppActies,
    onMelding: (String) -> Unit,
    onKlaar: (BoekSoort) -> Unit,
) {
    var soort by remember { mutableStateOf(beginSoort) }
    var tekst by remember { mutableStateOf("") }
    var van by remember { mutableStateOf(GeldBoek.laatstePotje(data, beginSoort)) }
    var naar by remember { mutableStateOf<String?>(null) }
    var cat by remember { mutableStateOf(Geld.categorieen(beginSoort).first().id) }
    var meer by remember { mutableStateOf(false) }
    var datum by remember { mutableStateOf(vandaag) }
    var kalender by remember { mutableStateOf(beginKalender) }
    var tags by remember { mutableStateOf(setOf<String>()) }
    var nieuweTag by remember { mutableStateOf<String?>(null) }
    var notitie by remember { mutableStateOf("") }
    val cent = Geld.leesBedrag(tekst)

    fun kiesSoort(s: BoekSoort) {
        soort = s
        meer = false
        cat = Geld.categorieen(s).first().id
        van = GeldBoek.laatstePotje(data, s)
        naar = null
    }

    fun bewaar() {
        val c = cent
        val bron = van
        when {
            c == null || c <= 0L -> return onMelding("Typ eerst een bedrag")
            bron == null -> return onMelding("Kies een potje")
            soort == BoekSoort.Over && (naar == null || naar == bron) -> return onMelding("Kies naar welk potje")
        }
        val b = Boeking(
            id = GeldBoek.nieuwId(),
            soort = soort,
            cent = c!!,
            potjeId = bron!!,
            categorie = cat,
            datum = datum,
            naarPotjeId = if (soort == BoekSoort.Over) naar else null,
            tags = tags.toList(),
            notitie = notitie.trim().ifEmpty { null },
        )
        acties.wijzigGeld { GeldBoek.voegToe(it, b) }
        onMelding(Geld.geheugensteuntje(b, data.potjes) ?: "✓ Toegevoegd")
        onKlaar(soort)
    }

    Paneel {
        Segment(
            listOf(BoekSoort.Uit, BoekSoort.In, BoekSoort.Over), soort,
            { when (it) { BoekSoort.Uit -> "➖ Uitgave"; BoekSoort.In -> "➕ Inkomen"; BoekSoort.Over -> "🔁 Sparen" } },
            ::kiesSoort, maat = 14.sp, hoogte = 46.dp,
        )
        BedragVeld(tekst, { tekst = it }, "Bedrag") { Tekst("🧮", 26.sp) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 5, 10, 20).forEach { n ->
                Chip("€$n", cent == n * 100L, { tekst = "$n,00" }, Modifier.weight(1f), vorm = RoundedCornerShape(16.dp))
            }
        }

        GeldKopje(if (soort == BoekSoort.In) "🫙 In welk potje?" else "🫙 Uit welk potje?")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.potjes.forEach { p -> Chip("${p.emoji} ${p.naam}", van == p.id, { van = p.id }) }
        }
        if (soort == BoekSoort.Over) {
            GeldKopje("➡️ Naar welk potje?")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.potjes.filter { it.id != van }.forEach { p ->
                    Chip("${p.emoji} ${p.naam}" + if (p.rekening != null) " 🏦" else "", naar == p.id, { naar = p.id })
                }
            }
        }

        GeldKopje("🏷️ Wat was het?")
        val lijst = Geld.categorieen(soort)
        val zichtbaar = if (soort == BoekSoort.Uit && !meer) lijst.filter { it.vaak } else lijst
        val knoppen = zichtbaar.map { it.id to it } + if (soort == BoekSoort.Uit && !meer) listOf("meer" to null) else emptyList()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            knoppen.chunked(4).forEach { rij ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rij.forEach { (id, c) ->
                        CatKnop(
                            emoji = c?.emoji ?: "➕",
                            label = c?.label ?: "Meer",
                            kleur = Color(c?.kleur ?: 0xFFC9D3CE),
                            aan = cat == id,
                            modifier = Modifier.weight(1f),
                        ) { if (c == null) meer = true else cat = id }
                    }
                    repeat(4 - rij.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        GeldKopje("📅 Wanneer?")
        val eerder = vandaag.minusDays(3)
        val derde = if (datum != vandaag && datum != vandaag.minusDays(1)) datum else eerder
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(vandaag to "vandaag", vandaag.minusDays(1) to "gisteren", derde to if (derde == eerder) "eerder" else "gekozen").forEach { (d, naam) ->
                val aan = datum == d
                Box(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (aan) Groen else Color.White)
                        .selectable(aan, role = Role.RadioButton, onClick = { datum = d })
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Tekst("${GeldBoek.kort(d)}\n$naam", 14.sp, dikte = FontWeight.ExtraBold, kleur = if (aan) Color.White else Color(0xFF5A4D4A), uitlijning = TextAlign.Center, regel = 16.sp)
                }
            }
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(3.dp, if (kalender) Kleur.Koraal else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { kalender = !kalender }
                    .semantics { contentDescription = "Kalender" },
                contentAlignment = Alignment.Center,
            ) { Tekst("🗓️", 24.sp) }
        }
        if (kalender) Kalender(datum, vandaag) { datum = it; kalender = false }

        GeldKopje("🔖 Tags")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.tags.forEach { t ->
                val aan = t in tags
                Box(
                    Modifier
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .background(if (aan) Groen else Color.Transparent)
                        .border(2.dp, Groen, CircleShape)
                        .selectable(aan, role = Role.Checkbox, onClick = { tags = if (aan) tags - t else tags + t })
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) { Tekst(t, 14.sp, dikte = FontWeight.ExtraBold, kleur = if (aan) Color.White else GroenDonker) }
            }
            Box(
                Modifier
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Groen.copy(alpha = 0.7f), CircleShape)
                    .clickable { nieuweTag = "" }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) { Tekst("+ tag", 14.sp, dikte = FontWeight.ExtraBold, kleur = GroenDonker) }
        }
        nieuweTag?.let { t ->
            Invoer(t, { nieuweTag = it.take(30) }, "nieuwe tag") {
                Chip("✓", false, {
                    val n = t.trim()
                    if (n.isNotEmpty()) {
                        acties.wijzigGeld { GeldBoek.voegTagToe(it, n) }
                        tags = tags + n
                    }
                    nieuweTag = null
                })
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Invoer(notitie, { notitie = it.take(80) }, "waar / wat? (bv. Ah)", Modifier.weight(1f))
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal))
                    .clickable {
                        val gelukt = acties.inspreken { gehoord ->
                            val (bedrag, rest) = GeldBoek.uitGesproken(gehoord)
                            if (bedrag != null) tekst = invulTekst(bedrag)
                            if (rest.isNotEmpty()) notitie = rest
                        }
                        if (!gelukt) onMelding("Inspreken lukt niet op deze telefoon")
                    }
                    .semantics { contentDescription = "Inspreken" },
                contentAlignment = Alignment.Center,
            ) { Tekst("🎤", 22.sp) }
        }
        GroeneKnop("✓ Toevoegen", ::bewaar)
    }
}

@Composable
private fun CatKnop(emoji: String, label: String, kleur: Color, aan: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .selectable(aan, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            Modifier
                .size(56.dp)
                .border(4.dp, if (aan) Kleur.Koraal else Color.Transparent, CircleShape)
                .padding(if (aan) 4.dp else 2.dp)
                .shadow(4.dp, CircleShape, ambientColor = Color(0xFF785A50), spotColor = Color(0xFF785A50))
                .background(kleur, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Tekst(emoji, 24.sp) }
        Tekst(label, 12.sp, dikte = FontWeight.ExtraBold, uitlijning = TextAlign.Center, regel = 13.sp)
    }
}

/** Kalendertje in de kaart (ontwerp: GeldKalender). Dagen na vandaag kun je niet kiezen. */
@Composable
private fun Kalender(gekozen: LocalDate, vandaag: LocalDate, onKies: (LocalDate) -> Unit) {
    var maand by remember { mutableStateOf(YearMonth.from(gekozen)) }
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = Color(0xFF785A50), spotColor = Color(0xFF785A50))
            .background(Color.White, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MaandKnop("‹", "Vorige maand") { maand = maand.minusMonths(1) }
            Tekst("${GeldBoek.maanden[maand.monthValue - 1]} ${maand.year}", 18.sp, Modifier.weight(1f), kleur = Bruin, uitlijning = TextAlign.Center)
            MaandKnop("›", "Volgende maand") { if (maand < YearMonth.from(vandaag)) maand = maand.plusMonths(1) }
        }
        Row {
            listOf("ma", "di", "wo", "do", "vr", "za", "zo").forEach {
                Tekst(it, 13.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Color(0xFF9A7A6A), uitlijning = TextAlign.Center)
            }
        }
        val leeg = maand.atDay(1).dayOfWeek.value - 1
        val vakjes: List<LocalDate?> = List(leeg) { null } + (1..maand.lengthOfMonth()).map { maand.atDay(it) }
        vakjes.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { d ->
                    if (d == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val aan = d == gekozen
                        val later = d > vandaag
                        Box(
                            Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (aan) Groen else Kleur.Kaart)
                                .border(2.dp, if (d == vandaag && !aan) Groen else Color.Transparent, RoundedCornerShape(12.dp))
                                .let { if (later) it else it.clickable { onKies(d) } }
                                .semantics { contentDescription = "${d.dayOfMonth} ${GeldBoek.maanden[d.monthValue - 1]}" },
                            contentAlignment = Alignment.Center,
                        ) {
                            Tekst(
                                d.dayOfMonth.toString(), 15.sp, dikte = FontWeight.ExtraBold,
                                kleur = when { aan -> Color.White; later -> Color(0xFFC9B8AC); else -> Kleur.Inkt },
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MaandKnop(teken: String, uitleg: String, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).background(Kleur.Kaart).clickable(onClickLabel = uitleg, onClick = onClick)
            .semantics { contentDescription = uitleg },
        contentAlignment = Alignment.Center,
    ) { Tekst(teken, 20.sp, kleur = Color(0xFF7A4A38)) }
}

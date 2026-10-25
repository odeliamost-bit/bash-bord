package nl.odelia.dashboard.ui.geld

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.GeldBoek
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.logica.GeldPeriode
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop

/** Tab Uitgaven (ontwerp: GeldUitgaven): Dag/Week/Maand, ring per categorie, en "Wat heb ik gekocht?". */
@Composable
internal fun UitgavenPaneel(data: GeldData, vandaag: LocalDate, acties: AppActies, onMelding: (String) -> Unit, onLees: (String) -> Unit) {
    var periode by remember { mutableStateOf(GeldPeriode.Week) }
    var dag by remember { mutableStateOf(vandaag) }
    var weg by remember { mutableStateOf<String?>(null) }
    val totalen = Geld.perCategorie(data.boekingen, periode, dag)
    val totaal = totalen.sumOf { it.cent }
    val periodeTekst = GeldBoek.periodeTekst(periode, dag)

    Paneel(ruimte = 14.dp, midden = true) {
        Segment(
            GeldPeriode.entries, periode, { it.name }, { periode = it; dag = vandaag },
            aan = SolidColor(Groen), achter = Color.Transparent, uit = Color.White, maat = 16.sp, hoogte = 44.dp,
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Pijl("‹", "Eerder") { dag = GeldBoek.schuif(periode, dag, -1) }
            Tekst(periodeTekst, 16.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = BruinZacht, uitlijning = androidx.compose.ui.text.style.TextAlign.Center)
            Pijl("›", "Later") { if (dag < vandaag) dag = minOf(GeldBoek.schuif(periode, dag, 1), vandaag) }
        }
        Box(Modifier.size(210.dp), contentAlignment = Alignment.Center) {
            Ring(totalen.map { Color(categorie(it.categorie).kleur) to it.cent }, Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Tekst("uitgegeven", 15.sp, dikte = FontWeight.ExtraBold, kleur = BruinZacht)
                Tekst(Geld.bedrag(totaal), 28.sp, kleur = Bruin)
            }
        }
        ZachteKnop("🔊 Voorlezen", {
            val delen = totalen.take(3).joinToString(", ") { "${categorie(it.categorie).label} ${GeldBoek.uitspraak(it.cent)}" }
            onLees("$periodeTekst uitgegeven: ${GeldBoek.uitspraak(totaal)}." + if (delen.isNotEmpty()) " Meeste aan: $delen." else "")
        })
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (totalen.isEmpty()) Tekst("Nog niks uitgegeven in deze tijd.", 16.sp, dikte = FontWeight.Bold, kleur = Grijs)
            totalen.forEach { t ->
                val c = categorie(t.categorie)
                WitteRegel {
                    Box(Modifier.size(14.dp).background(Color(c.kleur), RoundedCornerShape(3.dp)))
                    Tekst(c.emoji, 22.sp)
                    Tekst(c.label, 16.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold)
                    Tekst("${t.procent}%", 14.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
                    Tekst(Geld.bedrag(t.cent), 17.sp, Modifier.widthIn(min = 64.dp), kleur = Bruin)
                }
            }
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Tekst("Wat heb ik gekocht?", 19.sp, kleur = Bruin)
            GeldBoek.uitgavenPerDag(data.boekingen, periode, dag).forEach { d ->
                Tekst(GeldBoek.dagTekst(d.datum, vandaag), 15.sp, dikte = FontWeight.ExtraBold, kleur = BruinZacht)
                d.boekingen.forEach { b -> BoekingRegel(b, data, onClick = { weg = b.id }) }
            }
        }
    }
    weg?.let { id -> WegVraag(id, data, acties, onMelding) { weg = null } }
}

@Composable
private fun Pijl(teken: String, uitleg: String, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).background(Color.White).clickable(onClickLabel = uitleg, onClick = onClick)
            .semantics { contentDescription = uitleg },
        contentAlignment = Alignment.Center,
    ) { Tekst(teken, 22.sp, kleur = Color(0xFF7A4A38)) }
}

/** Donut zoals in het ontwerp: r78, dikte 24 op 210, kleine gaatjes ertussen. */
@Composable
private fun Ring(delen: List<Pair<Color, Long>>, modifier: Modifier) {
    Canvas(modifier) {
        val dikte = size.minDimension * 24f / 210f
        val straal = size.minDimension * 78f / 210f
        val linksBoven = Offset(center.x - straal, center.y - straal)
        val boog = Size(straal * 2, straal * 2)
        drawCircle(Color(0xFFF1E4D6), radius = straal, style = Stroke(dikte))
        val som = delen.sumOf { it.second }.toFloat()
        if (som <= 0f) return@Canvas
        var start = -90f
        delen.forEach { (kleur, cent) ->
            val hoek = cent / som * 360f
            val gat = if (delen.size > 1) 2f else 0f
            if (hoek > gat) drawArc(kleur, start, hoek - gat, useCenter = false, topLeft = linksBoven, size = boog, style = Stroke(dikte))
            start += hoek
        }
    }
}

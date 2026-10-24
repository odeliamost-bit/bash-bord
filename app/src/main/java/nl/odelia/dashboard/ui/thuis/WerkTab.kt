package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/** Tabje "Werk": ring met uren van de week en staafjes per dag. */
@Composable
fun WerkTab() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        UrenRingKaart()
        StaafjesKaart()
    }
}

@Composable
private fun UrenRingKaart() {
    val week = Voorbeeld.week
    val totaal = week.sumOf { it.uren.toDouble() }.toFloat()
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tekst("Uren deze week", 20.sp)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val maat = min(180.dp, maxWidth - 104.dp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier
                        .size(maat)
                        .semantics { contentDescription = "${alsBreuk(totaal)} van ${alsBreuk(Voorbeeld.WEEKDOEL)} uur gewerkt" },
                    contentAlignment = Alignment.Center,
                ) {
                    UrenRing(week, Voorbeeld.WEEKDOEL, Modifier.fillMaxSize())
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val groot = (42f * maat.value / 180f).sp
                        Tekst(alsBreuk(totaal), groot, regel = groot)
                        Tekst("van ${alsBreuk(Voorbeeld.WEEKDOEL)} uur", 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    week.forEach { dag ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(14.dp).background(dag.ring, RoundedCornerShape(5.dp)))
                            Spacer(Modifier.width(8.dp))
                            Tekst(dag.naam, 16.sp, dikte = FontWeight.ExtraBold)
                            Spacer(Modifier.weight(1f))
                            Tekst(alsBreuk(dag.uren), 16.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                        }
                    }
                }
            }
        }
    }
}

/** Dunne donut: elk stukje is een dag, met een klein gaatje ertussen. */
@Composable
private fun UrenRing(week: List<Voorbeeld.Dag>, doel: Float, modifier: Modifier) {
    Canvas(modifier) {
        val dikte = size.minDimension * 24f / 200f
        val straal = size.minDimension * 78f / 200f
        val linksBoven = Offset(center.x - straal, center.y - straal)
        val boog = Size(straal * 2, straal * 2)
        drawCircle(Color(0xFFF1EBF8), radius = straal, style = Stroke(dikte))
        var start = -90f
        week.forEach { dag ->
            val hoek = dag.uren / doel * 360f
            drawArc(dag.ring, start, hoek - 3f, useCenter = false, topLeft = linksBoven, size = boog, style = Stroke(dikte))
            start += hoek
        }
    }
}

@Composable
private fun StaafjesKaart() {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tekst("Per dag", 20.sp, Modifier.weight(1f))
            Tekst(
                "deze week",
                14.sp,
                Modifier
                    .background(Kleur.KaartPil, CircleShape)
                    .glans(CircleShape, licht = 0.8f, donker = 0f, boven = 2.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                dikte = FontWeight.ExtraBold,
                kleur = Kleur.InktZacht,
            )
        }
        Box(Modifier.fillMaxWidth().height(180.dp)) {
            // Hulplijntjes
            Canvas(Modifier.fillMaxSize()) {
                listOf(37f, 75f, 113f).forEach { y ->
                    val yPx = y.dp.toPx()
                    drawLine(Color(0xFFEFE3D6), Offset(0f, yPx), Offset(size.width, yPx), strokeWidth = 1.dp.toPx())
                }
            }
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Voorbeeld.week.forEachIndexed { i, dag ->
                    Column(
                        Modifier.weight(1f).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Box(
                            Modifier
                                .width(30.dp)
                                .height((dag.uren * 16f).coerceAtMost(150f).dp)
                                .shadow(5.dp, CircleShape, ambientColor = dag.staaf[2], spotColor = dag.staaf[2])
                                .background(Brush.horizontalGradient(dag.staaf), CircleShape)
                                .glans(CircleShape, licht = 0.5f, donker = 0f),
                        )
                        Box(Modifier.height(28.dp), contentAlignment = Alignment.BottomCenter) {
                            if (i == Voorbeeld.VANDAAG) {
                                Tekst(
                                    dag.naam,
                                    15.sp,
                                    Modifier
                                        .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal), CircleShape)
                                        .padding(horizontal = 8.dp),
                                    kleur = Color.White,
                                )
                            } else {
                                Tekst(dag.naam, 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)
                            }
                        }
                    }
                }
            }
        }
    }
}

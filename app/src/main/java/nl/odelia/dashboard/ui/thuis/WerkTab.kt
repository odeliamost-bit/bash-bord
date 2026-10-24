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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import nl.odelia.dashboard.ui.onderdelen.Knop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.tik
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.uren.Dag
import nl.odelia.dashboard.uren.DagSoort
import nl.odelia.dashboard.uren.KlokActies
import nl.odelia.dashboard.uren.KlokStaat

/** Tabje "Werk": ring met uren van de week en staafjes per dag. Tik op een dag = aanpassen. */
@Composable
fun WerkTab(klok: KlokStaat, acties: KlokActies, onOpenDag: (LocalDate) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        UrenRingKaart(klok, acties, onOpenDag)
        StaafjesKaart(klok, onOpenDag)
        Tekst(
            "Tik op een dag om tijden te verbeteren, of om hem op vrij of ziek te zetten.",
            15.sp,
            Modifier.padding(horizontal = 8.dp),
            dikte = FontWeight.Bold,
            kleur = Kleur.InktGrijs,
        )
    }
}

@Composable
private fun UrenRingKaart(klok: KlokStaat, acties: KlokActies, onOpenDag: (LocalDate) -> Unit) {
    val totaal = klok.weekTotaal
    var doelOpen by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tekst("Uren deze week", 20.sp, Modifier.weight(1f))
            Tekst(
                "doel ${alsBreuk(klok.weekdoel)} u",
                14.sp,
                Modifier
                    .tik { doelOpen = !doelOpen }
                    .heightIn(min = 36.dp)
                    .background(Kleur.KaartPil, CircleShape)
                    .glans(CircleShape, licht = 0.8f, donker = 0f, boven = 2.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                dikte = FontWeight.ExtraBold,
                kleur = Kleur.InktZacht,
            )
        }
        if (doelOpen) WeekdoelRegel(klok.weekdoel, acties.zetWeekdoel)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val maat = min(180.dp, maxWidth - 104.dp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier
                        .size(maat)
                        .semantics { contentDescription = "${alsBreuk(totaal)} van ${alsBreuk(klok.weekdoel)} uur gewerkt" },
                    contentAlignment = Alignment.Center,
                ) {
                    UrenRing(klok, Modifier.fillMaxSize())
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val groot = (42f * maat.value / 180f).sp
                        Tekst(alsBreuk(totaal), groot, regel = groot)
                        Tekst("van ${alsBreuk(klok.weekdoel)} uur", 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    klok.week.forEachIndexed { i, dag ->
                        Row(
                            Modifier.tik { onOpenDag(dag.datum) }.fillMaxWidth().heightIn(min = 30.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(14.dp).background(DagKleur.van(dag.datum.dayOfWeek).ring, RoundedCornerShape(5.dp)))
                            Spacer(Modifier.width(8.dp))
                            Tekst(dagKort(dag.datum.dayOfWeek), 16.sp, dikte = FontWeight.ExtraBold)
                            Spacer(Modifier.weight(1f))
                            Tekst(urenOfSoort(dag, klok.weekUren[i]), 16.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                        }
                    }
                }
            }
        }
        // Vrije en zieke dagen tellen niet mee voor het doel, maar staan er wel bij (keuze van Odelia).
        klok.weekVrijZiek?.let {
            Tekst("${alsBreuk(totaal)} uur gewerkt · $it", 16.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)
        }
    }
}

/** "7¼", of "vrij"/"ziek". */
private fun urenOfSoort(dag: Dag, uren: Float): String = if (dag.soort == DagSoort.Werk) alsBreuk(uren) else dag.soort.tekst

/** Weekdoel aanpassen met − en +, per half uur. */
@Composable
private fun WeekdoelRegel(doel: Float, onZet: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tekst("Uren per week", 16.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)
        Knop("−", onClick = { onZet(doel - 0.5f) }, modifier = Modifier.width(56.dp))
        Tekst(alsBreuk(doel), 22.sp, Modifier.widthIn(min = 52.dp), uitlijning = TextAlign.Center)
        Knop("+", onClick = { onZet(doel + 0.5f) }, modifier = Modifier.width(56.dp))
    }
}

/** Dunne donut: elk stukje is een dag, met een klein gaatje ertussen. Boven het doel: ring is vol. */
@Composable
private fun UrenRing(klok: KlokStaat, modifier: Modifier) {
    Canvas(modifier) {
        val dikte = size.minDimension * 24f / 200f
        val straal = size.minDimension * 78f / 200f
        val linksBoven = Offset(center.x - straal, center.y - straal)
        val boog = Size(straal * 2, straal * 2)
        drawCircle(Color(0xFFF1EBF8), radius = straal, style = Stroke(dikte))
        val schaal = 360f / maxOf(klok.weekdoel, klok.weekTotaal)
        var start = -90f
        klok.week.forEachIndexed { i, dag ->
            val hoek = klok.weekUren[i] * schaal
            if (hoek > 3f) {
                drawArc(DagKleur.van(dag.datum.dayOfWeek).ring, start, hoek - 3f, useCenter = false, topLeft = linksBoven, size = boog, style = Stroke(dikte))
            }
            start += hoek
        }
    }
}

@Composable
private fun StaafjesKaart(klok: KlokStaat, onOpenDag: (LocalDate) -> Unit) {
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
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(if (klok.week.size > 5) 4.dp else 10.dp)) {
                klok.week.forEachIndexed { i, dag ->
                    val kleur = DagKleur.van(dag.datum.dayOfWeek)
                    val uren = klok.weekUren[i]
                    Column(
                        Modifier.weight(1f).fillMaxSize().tik { onOpenDag(dag.datum) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        if (dag.soort != DagSoort.Werk) {
                            Tekst(dag.soort.tekst, 13.sp, Modifier.padding(bottom = 4.dp), dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                        }
                        if (uren > 0f) {
                            Box(
                                Modifier
                                    .width(30.dp)
                                    .height((uren * 16f).coerceIn(12f, 150f).dp)
                                    .shadow(5.dp, CircleShape, ambientColor = kleur.staaf[2], spotColor = kleur.staaf[2])
                                    .background(Brush.horizontalGradient(kleur.staaf), CircleShape)
                                    .glans(CircleShape, licht = 0.5f, donker = 0f),
                            )
                        }
                        Box(Modifier.height(28.dp), contentAlignment = Alignment.BottomCenter) {
                            val naam = dagKort(dag.datum.dayOfWeek)
                            if (dag.datum == klok.nu.toLocalDate()) {
                                Tekst(
                                    naam,
                                    15.sp,
                                    Modifier
                                        .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal), CircleShape)
                                        .padding(horizontal = 8.dp),
                                    kleur = Color.White,
                                )
                            } else {
                                Tekst(naam, 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)
                            }
                        }
                    }
                }
            }
        }
    }
}

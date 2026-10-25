package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.Uren
import nl.odelia.dashboard.logica.Werkdag
import nl.odelia.dashboard.logica.alsBreuk
import nl.odelia.dashboard.logica.alsKlok
import nl.odelia.dashboard.logica.korteDatum
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/** Kleuren per werkdag (ma–vr), uit het ontwerp. */
private val ringKleur = listOf(Color(0xFFF7A8C4), Color(0xFFF6CF5A), Color(0xFF8EC5F5), Color(0xFFB9A3F0), Color(0xFF9ACFAE))
private val staafKleur = listOf(
    listOf(Color(0xFFF6BCAE), Color(0xFFEE9F8E), Color(0xFFDD8574)),
    listOf(Color(0xFFF8D995), Color(0xFFF2C66D), Color(0xFFE0AE50)),
    listOf(Color(0xFFBFD3EA), Color(0xFF9DBBDE), Color(0xFF7FA2CC)),
    listOf(Color(0xFFDAC8EA), Color(0xFFC6AEDD), Color(0xFFAE93CB)),
    listOf(Color(0xFFB4D8BE), Color(0xFF8EC09C), Color(0xFF74A883)),
)
private val dagKort = listOf("ma", "di", "wo", "do", "vr")

/** Tabje "Werk": ring met uren van de week, staafjes per dag en de dagen om aan te passen. */
@Composable
fun WerkTab(staat: AppStaat, acties: AppActies, onGaNaar: (Scherm) -> Unit) {
    var vorigeWeek by remember { mutableStateOf(false) }
    var bewerk by remember { mutableStateOf<Werkdag?>(null) }
    val vandaag = staat.nu.toLocalDate()
    val maandag = Uren.maandagVan(vandaag).minusWeeks(if (vorigeWeek) 1 else 0)
    val dagen = Uren.werkweek(staat.perioden, staat.soorten, maandag, staat.nu)
    val totaal = Uren.weekTotaal(staat.perioden, staat.soorten, maandag, staat.nu)

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        UrenRingKaart(dagen, totaal, staat.klok.weekdoel, if (vorigeWeek) "Uren vorige week" else "Uren deze week")
        StaafjesKaart(dagen, vandaagIndex = dagen.indexOfFirst { it.datum == vandaag }, vorigeWeek, onWissel = { vorigeWeek = !vorigeWeek })
        DagenKaart(dagen, onKies = { bewerk = it })
        GroteKnop("Urenmail voor Kim", onClick = { onGaNaar(Scherm.Urenmail) }, icoon = Icoontjes.mailWit)
    }

    bewerk?.let { dag ->
        DagBewerken(
            dag = dag,
            isVandaag = dag.datum == vandaag,
            onBewaar = { soort, begin, eind ->
                acties.zetDag(dag.datum, soort, begin, eind)
                bewerk = null
            },
            onSluit = { bewerk = null },
        )
    }
}

@Composable
private fun UrenRingKaart(dagen: List<Werkdag>, totaal: Float, doel: Float, titel: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tekst(titel, 20.sp)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val maat = min(180.dp, maxWidth - 104.dp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier
                        .size(maat)
                        .semantics { contentDescription = "${alsBreuk(totaal)} van ${alsBreuk(doel)} uur gewerkt" },
                    contentAlignment = Alignment.Center,
                ) {
                    UrenRing(dagen, doel, Modifier.fillMaxSize())
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val groot = (42f * maat.value / 180f).sp
                        Tekst(alsBreuk(totaal), groot, regel = groot)
                        Tekst("van ${alsBreuk(doel)} uur", 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    dagen.forEachIndexed { i, dag ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(14.dp).background(ringKleur[i], RoundedCornerShape(5.dp)))
                            Spacer(Modifier.width(8.dp))
                            Tekst(dagKort[i], 16.sp, dikte = FontWeight.ExtraBold)
                            Spacer(Modifier.weight(1f))
                            Tekst(korteUren(dag), 16.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                        }
                    }
                }
            }
        }
    }
}

private fun korteUren(dag: Werkdag) = when (dag.soort) {
    DagSoort.Vrij -> "vrij"
    DagSoort.Ziek -> "ziek"
    DagSoort.Leeg -> "–"
    DagSoort.Gewerkt -> alsBreuk(dag.uren)
}

/** Dunne donut: elk stukje is een dag, met een klein gaatje ertussen. */
@Composable
private fun UrenRing(dagen: List<Werkdag>, doel: Float, modifier: Modifier) {
    Canvas(modifier) {
        val dikte = size.minDimension * 24f / 200f
        val straal = size.minDimension * 78f / 200f
        val linksBoven = Offset(center.x - straal, center.y - straal)
        val boog = Size(straal * 2, straal * 2)
        drawCircle(Color(0xFFF1EBF8), radius = straal, style = Stroke(dikte))
        var start = -90f
        dagen.forEachIndexed { i, dag ->
            if (dag.uren <= 0f) return@forEachIndexed
            val hoek = (dag.uren / doel * 360f).coerceAtMost(360f + -90f - start)
            if (hoek > 3f) drawArc(ringKleur[i], start, hoek - 3f, useCenter = false, topLeft = linksBoven, size = boog, style = Stroke(dikte))
            start += hoek
        }
    }
}

@Composable
private fun StaafjesKaart(dagen: List<Werkdag>, vandaagIndex: Int, vorigeWeek: Boolean, onWissel: () -> Unit) {
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
                if (vorigeWeek) "vorige week" else "deze week",
                14.sp,
                Modifier
                    .clip(CircleShape)
                    .background(Kleur.KaartPil)
                    .glans(CircleShape, licht = 0.8f, donker = 0f, boven = 2.dp)
                    .clickable(onClickLabel = "Andere week", onClick = onWissel)
                    .heightIn(min = 36.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
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
                dagen.forEachIndexed { i, dag ->
                    Column(
                        Modifier.weight(1f).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        if (dag.uren > 0f) {
                            Box(
                                Modifier
                                    .width(30.dp)
                                    .height((dag.uren * 16f).coerceIn(12f, 150f).dp)
                                    .shadow(5.dp, CircleShape, ambientColor = staafKleur[i][2], spotColor = staafKleur[i][2])
                                    .background(Brush.horizontalGradient(staafKleur[i]), CircleShape)
                                    .glans(CircleShape, licht = 0.5f, donker = 0f),
                            )
                        } else {
                            Box(Modifier.width(30.dp).height(10.dp).background(Color(0xFFF1EBF8), CircleShape))
                        }
                        Box(Modifier.height(28.dp), contentAlignment = Alignment.BottomCenter) {
                            if (i == vandaagIndex) {
                                Tekst(
                                    dagKort[i],
                                    15.sp,
                                    Modifier
                                        .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal), CircleShape)
                                        .padding(horizontal = 8.dp),
                                    kleur = Color.White,
                                )
                            } else {
                                Tekst(dagKort[i], 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Lijstje ma–vr. Tik op een dag om hem aan te passen. */
@Composable
private fun DagenKaart(dagen: List<Werkdag>, onKies: (Werkdag) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Tekst("Dagen", 20.sp, Modifier.padding(start = 6.dp, top = 4.dp, bottom = 4.dp))
        dagen.forEach { dag ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .clickable(onClickLabel = "Aanpassen") { onKies(dag) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Tekst(korteDatum(dag.datum), 16.sp, Modifier.width(56.dp))
                Tekst(
                    when (dag.soort) {
                        DagSoort.Gewerkt -> "${dag.begin?.let(::alsKlok) ?: "?"} – ${if (dag.bezig) "nu" else dag.eind?.let(::alsKlok) ?: "?"}"
                        DagSoort.Vrij -> "vrij 🌴"
                        DagSoort.Ziek -> "ziek 🤒"
                        DagSoort.Leeg -> "niks ingevuld"
                    },
                    16.sp,
                    Modifier.weight(1f),
                    dikte = FontWeight.Bold,
                    kleur = if (dag.soort == DagSoort.Leeg) Kleur.InktGrijs else Kleur.Inkt,
                )
                Tekst(if (dag.soort == DagSoort.Gewerkt) "${alsBreuk(dag.uren)} u" else "", 16.sp, kleur = Kleur.InktZacht)
                Tekst("✏️", 16.sp)
            }
        }
    }
}

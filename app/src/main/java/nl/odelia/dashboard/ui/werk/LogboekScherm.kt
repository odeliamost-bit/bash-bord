package nl.odelia.dashboard.ui.werk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.UrenStats
import nl.odelia.dashboard.logica.Werkdag
import nl.odelia.dashboard.logica.alsBreuk
import nl.odelia.dashboard.logica.alsKlok
import nl.odelia.dashboard.logica.korteDatum
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.DagBewerken

private val staafKleuren = listOf(
    Color(0xFFF7A8C4), Color(0xFFF6CF5A), Color(0xFF8EC5F5), Color(0xFFB9A3F0), Color(0xFF9ACFAE), Color(0xFFEE9F8E),
)

/**
 * Urenlogboek (nov 2026): terugkijken wat je gewerkt hebt.
 * Bovenaan de tellers (week, maand, jaar), dan staafjes per maand, dan alle dagen van een maand.
 * Tik op een dag om hem aan te passen. Alles staat alleen op dit apparaat.
 */
@Composable
fun LogboekScherm(staat: AppStaat, acties: AppActies, onTerug: () -> Unit, modifier: Modifier = Modifier) {
    val vandaag = staat.nu.toLocalDate()
    val dagen = remember(staat.perioden, staat.soorten, vandaag) { UrenStats.dagen(staat.perioden, staat.soorten, staat.nu) }
    var maand by remember { mutableStateOf(YearMonth.from(vandaag)) }
    var bewerk by remember { mutableStateOf<Werkdag?>(null) }
    val nuMaand = YearMonth.from(vandaag)

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk("Urenlogboek", onTerug)

            Kaart {
                Tekst("Gewerkt", 20.sp)
                Teller("Deze week", UrenStats.week(dagen, vandaag))
                Teller("Deze maand", UrenStats.maand(dagen, nuMaand))
                val jaar = UrenStats.jaar(dagen, vandaag.year)
                Teller("Dit jaar", jaar)
                if (jaar.dagen > 0) {
                    Tekst("Gemiddeld ${alsBreuk(jaar.gemiddeld)} uur per dag", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                }
            }

            Kaart {
                Tekst("Per maand", 20.sp)
                val rij = UrenStats.perMaand(dagen, nuMaand)
                val hoogste = rij.maxOf { it.second.uren }.coerceAtLeast(1f)
                rij.forEachIndexed { i, (m, som) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClickLabel = "Deze maand bekijken") { maand = m },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Tekst(UrenStats.maandNamen[m.monthValue - 1].take(3), 16.sp, Modifier.width(40.dp), dikte = FontWeight.ExtraBold)
                        Box(Modifier.weight(1f).height(18.dp).background(Color(0xFFF1EBF8), CircleShape)) {
                            if (som.uren > 0f) {
                                Box(
                                    Modifier
                                        .fillMaxWidth((som.uren / hoogste).coerceIn(0.04f, 1f))
                                        .height(18.dp)
                                        .background(staafKleuren[i % staafKleuren.size], CircleShape),
                                )
                            }
                        }
                        Tekst("${alsBreuk(som.uren)} u", 16.sp, Modifier.width(64.dp), kleur = Kleur.InktZacht)
                    }
                }
            }

            Kaart {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ZachteKnop("◀", onClick = { maand = maand.minusMonths(1) })
                    Tekst(UrenStats.naam(maand), 20.sp, Modifier.weight(1f))
                    if (maand < nuMaand) ZachteKnop("▶", onClick = { maand = maand.plusMonths(1) })
                }
                val som = UrenStats.maand(dagen, maand)
                Tekst("${alsBreuk(som.uren)} uur in ${som.dagen} ${if (som.dagen == 1) "dag" else "dagen"}", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                val lijst = UrenStats.dagenVan(dagen, maand)
                if (lijst.isEmpty()) Tekst("Nog niks in deze maand.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                lijst.forEach { dag -> DagRegel(dag) { bewerk = dag } }
            }
        }
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
private fun Kaart(inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { inhoud() }
}

@Composable
private fun Teller(naam: String, som: UrenStats.Som) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tekst(naam, 17.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold)
        Tekst("${som.dagen} ${if (som.dagen == 1) "dag" else "dagen"}", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        Tekst("${alsBreuk(som.uren)} u", 24.sp, kleur = Kleur.Koraal)
    }
}

@Composable
private fun DagRegel(dag: Werkdag, onKies: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClickLabel = "Aanpassen", onClick = onKies)
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
        )
        Tekst(if (dag.soort == DagSoort.Gewerkt) "${alsBreuk(dag.uren)} u" else "", 16.sp, kleur = Kleur.InktZacht)
        Tekst("✏️", 16.sp)
    }
}

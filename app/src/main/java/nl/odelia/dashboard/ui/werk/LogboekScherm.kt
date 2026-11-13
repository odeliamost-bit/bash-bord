package nl.odelia.dashboard.ui.werk

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.DagBewerken

/** Urenlogboek: tellen per week, maand en jaar, en per maand alle dagen terugkijken en aanpassen. */
@Composable
fun LogboekScherm(staat: AppStaat, acties: AppActies, onTerug: () -> Unit, modifier: Modifier = Modifier) {
    val vandaag = staat.nu.toLocalDate()
    val dagen = UrenStats.dagen(staat.perioden, staat.soorten, staat.nu)
    var maand by remember { mutableStateOf(YearMonth.from(vandaag)) }
    var bewerk by remember { mutableStateOf<Werkdag?>(null) }
    val vanMaand = UrenStats.vanMaand(dagen, maand)

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
                Tekst("📊 Hoeveel gewerkt", 20.sp)
                Regel("Deze week", UrenStats.week(dagen, vandaag))
                Regel("Vorige week", UrenStats.week(dagen, vandaag.minusWeeks(1)))
                Regel("Deze maand", UrenStats.maand(dagen, YearMonth.from(vandaag)))
                Regel("Vorige maand", UrenStats.maand(dagen, YearMonth.from(vandaag).minusMonths(1)))
                Regel("Dit jaar", UrenStats.jaar(dagen, vandaag.year))
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ZachteKnop("‹", onClick = { maand = maand.minusMonths(1) })
                Tekst(UrenStats.naam(maand), 20.sp, Modifier.weight(1f), uitlijning = TextAlign.Center)
                ZachteKnop("›", onClick = { if (maand < YearMonth.from(vandaag)) maand = maand.plusMonths(1) })
            }

            Kaart {
                val som = UrenStats.maand(dagen, maand)
                Tekst("${alsBreuk(som.uren)} uur in ${som.dagen} ${if (som.dagen == 1) "dag" else "dagen"}", 18.sp, kleur = Kleur.InktZacht)
                if (vanMaand.isEmpty()) Tekst("Nog niks in deze maand.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                vanMaand.forEach { dag ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable(onClickLabel = "Dag aanpassen") { bewerk = dag },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Tekst(korteDatum(dag.datum), 17.sp, Modifier.weight(1f))
                        Tekst(tijden(dag), 16.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                        Tekst(if (dag.soort == DagSoort.Gewerkt) "${alsBreuk(dag.uren)} u" else "", 17.sp, kleur = Kleur.InktZacht)
                        Tekst("✏️", 16.sp)
                    }
                }
            }
            Tekst("🔒 Je uren blijven alleen op deze telefoon.", 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
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

private fun tijden(dag: Werkdag): String = when (dag.soort) {
    DagSoort.Vrij -> "vrij"
    DagSoort.Ziek -> "ziek"
    DagSoort.Leeg -> ""
    DagSoort.Gewerkt -> "${dag.begin?.let(::alsKlok) ?: "?"} – ${dag.eind?.let(::alsKlok) ?: if (dag.bezig) "nu" else "?"}"
}

@Composable
private fun Kaart(inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(28.dp), Kleur.KaartSchaduw)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { inhoud() }
}

@Composable
private fun Regel(naam: String, som: UrenStats.Som) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Tekst(naam, 17.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold)
        Tekst(
            if (som.dagen == 0) "–" else "${alsBreuk(som.uren)} u · ${som.dagen} d · ${alsBreuk(som.gemiddeld)} per dag",
            16.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs,
        )
    }
}

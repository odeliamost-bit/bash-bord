package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.Werkdag
import nl.odelia.dashboard.logica.alsKlok
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TijdKiezer
import nl.odelia.dashboard.ui.onderdelen.Venster
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.theme.Kleur

/** Venster om één dag aan te passen: gewerkt (van–tot), vrij, ziek of niks. */
@Composable
fun DagBewerken(
    dag: Werkdag,
    isVandaag: Boolean,
    onBewaar: (DagSoort, LocalTime?, LocalTime?) -> Unit,
    onSluit: () -> Unit,
) {
    var soort by remember { mutableStateOf(if (dag.soort == DagSoort.Leeg) DagSoort.Gewerkt else dag.soort) }
    var begin by remember { mutableStateOf(dag.begin ?: LocalTime.of(7, 45)) }
    var eind by remember { mutableStateOf(if (dag.bezig) null else dag.eind ?: LocalTime.of(15, 45)) }
    var kiest by remember { mutableStateOf<String?>(null) }
    val fout = soort == DagSoort.Gewerkt && eind?.isAfter(begin) == false

    Venster(
        titel = datumLang(dag),
        onSluit = onSluit,
        onKlaar = {
            if (!fout) onBewaar(soort, begin.takeIf { soort == DagSoort.Gewerkt }, eind.takeIf { soort == DagSoort.Gewerkt })
        },
        klaarTekst = "Bewaren",
    ) {
        Column(Modifier.widthIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(DagSoort.Gewerkt to "gewerkt", DagSoort.Vrij to "vrij", DagSoort.Ziek to "ziek", DagSoort.Leeg to "niks").forEach { (s, tekst) ->
                    Keuze(tekst, soort == s, onClick = { soort = s }, Modifier.weight(1f))
                }
            }
            if (soort == DagSoort.Gewerkt) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Tekst("begin", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                        ZachteKnop(alsKlok(begin), onClick = { kiest = "begin" }, Modifier.fillMaxWidth(), kleur = Kleur.Inkt)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Tekst("eind", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                        ZachteKnop(eind?.let(::alsKlok) ?: "nog bezig", onClick = { kiest = "eind" }, Modifier.fillMaxWidth(), kleur = Kleur.Inkt)
                    }
                }
                if (isVandaag) {
                    Keuze("ik ben nog aan het werk", eind == null, onClick = { eind = if (eind == null) LocalTime.of(15, 45) else null }, Modifier.fillMaxWidth())
                }
                if (fout) Tekst("De eindtijd moet na de begintijd zijn.", 15.sp, kleur = Kleur.KoraalDiep)
                Tekst("De 45 min pauze gaat er vanzelf af.", 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
            }
        }
    }

    when (kiest) {
        "begin" -> TijdKiezer("Begonnen om", begin, onKies = { begin = it; kiest = null }, onSluit = { kiest = null })
        "eind" -> TijdKiezer("Gestopt om", eind ?: LocalTime.of(15, 45), onKies = { eind = it; kiest = null }, onSluit = { kiest = null })
    }
}

private fun datumLang(dag: Werkdag): String = datumTekst(dag.datum.atStartOfDay())

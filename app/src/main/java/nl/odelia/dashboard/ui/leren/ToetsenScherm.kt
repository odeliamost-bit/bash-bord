package nl.odelia.dashboard.ui.leren

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import nl.odelia.dashboard.logica.LerenData
import nl.odelia.dashboard.logica.LerenInhoud
import nl.odelia.dashboard.logica.Toets
import nl.odelia.dashboard.logica.dagenTot
import nl.odelia.dashboard.ui.onderdelen.DatumKiezer
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastels

private val datumTekst = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale("nl", "NL"))

/** Toetsen plannen (okt 2026): wat, welk vak, wanneer en welke stof. Bewaard in leren.json. */
@Composable
fun ToetsenScherm(
    leren: LerenData,
    vandaag: LocalDate,
    onWijzig: ((LerenData) -> LerenData) -> Unit,
    onTerug: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var naam by remember { mutableStateOf("") }
    var stof by remember { mutableStateOf("") }
    var vak by remember { mutableStateOf(leren.vak) }
    var datum by remember { mutableStateOf(vandaag.plusDays(7)) }
    var kiesDatum by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TerugBalk("Toetsen", onTerug)

            val lijst = leren.toetsen.sortedBy { it.datum }
            val komend = lijst.filter { it.dagenTot(vandaag) >= 0 }
            val voorbij = lijst.filter { it.dagenTot(vandaag) < 0 }
            if (lijst.isEmpty()) Tekst("Nog geen toetsen. Zet je eerste toets hieronder.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
            komend.forEach { t -> ToetsKaart(t, vandaag, onWeg = { onWijzig { d -> d.copy(toetsen = d.toetsen.filter { it.id != t.id }) } }) }
            if (voorbij.isNotEmpty()) {
                Tekst("Voorbij", 15.sp, kleur = Kleur.InktGrijs)
                voorbij.takeLast(5).reversed().forEach { t ->
                    ToetsKaart(t, vandaag, Modifier.alpha(0.6f), onWeg = { onWijzig { d -> d.copy(toetsen = d.toetsen.filter { it.id != t.id }) } })
                }
            }

            // Nieuwe toets
            Column(
                Modifier.fillMaxWidth().klei(Pastels.Abrikoos.kaart, RoundedCornerShape(26.dp), Pastels.Abrikoos.donker, hoogte = 10.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Tekst("Nieuwe toets", 20.sp, kleur = Pastels.Abrikoos.groot)
                Invoer(naam, { naam = it.take(60) }, "Bijv. Wet van Ohm (toets 2)")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LerenInhoud.vakken.forEach { v -> Keuze("${v.emoji} ${v.naam}", v.id == vak, onClick = { vak = v.id }) }
                }
                ZachteKnop("📅 ${datum.format(datumTekst)}", onClick = { kiesDatum = true })
                Invoer(stof, { stof = it.take(200) }, "Welke stof? (mag leeg)", regels = 2)
                GroteKnop("Bewaar toets", onClick = {
                    if (naam.isNotBlank()) {
                        val nieuw = Toets(UUID.randomUUID().toString(), vak, naam.trim(), datum, stof.trim())
                        onWijzig { d -> d.copy(toetsen = d.toetsen + nieuw) }
                        naam = ""; stof = ""
                    }
                })
            }
        }
    }
    if (kiesDatum) DatumKiezer(datum, onKies = { datum = it; kiesDatum = false }, onSluit = { kiesDatum = false })
}

@Composable
private fun ToetsKaart(t: Toets, vandaag: LocalDate, modifier: Modifier = Modifier, onWeg: () -> Unit) {
    val n = t.dagenTot(vandaag)
    val (p, wanneer) = when {
        n < 0 -> Pastels.Blauw to "voorbij"
        n == 0L -> Pastels.Koraal to "vandaag!"
        n == 1L -> Pastels.Koraal to "morgen"
        n <= 7 -> Pastels.Geel to "over $n dagen"
        else -> Pastels.Salie to "over $n dagen"
    }
    val vak = LerenInhoud.vakken.firstOrNull { it.id == t.vak }
    Column(
        modifier.fillMaxWidth().klei(p.kaart, RoundedCornerShape(26.dp), p.donker, hoogte = 10.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tekst(t.naam, 19.sp, Modifier.weight(1f), kleur = p.groot)
            Tekst(wanneer, 15.sp, kleur = p.label)
        }
        Tekst("${vak?.emoji ?: ""} ${vak?.naam ?: t.vak} · ${t.datum.format(datumTekst)}", 14.sp, dikte = FontWeight.Bold, kleur = p.klein)
        if (t.stof.isNotBlank()) Tekst(t.stof, 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
        ZachteKnop("🗑 Weg", onClick = onWeg)
    }
}

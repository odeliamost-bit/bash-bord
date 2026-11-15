package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import nl.odelia.dashboard.logica.Bolletje
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.Weekstreep
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

private val dagNaam = listOf("ma", "di", "wo", "do", "vr")
private val dagLang = listOf("maandag", "dinsdag", "woensdag", "donderdag", "vrijdag")

/** Zelfde vijf kleuren als de uren-ring op het tabje Werk. */
private val dagKleur = listOf(Color(0xFFF7A8C4), Color(0xFFF6CF5A), Color(0xFF8EC5F5), Color(0xFFB9A3F0), Color(0xFF9ACFAE))

/** Weekstreep: vijf bolletjes (ma–vr). In één oogopslag zien welke dagen kloppen. */
@Composable
fun WeekstreepKaart(klok: KlokStaat) {
    val vandaag = klok.nu.toLocalDate()
    val bolletjes = Weekstreep.bolletjes(klok.week, vandaag)
    Row(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(26.dp), Kleur.KaartSchaduw, hoogte = 8.dp)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        bolletjes.forEachIndexed { i, b ->
            val isVandaag = klok.week[i].datum == vandaag
            Column(
                Modifier.semantics(mergeDescendants = true) { contentDescription = "${dagLang[i]}: ${b.uitleg}" },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Bol(b, dagKleur[i], isVandaag)
                Tekst(
                    dagNaam[i], 15.sp,
                    dikte = if (isVandaag) FontWeight.Black else FontWeight.ExtraBold,
                    kleur = if (isVandaag) Kleur.Inkt else Kleur.InktGrijs,
                )
            }
        }
    }
}

@Composable
private fun Bol(b: Bolletje, kleur: Color, isVandaag: Boolean) {
    val vol = b == Bolletje.Gewerkt || b == Bolletje.Bezig
    val achter = when (b) {
        Bolletje.Gewerkt, Bolletje.Bezig -> kleur
        Bolletje.Vrij, Bolletje.Ziek -> kleur.copy(alpha = 0.35f)
        Bolletje.Gemist -> Color(0xFFFFF1C6)
        Bolletje.Komt -> Kleur.KaartPil
    }
    Box(
        Modifier
            .size(44.dp)
            .background(achter, CircleShape)
            .then(if (isVandaag) Modifier.border(3.dp, Kleur.Koraal, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(
            b.teken, if (b == Bolletje.Bezig) 16.sp else 20.sp,
            kleur = when {
                vol -> Color.White
                b == Bolletje.Gemist -> Color(0xFF6E5410)
                else -> Kleur.Inkt
            },
            regel = 22.sp,
        )
    }
}

/** Vrij of ziek met één tik. Verkeerd getikt? Dan met één tik weer terug. */
@Composable
fun VrijZiekRij(klok: KlokStaat, onZet: (LocalDate, DagSoort) -> Unit) {
    val vandaag = klok.nu.toLocalDate()
    when {
        Weekstreep.kanVrijOfZiek(klok) -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ZachteKnop("🌴 Vrij vandaag", onClick = { onZet(vandaag, DagSoort.Vrij) }, modifier = Modifier.weight(1f))
            ZachteKnop("🤒 Ziek vandaag", onClick = { onZet(vandaag, DagSoort.Ziek) }, modifier = Modifier.weight(1f))
        }
        Weekstreep.kanTerug(klok) -> ZachteKnop(
            if (klok.vandaag.soort == DagSoort.Vrij) "↩️ Toch niet vrij" else "↩️ Toch niet ziek",
            onClick = { onZet(vandaag, DagSoort.Leeg) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

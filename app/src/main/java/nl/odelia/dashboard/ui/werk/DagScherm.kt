package nl.odelia.dashboard.ui.werk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Knop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.tik
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastels
import nl.odelia.dashboard.ui.thuis.GroteKnop
import nl.odelia.dashboard.ui.thuis.alsBreuk
import nl.odelia.dashboard.ui.thuis.datumTekst
import nl.odelia.dashboard.ui.thuis.tijdTekst
import nl.odelia.dashboard.uren.Blok
import nl.odelia.dashboard.uren.Dag
import nl.odelia.dashboard.uren.DagSoort
import nl.odelia.dashboard.uren.KlokActies
import nl.odelia.dashboard.uren.Uren

/**
 * Eén dag aanpassen: gewerkt, vrij of ziek, en de begin- en eindtijden.
 * Alles wordt meteen bewaard.
 */
@Composable
fun DagScherm(
    dag: Dag,
    uren: Float,
    nu: LocalDateTime,
    acties: KlokActies,
    onTerug: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Kop(datumTekst(dag.datum).replaceFirstChar { it.uppercase() }, onTerug)

            SoortKaart(dag.soort) { acties.zetSoort(dag.datum, it) }

            if (dag.soort == DagSoort.Werk) {
                dag.blokken.forEachIndexed { i, blok ->
                    BlokKaart(blok, nummer = if (dag.blokken.size > 1) i + 1 else null, nu = nu, acties = acties)
                }
                if (dag.blokken.isEmpty()) {
                    Tekst(
                        "Op deze dag is nog niet geklokt.",
                        17.sp,
                        Modifier.padding(horizontal = 8.dp),
                        dikte = FontWeight.Bold,
                        kleur = Kleur.InktGrijs,
                    )
                }
                GroteKnop(
                    if (dag.blokken.isEmpty()) "Tijden invullen" else "Nog een blok",
                    onClick = { acties.bewaar(nieuwBlok(dag)) },
                )
                TotaalKaart(uren)
            }
        }
    }
}

/** Terug-knop met de datum ernaast. */
@Composable
private fun Kop(titel: String, onTerug: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .tik(onClick = onTerug)
                .size(48.dp)
                .shadow(6.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                .clip(CircleShape)
                .background(Kleur.Kaart)
                .semantics { contentDescription = "Terug" },
            contentAlignment = Alignment.Center,
        ) {
            Icoon(Icoontjes.terug, 24.dp)
        }
        Tekst(titel, 26.sp)
    }
}

/** Drie grote keuzes: gewerkt, vrij, ziek. */
@Composable
private fun SoortKaart(soort: DagSoort, onKies: (DagSoort) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tekst("Wat voor dag?", 19.sp)
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                Triple(DagSoort.Werk, "💼", Pastels.Salie),
                Triple(DagSoort.Vrij, "🌿", Pastels.Blauw),
                Triple(DagSoort.Ziek, "🤒", Pastels.Roze),
            ).forEach { (s, emoji, p) ->
                val aan = s == soort
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .tik { onKies(s) }
                        .heightIn(min = 84.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(p.kaart)
                        .border(3.dp, if (aan) Kleur.Koraal else Color.Transparent, RoundedCornerShape(20.dp))
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Tekst(emoji, 26.sp)
                    Tekst(s.tekst, 16.sp, dikte = FontWeight.ExtraBold, kleur = p.label)
                }
            }
        }
    }
}

/** Begin- en eindtijd van één blok, met knoppen om ze te verschuiven. */
@Composable
private fun BlokKaart(blok: Blok, nummer: Int?, nu: LocalDateTime, acties: KlokActies) {
    val begin = blok.begin.toLocalTime()
    val eind = blok.eind?.toLocalTime()
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (nummer != null) Tekst("Blok $nummer", 19.sp)
        TijdRegel("Begin", begin) { nieuw ->
            val max = eind?.minusMinutes(5) ?: LocalTime.of(23, 55)
            acties.bewaar(blok.copy(begin = blok.datum.atTime(minOf(nieuw, max))))
        }
        if (eind != null) {
            TijdRegel("Eind", eind) { nieuw ->
                acties.bewaar(blok.copy(eind = blok.datum.atTime(maxOf(nieuw, begin.plusMinutes(5)))))
            }
        } else {
            val vandaag = blok.datum == nu.toLocalDate()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Tekst("Eind", 17.sp, Modifier.widthIn(min = 64.dp), dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)
                Tekst(if (vandaag) "nog bezig" else "vergeten", 22.sp, Modifier.weight(1f), kleur = Kleur.Inkt)
                val voorstel = if (vandaag) nu.toLocalTime() else maxOf(LocalTime.of(15, 45), begin.plusMinutes(5))
                Knop(
                    "eind ${tijdTekst(voorstel)}",
                    onClick = { acties.bewaar(blok.copy(eind = blok.datum.atTime(maxOf(voorstel, begin.plusMinutes(5))))) },
                    hoofd = true,
                )
            }
        }
        // Twee keer tikken, zodat je niet per ongeluk je uren weggooit.
        var zeker by remember(blok.id) { mutableStateOf(false) }
        Knop(
            if (zeker) "Echt weghalen? Tik nog een keer" else "Blok weghalen",
            onClick = { if (zeker) acties.verwijder(blok.id) else zeker = true },
            modifier = Modifier.fillMaxWidth(),
            hoofd = zeker,
        )
    }
}

/** "Begin  7:45" met daaronder −1 uur, −5 min, +5 min, +1 uur. */
@Composable
private fun TijdRegel(naam: String, tijd: LocalTime, onZet: (LocalTime) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tekst(naam, 17.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)
            Tekst(tijdTekst(tijd), 32.sp, kleur = Kleur.Inkt, regel = 34.sp)
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Binnen dezelfde dag blijven: niet voor 0:00 en niet na 23:55.
            fun schuif(minuten: Long) {
                val van = tijd.toSecondOfDay() / 60L
                onZet(LocalTime.ofSecondOfDay((van + minuten).coerceIn(0L, 23L * 60 + 55) * 60))
            }
            listOf(-60L to "−1\u00A0u", -5L to "−5", 5L to "+5", 60L to "+1\u00A0u").forEach { (min, tekst) ->
                Knop(tekst, onClick = { schuif(min) }, modifier = Modifier.weight(1f).fillMaxHeight(), smal = true)
            }
        }
    }
}

@Composable
private fun TotaalKaart(uren: Float) {
    val p = Pastels.Salie
    Row(
        Modifier
            .fillMaxWidth()
            .klei(p.kaart, RoundedCornerShape(26.dp), p.donker, hoogte = 10.dp)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Tekst("Totaal", 17.sp, dikte = FontWeight.ExtraBold, kleur = p.label)
            Tekst("met ${Uren.PAUZE.toMinutes()} min pauze eraf", 14.sp, dikte = FontWeight.Bold, kleur = p.klein)
        }
        Tekst("${alsBreuk(uren)} u", 32.sp, kleur = p.groot, uitlijning = TextAlign.End)
    }
}

/** Een nieuw blok: gewone werktijd als de dag nog leeg is, anders na het laatste blok. */
private fun nieuwBlok(dag: Dag): Blok {
    val laatste = dag.blokken.mapNotNull { it.eind }.maxOrNull()
    val (van, tot) = if (laatste == null) {
        LocalTime.of(7, 45) to LocalTime.of(15, 45)
    } else {
        val start = minOf(laatste.toLocalTime().plusMinutes(30), LocalTime.of(23, 0))
        start to minOf(start.plusHours(1), LocalTime.of(23, 55))
    }
    return Blok(UUID.randomUUID().toString(), dag.datum.atTime(van), dag.datum.atTime(tot))
}

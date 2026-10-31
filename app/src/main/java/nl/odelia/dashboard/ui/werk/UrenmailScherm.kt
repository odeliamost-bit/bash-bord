package nl.odelia.dashboard.ui.werk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.Uren
import nl.odelia.dashboard.logica.UrenBereik
import nl.odelia.dashboard.logica.UrenRij
import nl.odelia.dashboard.logica.Urenmail
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.DatumKiezer
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/**
 * De urenmail voor Kim:
 * 1. kiezen welke uren (vandaag, deze week, deze maand of zelf van–tot);
 * 2. voorbeeld bekijken (tabel + totaal), opmerking erbij;
 * 3. pas dan Gmail openen, met de tabel ook als PDF-bijlage. Versturen doet Odelia zelf.
 */
@Composable
fun UrenmailScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onNaarWerk: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
    begin: UrenBereik? = null,
) {
    val vandaag = staat.nu.toLocalDate()
    var bereik by remember { mutableStateOf(begin) }
    var van by remember { mutableStateOf(Uren.maandagVan(vandaag)) }
    var tot by remember { mutableStateOf(vandaag) }
    var kiesVan by remember { mutableStateOf(false) }
    var kiesTot by remember { mutableStateOf(false) }
    var opmerking by remember { mutableStateOf("") }
    val klembord = LocalClipboardManager.current

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk("Urenmail", onTerug)

            // 1. Welke uren?
            Kaart {
                Tekst("1️⃣ Welke uren stuur je?", 18.sp, kleur = Kleur.InktZacht)
                UrenBereik.entries.chunked(2).forEach { paar ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        paar.forEach { b ->
                            Keuze(b.knop, bereik == b, onClick = { bereik = b }, Modifier.weight(1f), achter = Color(0xFFFFF4EC))
                        }
                    }
                }
                if (bereik == UrenBereik.Zelf) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Keuze("📅 van ${Urenmail.korteDatum(van)}", false, onClick = { kiesVan = true }, Modifier.weight(1f), achter = Color(0xFFD4E8FF))
                        Keuze("📅 tot ${Urenmail.korteDatum(tot)}", false, onClick = { kiesTot = true }, Modifier.weight(1f), achter = Color(0xFFD4E8FF))
                    }
                }
            }

            val b = bereik
            if (b == null) {
                Uitleg("👆 Kies eerst welke uren. Daarna zie je de mail.")
            } else {
                val periode = Urenmail.periode(b, vandaag, van, tot)
                val dagen = Urenmail.dagen(staat.perioden, staat.soorten, periode, staat.nu)
                val leeg = dagen.count { it.soort == DagSoort.Leeg }
                val bezig = dagen.any { it.bezig }

                if (leeg > 0) {
                    Waarschuwing("Let op: $leeg ${if (leeg == 1) "dag is" else "dagen zijn"} nog leeg.", "Was je vrij of ziek? Vul het in bij Werk.") {
                        ZachteKnop("Naar Werk", onClick = onNaarWerk, Modifier.fillMaxWidth())
                    }
                }
                if (bezig) Waarschuwing("Je bent nog ingeklokt.", "De uren van vandaag lopen nog door.") {}

                if (dagen.isEmpty()) {
                    Uitleg("In deze periode zijn nog geen werkdagen.")
                } else {
                    Invoer(opmerking, onWijzig = { opmerking = it }, hint = "2️⃣ Opmerking voor Kim (mag leeg)", regels = 2)

                    // 3. Voorbeeld: zo ziet Kim het.
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .klei(Color.White, RoundedCornerShape(24.dp), Kleur.KaartSchaduw)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Tekst("3️⃣ Voorbeeld", 18.sp, kleur = Kleur.InktZacht)
                        Tekst("Onderwerp: ${Urenmail.onderwerp(periode)}", 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                        Tekst(Urenmail.brief(opmerking, periode.zin), 16.sp, dikte = FontWeight.SemiBold, regel = 23.sp)
                        Tabel(dagen.map(Urenmail::rij), Urenmail.decimaal(Urenmail.totaal(dagen)))
                        Tekst(Urenmail.totaalRegel(dagen), 17.sp, dikte = FontWeight.ExtraBold)
                        Tekst("📎 ${Urenmail.bestandsnaam(periode)}", 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                    }

                    GroteKnop("Open in Gmail", icoon = Icoontjes.mailWit, onClick = {
                        if (!acties.openUrenmail(periode, dagen, opmerking)) onMelding("Geen mail-app gevonden.")
                    })
                    ZachteKnop("Kopieer tekst", onClick = {
                        klembord.setText(AnnotatedString(Urenmail.tekst(dagen, opmerking, periode.zin)))
                        onMelding("Gekopieerd ✓")
                    }, Modifier.fillMaxWidth())

                    Uitleg(
                        "Gmail opent met deze mail en de tabel als PDF. Kies zelf Kim en druk zelf op versturen. " +
                            "De uren blijven op je telefoon. Pas als jij verstuurt, gaan ze naar Kim.",
                    )
                }
            }
        }
    }

    if (kiesVan) DatumKiezer(van, onKies = { van = it; kiesVan = false }, onSluit = { kiesVan = false })
    if (kiesTot) DatumKiezer(tot, onKies = { tot = it; kiesTot = false }, onSluit = { kiesTot = false })
}

/** De tabel zoals in de PDF. Smal genoeg voor de telefoon: 5 kolommen, kleine letters. */
@Composable
private fun Tabel(rijen: List<UrenRij>, totaal: String) {
    val vorm = RoundedCornerShape(14.dp)
    Column(Modifier.fillMaxWidth().background(Color(0xFFFBF3EA), vorm)) {
        Regel(UrenRij(Urenmail.KOPJES[0], Urenmail.KOPJES[1], Urenmail.KOPJES[2], Urenmail.KOPJES[3], Urenmail.KOPJES[4]), kop = true)
        rijen.forEachIndexed { i, r -> Regel(r, streep = i % 2 == 1) }
        Regel(UrenRij("Totaal", "", "", "", totaal), kop = true)
    }
}

@Composable
private fun Regel(r: UrenRij, kop: Boolean = false, streep: Boolean = false) {
    val achter = when {
        kop -> Color(0xFFFAD8CF)
        streep -> Color.White.copy(alpha = 0.6f)
        else -> Color.Transparent
    }
    val dikte = if (kop) FontWeight.Black else FontWeight.Bold
    Row(Modifier.fillMaxWidth().background(achter).padding(horizontal = 8.dp, vertical = 7.dp)) {
        Vakje(r.datum, 1.5f, dikte)
        if (!kop && r.eind.isEmpty() && r.totaal.isEmpty()) {
            // vrij / ziek / leeg: het woord loopt over de lege vakjes heen
            Vakje(r.begin, 4f, dikte)
        } else {
            Vakje(r.begin, 1f, dikte)
            Vakje(r.eind, 1f, dikte)
            Vakje(r.pauze, 1f, dikte)
            Vakje(r.totaal, 1f, dikte)
        }
    }
}

@Composable
private fun RowScope.Vakje(tekst: String, gewicht: Float, dikte: FontWeight) {
    Tekst(tekst, 13.sp, Modifier.weight(gewicht), dikte = dikte, kleur = Kleur.Inkt, eenRegel = true)
}

@Composable
private fun Kaart(inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(24.dp), Kleur.KaartSchaduw)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { inhoud() }
}

@Composable
private fun Waarschuwing(titel: String, uitleg: String, extra: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Color(0xFFFFF1C6), RoundedCornerShape(22.dp), Color(0xFFC48C3C), hoogte = 8.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Tekst(titel, 16.sp, kleur = Color(0xFF4E3B08))
        Tekst(uitleg, 15.sp, dikte = FontWeight.Bold, kleur = Color(0xFF6E5410))
        extra()
    }
}

@Composable
private fun Uitleg(tekst: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Kleur.Kaart.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Tekst("ℹ️ Zo werkt het", 15.sp, kleur = Kleur.InktZacht)
        Tekst(tekst, 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
    }
}

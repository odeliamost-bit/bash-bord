package nl.odelia.dashboard.ui.werk

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.Uren
import nl.odelia.dashboard.logica.Urenmail
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.datumTekst

/** De urenmail voor Kim: bekijken, opmerking toevoegen en als nieuwe mail in Gmail zetten. */
@Composable
fun UrenmailScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onNaarWerk: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var opmerking by remember { mutableStateOf("") }
    val maandag = Urenmail.maandag(staat.nu.toLocalDate())
    val dagen = Uren.werkweek(staat.perioden, staat.soorten, maandag, staat.nu)
    val tekst = Urenmail.tekst(dagen, opmerking)
    val leeg = dagen.count { it.soort == DagSoort.Leeg }
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
            Tekst("Week van ${datumTekst(maandag.atStartOfDay())}", 17.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)

            if (leeg > 0) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .klei(Color(0xFFFFF1C6), RoundedCornerShape(22.dp), Color(0xFFC48C3C), hoogte = 8.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Tekst("Let op: $leeg ${if (leeg == 1) "dag is" else "dagen zijn"} nog leeg.", 16.sp, kleur = Color(0xFF4E3B08))
                    Tekst("Was je vrij of ziek? Vul het in bij Werk.", 15.sp, dikte = FontWeight.Bold, kleur = Color(0xFF6E5410))
                    ZachteKnop("Naar Werk", onClick = onNaarWerk, Modifier.fillMaxWidth())
                }
            }

            Invoer(opmerking, onWijzig = { opmerking = it }, hint = "Opmerking voor Kim (mag leeg)", regels = 2)

            // Voorbeeld van de mail
            Column(
                Modifier
                    .fillMaxWidth()
                    .klei(Color.White, RoundedCornerShape(24.dp), Kleur.KaartSchaduw)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Tekst("Onderwerp: ${Urenmail.ONDERWERP}", 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
                Tekst(tekst, 16.sp, dikte = FontWeight.SemiBold, regel = 23.sp)
            }

            GroteKnop("Open in Gmail", icoon = Icoontjes.mailWit, onClick = {
                if (!acties.openMail(Urenmail.ONDERWERP, tekst)) onMelding("Geen mail-app gevonden.")
            })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ZachteKnop("Kopieer tekst", onClick = {
                    klembord.setText(AnnotatedString(tekst))
                    onMelding("Gekopieerd ✓")
                }, Modifier.weight(1f))
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Kleur.Kaart.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Tekst("ℹ️ Zo werkt het", 15.sp, kleur = Kleur.InktZacht)
                Tekst(
                    "Gmail opent een nieuwe mail met deze tekst. Kies zelf Kim en druk zelf op versturen. " +
                        "Antwoorden in de draad \"Urenregistratie\" kan nog niet.",
                    14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs,
                )
            }
        }
    }
}

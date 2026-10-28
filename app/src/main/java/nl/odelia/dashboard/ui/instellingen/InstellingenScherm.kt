package nl.odelia.dashboard.ui.instellingen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/** Back-up maken, terugzetten, en de oude Kompas-gegevens inlezen. */
@Composable
fun InstellingenScherm(
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var zekerTerug by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TerugBalk(Plek.Instellingen.label, onTerug)

            Kaart(
                titel = "💾 Back-up",
                uitleg = "Alles uit de app in één bestand. Bewaar het in Downloads of Google Drive.",
            ) {
                GroteKnop("Back-up maken", onClick = {
                    if (!acties.maakBackup(onMelding)) onMelding("Opslaan lukt hier niet.")
                })
            }

            Kaart(
                titel = "♻️ Terugzetten",
                uitleg = if (zekerTerug) "Let op: alles wat nu in de app staat, wordt vervangen. Daarna start de app opnieuw."
                else "Een oude back-up terugzetten.",
            ) {
                if (!zekerTerug) {
                    GroteKnop("Back-up terugzetten", onClick = { zekerTerug = true })
                } else {
                    GroteKnop("Ja, kies het bestand", onClick = {
                        zekerTerug = false
                        if (!acties.zetBackupTerug(onMelding)) onMelding("Bestand kiezen lukt hier niet.")
                    })
                    GroteKnop("Nee, toch niet", onClick = { zekerTerug = false })
                }
            }

            Kaart(
                titel = "🧭 Uit Kompas halen",
                uitleg = "Kies je kompas-backup-….json. Open taken en ideeën gaan naar Onthoud (groep Kompas), wensen naar Wensen. Twee keer inlezen geeft geen dubbele.",
            ) {
                GroteKnop("Kompas-bestand kiezen", onClick = {
                    if (!acties.leesKompas(onMelding)) onMelding("Bestand kiezen lukt hier niet.")
                })
            }
        }
    }
}

@Composable
private fun Kaart(titel: String, uitleg: String, knoppen: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tekst(titel, 22.sp)
        Tekst(uitleg, 17.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        knoppen()
    }
}

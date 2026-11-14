package nl.odelia.dashboard.ui.vertalen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.Vertalen
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.theme.Kleur

/** Vertalen: tekst typen, plakken of inspreken, taal kiezen, en de vraag klaarzetten in de Claude-app. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VertalenScherm(
    start: Scherm.Vertalen,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tekst by rememberSaveable { mutableStateOf(start.tekst.take(Vertalen.MAX)) }
    var taal by rememberSaveable { mutableStateOf(Vertalen.Taal.Auto) }
    var soort by rememberSaveable { mutableStateOf(Vertalen.Soort.Tekst) }
    var uitleg by rememberSaveable { mutableStateOf(false) }
    val klembord = LocalClipboardManager.current

    fun plak() {
        val geplakt = klembord.getText()?.text.orEmpty().trim()
        if (geplakt.isEmpty()) onMelding("Er staat niks op het klembord.") else tekst = geplakt.take(Vertalen.MAX)
    }

    fun spreekIn() {
        val gelukt = acties.inspreken { gehoord -> tekst = if (tekst.isBlank()) gehoord else "$tekst $gehoord" }
        if (!gelukt) onMelding("Inspreken lukt niet op deze telefoon.")
    }

    LaunchedEffect(Unit) {
        if (start.inspreken) spreekIn()
        if (start.plak) plak()
    }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk("Vertalen", onTerug)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Vertalen.Soort.entries.forEach { s -> Keuze(s.label, aan = soort == s, onClick = { soort = s }, Modifier.weight(1f)) }
            }

            Invoer(tekst, onWijzig = { tekst = it.take(Vertalen.MAX) }, hint = soort.hint, regels = 4)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ZachteKnop("📋 Plakken", onClick = ::plak, Modifier.weight(1f))
                ZachteKnop("🎤 Inspreken", onClick = ::spreekIn, Modifier.weight(1f))
                ZachteKnop("✕ Leeg", onClick = { tekst = "" }, Modifier.weight(1f))
            }

            Tekst("Naar welke taal?", 17.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Vertalen.Taal.entries.forEach { t -> Keuze(t.label, aan = taal == t, onClick = { taal = t }) }
            }
            Keuze("💡 Ook lastige woorden uitleggen", aan = uitleg, onClick = { uitleg = !uitleg }, Modifier.fillMaxWidth())

            GroteKnop("Vertaal met Claude", icoon = Icoontjes.mailWit, onClick = {
                val vraag = Vertalen.vraag(tekst, taal, uitleg, soort)
                when {
                    vraag == null -> onMelding("Typ of plak eerst een tekst.")
                    !acties.vraagClaude(vraag) -> onMelding("De Claude-app is niet gevonden.")
                }
            })

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Kleur.Kaart.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Tekst("ℹ️ Zo werkt het", 15.sp, kleur = Kleur.InktZacht)
                Tekst(
                    "Claude opent met je tekst al klaar. Jij drukt zelf op versturen. " +
                        "Zonder jouw tik gaat er niks naar Claude.",
                    14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs, regel = 20.sp,
                )
            }
        }
    }
}

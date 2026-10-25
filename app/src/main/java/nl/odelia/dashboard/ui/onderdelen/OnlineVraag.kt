package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
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
import nl.odelia.dashboard.ui.theme.Kleur

/** Iets dat online moet: eerst vragen, pas bij "ja" doen. */
class OnlineVraag(val titel: String, val uitleg: String, val ja: String, val doe: () -> Unit)

/** Houdt bij welke vraag er open staat. Gebruik: `vraag.stel(OnlineVraag(...))` en zet `vraag.Toon()` in het scherm. */
class VraagHouder {
    var open by mutableStateOf<OnlineVraag?>(null)
        private set

    fun stel(v: OnlineVraag) {
        open = v
    }

    @Composable
    fun Toon() {
        val v = open ?: return
        Venster(
            titel = v.titel,
            onSluit = { open = null },
            onKlaar = {
                open = null
                v.doe()
            },
            klaarTekst = v.ja,
        ) {
            Column(Modifier.widthIn(max = 340.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Tekst("🌐", 48.sp)
                Tekst(v.uitleg, 17.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht, uitlijning = TextAlign.Center)
            }
        }
    }
}

@Composable
fun rememberVraag(): VraagHouder = remember { VraagHouder() }

/** Vraag voor iets opzoeken in Chrome. */
fun opzoekVraag(term: String, doe: () -> Unit) = OnlineVraag(
    titel = "Opzoeken op internet?",
    uitleg = "Chrome gaat open en zoekt naar:\n\"${term.trim().take(80)}\"\nDe app zelf gaat niet online.",
    ja = "Ja, zoek",
    doe = doe,
)

/** Vraag voor een route in Google Maps. */
fun routeVraag(plek: String, doe: () -> Unit) = OnlineVraag(
    titel = "Route opzoeken?",
    uitleg = "Google Maps gaat open met:\n\"${plek.trim().take(80)}\"\nDe app zelf gaat niet online.",
    ja = "Ja, open Maps",
    doe = doe,
)

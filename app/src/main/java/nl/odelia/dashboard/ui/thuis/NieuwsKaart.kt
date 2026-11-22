package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import nl.odelia.dashboard.logica.Nieuws
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/**
 * 📰 Nieuws (bouwnacht 2): alleen de koppen van de NOS, feitelijk, zonder mening van de app.
 * De app gaat pas online als Odelia op de knop tikt (net als bij Zeilweer).
 */
@Composable
fun NieuwsKaart(acties: AppActies) {
    val scope = rememberCoroutineScope()
    var koppen by remember { mutableStateOf<List<Nieuws.Kop>?>(null) }
    var bezig by remember { mutableStateOf(false) }
    var dicht by rememberSaveable { mutableStateOf(false) }
    if (dicht) return
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Color(0xFFE5EEF8), RoundedCornerShape(26.dp), Kleur.KaartSchaduw, hoogte = 8.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tekst("📰 Nieuws uit de wereld", 18.sp, kleur = Kleur.Inkt)
        val k = koppen
        when {
            k == null -> {
                Tekst("Wil je de koppen van de NOS zien? Dan gaat de app even online (alleen naar de NOS).", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
                ZachteKnop(if (bezig) "Even wachten…" else "Ja, ophalen", {
                    if (!bezig) {
                        bezig = true
                        scope.launch { koppen = acties.haalNieuws(); bezig = false }
                    }
                })
            }
            k.isEmpty() -> Tekst("Ophalen lukte niet. Probeer het later nog eens.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
            else -> {
                k.forEach { kop ->
                    Tekst(
                        "• ${kop.titel}", 16.sp,
                        Modifier.clickable(enabled = kop.link != null) { kop.link?.let(acties::openLink) },
                        dikte = FontWeight.Bold, kleur = Kleur.Inkt,
                    )
                }
                ZachteKnop("🔊 Voorlezen", { acties.voorlezen(k.joinToString(". ") { it.titel }) })
            }
        }
        ZachteKnop("Verberg", { dicht = true })
    }
}

package nl.odelia.dashboard.ui.leren

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import nl.odelia.dashboard.logica.Kaart
import nl.odelia.dashboard.logica.Leren
import nl.odelia.dashboard.logica.LerenData
import nl.odelia.dashboard.logica.LerenInhoud
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Kopje
import nl.odelia.dashboard.ui.onderdelen.MicKnop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.theme.Kleur

private val Bruin = Color(0xFF5A3426)
private val Grijs = Color(0xFF8A6A5A)

/** Zelf kaarten maken: typen of inspreken. Ze komen bij het gekozen onderwerp. */
@Composable
internal fun MakenDeel(data: LerenData, onderwerp: String, acties: AppActies, onMelding: (String) -> Unit) {
    var vraag by remember(onderwerp) { mutableStateOf("") }
    var antwoord by remember(onderwerp) { mutableStateOf("") }
    val naam = LerenInhoud.onderwerpen.firstOrNull { it.id == onderwerp }?.naam ?: onderwerp

    fun spreekIn(klaar: (String) -> Unit) {
        if (!acties.inspreken(klaar)) onMelding("Inspreken lukt niet op deze telefoon.")
    }

    LKaart {
        Tekst("✏️ Zelf een kaart maken", 20.sp, kleur = Bruin)
        Tekst("Voor: $naam. Typ of spreek in met 🎤.", 15.sp, dikte = FontWeight.Bold, kleur = Grijs)
        Kopje("❓ Vraag")
        Invoer(vraag, { vraag = it.take(300) }, hint = "Bijv. Wat is een bewijsstuk?", regels = 2) {
            MicKnop({ spreekIn { vraag = it.take(300) } }, maat = 48.dp)
        }
        Kopje("💡 Antwoord")
        Invoer(antwoord, { antwoord = it.take(600) }, hint = "Het antwoord", regels = 3) {
            MicKnop({ spreekIn { antwoord = it.take(600) } }, maat = 48.dp)
        }
    }
    GroteKnop("Kaart bewaren", {
        if (vraag.isBlank() || antwoord.isBlank()) {
            onMelding("Vul een vraag en een antwoord in.")
        } else {
            val kaart = Kaart.nieuw(onderwerp, vraag, antwoord)
            acties.wijzigLeren { Leren.metEigen(it, kaart) }
            vraag = ""
            antwoord = ""
            onMelding("Kaart bewaard ✓")
        }
    }, icoon = Icoontjes.vinkje)

    val mijn = data.eigen.filter { it.onderwerp == onderwerp }
    LKaart {
        Tekst("🗂️ Jouw kaarten bij $naam", 18.sp, kleur = Bruin)
        if (mijn.isEmpty()) {
            Tekst("Nog geen eigen kaarten.", 16.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }
        mijn.forEach { k -> key(k.id) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Tekst(k.vraag, 16.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt)
                WisKnop { acties.wijzigLeren { Leren.zonderEigen(it, k.id) } }
            }
        } }
    }

    LKaart(Color(0xFFEFE7FB)) {
        Tekst("💬 Uitleg van Claude komt later", 17.sp, kleur = Color(0xFF3E2F66))
        Tekst(
            "Claude laten uitleggen of kaarten laten maken kan nog niet in de app. Daar is internet en een veilige sleutel voor nodig. Op de Leerplek-pagina werkt het wel.",
            14.sp, dikte = FontWeight.Bold, kleur = Color(0xFF5E4A8A),
        )
    }
}

/** Wissen in twee tikken: eerst "Zeker?", zodat je niet per ongeluk wist. */
@Composable
private fun WisKnop(onWis: () -> Unit) {
    var zeker by remember { mutableStateOf(false) }
    LaunchedEffect(zeker) {
        if (zeker) {
            delay(3_000)
            zeker = false
        }
    }
    ZachteKnop(if (zeker) "Zeker?" else "Wis", {
        if (zeker) onWis() else zeker = true
    }, kleur = Kleur.KoraalDiep)
}

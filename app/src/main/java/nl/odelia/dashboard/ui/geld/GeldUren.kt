package nl.odelia.dashboard.ui.geld

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.MaandCheck
import nl.odelia.dashboard.logica.UrenStats
import nl.odelia.dashboard.logica.alsBreuk
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.Tekst

/**
 * Kaart "Uren en loon" (nov 2026): uren per maand, en de maandcheck uren × uurloon naast het geboekte loon.
 * De uren en het uurloon staan alleen op dit apparaat en gaan nooit online (zie `logica/MaandCheck.kt`).
 */
@Composable
internal fun UrenKaart(staat: AppStaat, acties: AppActies) {
    val dezeMaand = YearMonth.from(staat.nu)
    var maand by remember { mutableStateOf(dezeMaand) }
    val dagen = remember(staat.perioden, staat.soorten) { UrenStats.dagen(staat.perioden, staat.soorten, staat.nu) }
    val u = MaandCheck.van(dagen, staat.geld.boekingen, maand, staat.uurloonCent)

    var tekst by remember { mutableStateOf("") }
    LaunchedEffect(staat.uurloonCent) {
        if (Geld.leesBedrag(tekst)?.takeIf { it > 0 } != staat.uurloonCent) tekst = staat.uurloonCent?.let(::invulTekst) ?: ""
    }

    Paneel {
        GeldKopje("⏱️ Uren en loon")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            MaandPijl("‹", "Vorige maand") { maand = maand.minusMonths(1) }
            Tekst(UrenStats.naam(maand), 18.sp, Modifier.weight(1f), kleur = Bruin, uitlijning = TextAlign.Center)
            if (maand < dezeMaand) MaandPijl("›", "Volgende maand") { maand = maand.plusMonths(1) } else Box(Modifier.size(48.dp))
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Tekst("${alsBreuk(u.uren)} uur", 34.sp, kleur = Bruin, regel = 38.sp)
            Tekst(if (u.dagen == 1) "1 dag gewerkt" else "${u.dagen} dagen gewerkt", 15.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
        }

        Tekst("Mijn uurloon", 15.sp, dikte = FontWeight.ExtraBold, kleur = BruinZacht)
        BedragVeld(
            tekst = tekst,
            onWijzig = { nieuw ->
                tekst = nieuw
                if (nieuw.isEmpty()) acties.zetUurloon(null) else Geld.leesBedrag(nieuw)?.let { acties.zetUurloon(it.takeIf { c -> c > 0 }) }
            },
            uitleg = "Uurloon in euro",
            maat = 26.sp,
            breed = 120.dp,
            rechts = { Tekst("per uur", 15.sp, dikte = FontWeight.ExtraBold, kleur = Grijs) },
        )

        u.verwachtCent?.let { verwacht ->
            WitteRegel {
                Tekst("Uren × uurloon", 15.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = BruinZacht)
                Tekst(Geld.bedrag(verwacht), 17.sp, kleur = Bruin)
            }
            WitteRegel {
                Tekst("Loon geboekt", 15.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = BruinZacht)
                Tekst(Geld.bedrag(u.loonCent), 17.sp, kleur = Bruin)
            }
        }
        Tekst(
            MaandCheck.zin(u), 16.sp, dikte = FontWeight.ExtraBold,
            kleur = if (u.oordeel == MaandCheck.Oordeel.Minder) Color(0xFFC2453A) else if (u.oordeel == MaandCheck.Oordeel.Klopt) GroenDonker else BruinZacht,
        )
        if (u.verwachtCent != null) {
            Tekst(
                "Let op: dit is een schatting. Van je loon gaat nog belasting af, en loon komt soms een week later.",
                13.sp, dikte = FontWeight.Bold, kleur = Grijs,
            )
        }
        Tekst("🔒 Uren en uurloon staan alleen op dit apparaat. Ze gaan nooit online.", 13.sp, dikte = FontWeight.ExtraBold, kleur = GroenDonker)
    }
}

@Composable
private fun MaandPijl(teken: String, uitleg: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = uitleg },
        contentAlignment = Alignment.Center,
    ) { Tekst(teken, 28.sp, kleur = Bruin) }
}

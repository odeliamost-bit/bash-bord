package nl.odelia.dashboard.ui.onthoud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.onthoud.Persoon
import nl.odelia.dashboard.onthoud.Wanneer
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.IcoonBlok
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.tik
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels
import nl.odelia.dashboard.ui.thuis.dagKort

/** Rond gekleurd rondje met de eerste letter van iemands naam. */
@Composable
fun PersoonRondje(persoon: Persoon, maat: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier.size(maat).clip(CircleShape).background(persoonKleur(persoon.kleur)),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(persoon.letter, (maat.value * 0.4f).sp, kleur = Color.White)
    }
}

/** Tekst en kleur van het pilletje rechts: "vandaag", "morgen", "ma\n26", "te laat". */
fun wanneerPil(a: Afspraak, staat: OnthoudStaat): Pair<String, Pastel>? {
    val d = a.deadline ?: return null
    return when (staat.wanneer(a)) {
        Wanneer.Klaar -> "klaar" to Pastels.Salie
        Wanneer.TeLaat -> "te laat" to Pastels.Roze
        Wanneer.Vandaag -> "vandaag" to Pastels.Geel
        Wanneer.Morgen -> "morgen" to Pastels.Abrikoos
        else -> datumPil(d) to Pastels.Blauw
    }
}

fun datumPil(d: LocalDate) = "${dagKort(d.dayOfWeek)}\n${d.dayOfMonth}"

/** "To-do · 15 min · Dhr. Jansen · PVB" (of de groep als er geen persoon is). Zonder [metPersoon]: alleen project of groep. */
fun onderregel(a: Afspraak, staat: OnthoudStaat, metPersoon: Boolean = true): String {
    val p = staat.persoon(a.persoonId)?.takeIf { metPersoon }
    val todo = if (a.todo) "✓ To-do" else null
    return listOfNotNull(todo, a.duur?.label, p?.naam, a.project, if (p == null && metPersoon) staat.groep(a)?.let(::groepTekst) else null)
        .joinToString(" · ")
        .ifEmpty { a.notitie.take(40) }
}

/**
 * Eén afspraak in een lijst. [onKlaar] gegeven = rond vinkje links (tik = klaar),
 * anders een gekleurd blokje zoals op het hoofdscherm.
 */
@Composable
fun AfspraakRegel(
    a: Afspraak,
    staat: OnthoudStaat,
    onOpen: () -> Unit,
    onKlaar: ((Boolean) -> Unit)? = null,
    metPersoon: Boolean = true,
) {
    Row(
        Modifier.fillMaxWidth().alpha(if (a.klaar) 0.6f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onKlaar != null) {
            Vinkje(a.klaar) { onKlaar(!a.klaar) }
        } else {
            val p = soortPastel(a.soort)
            IcoonBlok(p.licht, p.donker, lijstIcoon(a.soort), icoonMaat = 26.dp)
        }
        Row(
            Modifier.weight(1f).tik(onClick = onOpen).heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Tekst(a.wat, 17.sp)
                val onder = onderregel(a, staat, metPersoon)
                if (onder.isNotEmpty()) Tekst(onder, 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
            }
            wanneerPil(a, staat)?.let { (tekst, p) ->
                Tekst(
                    tekst,
                    14.sp,
                    Modifier
                        .background(p.kaart, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    kleur = p.label,
                    regel = 16.sp,
                    uitlijning = TextAlign.Center,
                )
            }
        }
    }
}

/** Rond vinkje: wit met randje, groen met ✓ als het klaar is. */
@Composable
fun Vinkje(aan: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .tik(onClick = onClick)
            .size(48.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(if (aan) Color(0xFF6FAE84) else Color.White)
            .border(3.dp, if (aan) Color(0xFF6FAE84) else Color(0xFFE3C3B0), CircleShape)
            .semantics { contentDescription = if (aan) "Klaar. Tik om terug te zetten" else "Tik als het klaar is" },
        contentAlignment = Alignment.Center,
    ) {
        if (aan) Icoon(Icoontjes.vinkje, 22.dp)
    }
}

package nl.odelia.dashboard.ui.onthoud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.korteDatum
import nl.odelia.dashboard.ui.onderdelen.IcoonBlok
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels

/** Kleur bij een datum: te laat roze, vandaag geel, morgen koraal, later lila, geen datum blauw. */
fun kleurBij(wanneer: LocalDate?, vandaag: LocalDate): Pastel = when {
    wanneer == null -> Pastels.Blauw
    wanneer.isBefore(vandaag) -> Pastels.Roze
    wanneer == vandaag -> Pastels.Geel
    wanneer == vandaag.plusDays(1) -> Pastels.Koraal
    else -> Pastels.Lila
}

/** "vandaag", "morgen", "ma\n26" */
fun wanneerTekst(wanneer: LocalDate?, vandaag: LocalDate): String = when (wanneer) {
    null -> "–"
    vandaag -> "vandaag"
    vandaag.plusDays(1) -> "morgen"
    else -> korteDatum(wanneer).replace(" ", "\n")
}

fun icoonBij(wat: String?): List<Vorm> = when (wat) {
    "Inleveren", "Maken" -> Icoontjes.document
    "Bellen" -> Icoontjes.telefoon
    "Mailen" -> Icoontjes.mail
    "Vragen" -> Icoontjes.praatwolk
    else -> Icoontjes.iemand
}

/** Eén afspraak: icoontje, wat, wie, datum. Met [onVink] kun je hem afvinken. */
@Composable
fun OnthoudRegel(
    o: Onthoud,
    vandaag: LocalDate,
    onKlik: () -> Unit,
    onVink: ((Boolean) -> Unit)? = null,
    wit: Boolean = false,
) {
    val p = kleurBij(o.wanneer, vandaag)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .then(if (wit) Modifier.background(Color.White) else Modifier)
            .clickable(onClickLabel = "Aanpassen", onClick = onKlik)
            .padding(horizontal = if (wit) 10.dp else 0.dp, vertical = if (wit) 8.dp else 2.dp)
            .alpha(if (o.klaar) 0.55f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onVink != null) {
            Box(
                Modifier
                    .size(48.dp)
                    .toggleable(value = o.klaar, role = Role.Checkbox, onValueChange = onVink)
                    .semantics { contentDescription = if (o.klaar) "Klaar" else "Nog doen" },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .background(if (o.klaar) Color(0xFF6FAE84) else Color.White, CircleShape)
                        .border(3.dp, if (o.klaar) Color(0xFF6FAE84) else Color(0xFFE3C3B0), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (o.klaar) Icoon(Icoontjes.vinkje, 18.dp)
                }
            }
        } else {
            IcoonBlok(p.licht, p.donker, icoonBij(o.wat), icoonMaat = 26.dp)
        }
        Column(Modifier.weight(1f)) {
            Tekst(o.titel, 17.sp)
            val onder = listOfNotNull(o.wie, o.groep, o.waar, if (o.foto != null) "📷" else null).joinToString(" · ")
            if (onder.isNotBlank()) Tekst(onder, 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        }
        if (o.wanneer != null) {
            Tekst(
                if (o.klaar) "klaar" else wanneerTekst(o.wanneer, vandaag),
                14.sp,
                Modifier
                    .background(p.kaart, RoundedCornerShape(14.dp))
                    .heightIn(min = 32.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                kleur = p.label,
                regel = 16.sp,
                uitlijning = TextAlign.Center,
            )
        }
    }
}

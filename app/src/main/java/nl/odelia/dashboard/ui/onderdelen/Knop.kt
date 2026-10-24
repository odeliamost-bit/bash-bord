package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.theme.Kleur

/**
 * Knop in klei-stijl, kleiner dan de grote koraal knop.
 * [hoofd] = koraal (de belangrijkste keuze), anders crème. [smal] = minder ruimte naast de tekst.
 */
@Composable
fun Knop(
    tekst: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hoofd: Boolean = false,
    icoon: List<Vorm>? = null,
    smal: Boolean = false,
) {
    val vorm = RoundedCornerShape(20.dp)
    val tekstKleur = if (hoofd) Color.White else Kleur.InktZacht
    Row(
        modifier
            .tik(onClick = onClick)
            .heightIn(min = 52.dp)
            .then(if (hoofd) Modifier.voet(Kleur.KoraalDiep, vorm, 4.dp) else Modifier)
            .shadow(6.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .clip(vorm)
            .background(if (hoofd) hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal) else hoekVerloop(Kleur.Kaart, Kleur.KaartPil))
            .glans(vorm, licht = if (hoofd) 0.4f else 0.9f, donker = 0.06f)
            .border(1.dp, Color.White.copy(alpha = 0.6f), vorm)
            .padding(horizontal = if (smal) 4.dp else 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        if (icoon != null) Icoon(icoon, 22.dp)
        Tekst(tekst, 16.sp, kleur = tekstKleur, uitlijning = TextAlign.Center, eenRegel = smal)
    }
}

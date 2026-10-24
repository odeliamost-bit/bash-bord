package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

/** Korte schrijfwijze voor tekst in Nunito. Standaard extra dik (900). */
@Composable
fun Tekst(
    tekst: String,
    maat: TextUnit,
    modifier: Modifier = Modifier,
    dikte: FontWeight = FontWeight.Black,
    kleur: Color = Color.Unspecified,
    regel: TextUnit = TextUnit.Unspecified,
    uitlijning: TextAlign? = null,
    eenRegel: Boolean = false,
) {
    Text(
        text = tekst,
        modifier = modifier,
        color = kleur,
        fontSize = maat,
        fontWeight = dikte,
        lineHeight = regel,
        textAlign = uitlijning,
        softWrap = !eenRegel,
        maxLines = if (eenRegel) 1 else Int.MAX_VALUE,
    )
}

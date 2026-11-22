package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.Staaf

/** Simpele staafjes-grafiek (bouwnacht 2): getal boven elk staafje, label eronder. */
@Composable
fun Staafjes(
    staven: List<Staaf>,
    kleur: Color,
    getal: (Float) -> String,
    modifier: Modifier = Modifier,
    hoogte: Dp = 120.dp,
    tekstKleur: Color = Color(0xFF5A3426),
) {
    val max = staven.maxOfOrNull { it.waarde }?.takeIf { it > 0f } ?: 1f
    Row(
        modifier.fillMaxWidth().semantics { contentDescription = staven.joinToString { "${it.label}: ${getal(it.waarde)}" } },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        staven.forEach { s ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                if (s.waarde > 0f) Tekst(getal(s.waarde), 11.sp, dikte = FontWeight.Bold, kleur = tekstKleur, uitlijning = TextAlign.Center)
                Box(Modifier.fillMaxWidth().height(hoogte), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight((s.waarde / max).coerceIn(0.02f, 1f))
                            .background(kleur, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    )
                }
                Tekst(s.label, 12.sp, dikte = FontWeight.Bold, kleur = tekstKleur, uitlijning = TextAlign.Center)
            }
        }
    }
}

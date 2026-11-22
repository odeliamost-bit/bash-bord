package nl.odelia.dashboard.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/** Tegelscherm van een groep uit de zijbalk: tik op een tegel om erheen te gaan. */
@Composable
fun GroepScherm(
    groep: Groep,
    verborgen: Set<String>,
    onTerug: () -> Unit,
    onPlek: (Plek) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk(groep.label, onTerug)
            groep.zichtbaar(verborgen).forEach { plek -> GroepTegel(plek) { onPlek(plek) } }
        }
    }
}

@Composable
private fun GroepTegel(plek: Plek, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(26.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, vorm, Kleur.KaartSchaduw, hoogte = 10.dp)
            .clip(vorm)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MenuIcoon(plek, 48.dp)
        Column {
            Tekst(plek.label, 20.sp)
            Tekst(plek.onderschrift(), 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        }
    }
}

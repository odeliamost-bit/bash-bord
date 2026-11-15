package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.NuDoen
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Pastels

/**
 * "Nu doen" (uit Kompas): één kaart met het belangrijkste ding van nu.
 * Koraal = te laat, geel = vandaag. Tik op de kaart = naar de lijst van Onthoud.
 */
@Composable
internal fun NuDoenKaart(nu: NuDoen, onOpen: () -> Unit, onKlaar: () -> Unit, onLees: () -> Unit) {
    val p = if (nu.teLaat) Pastels.Koraal else Pastels.Geel
    val vorm = RoundedCornerShape(26.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .klei(p.kaart, vorm, p.donker)
            .clip(vorm)
            .clickable(onClickLabel = "Open Onthoud", onClick = onOpen)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tekst("👉", 24.sp)
            Column(Modifier.weight(1f)) {
                Tekst("Nu doen", 15.sp, dikte = FontWeight.ExtraBold, kleur = p.label)
                Tekst(nu.wanneerTekst, 14.sp, dikte = FontWeight.ExtraBold, kleur = p.klein)
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f))
                    .clickable(onClick = onLees)
                    .semantics { contentDescription = "Voorlezen" },
                contentAlignment = Alignment.Center,
            ) { Tekst("🔊", 20.sp) }
        }
        Tekst(nu.item.titel, 22.sp, kleur = p.groot, regel = 26.sp)
        if (nu.daarna > 0) Tekst("daarna nog ${nu.daarna}", 14.sp, dikte = FontWeight.ExtraBold, kleur = p.klein)
        ZachteKnop("✅ Klaar", onClick = onKlaar, Modifier.fillMaxWidth())
    }
}

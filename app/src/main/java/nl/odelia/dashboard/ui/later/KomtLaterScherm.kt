package nl.odelia.dashboard.ui.later

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.menu.PlekRondje
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.tik
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.GroteKnop

/** Tijdelijk scherm voor alles wat na fase 1 komt (Geld, Leren, ...). */
@Composable
fun KomtLaterScherm(plek: Plek, onTerug: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .tik(onClick = onTerug)
                        .size(48.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                        .clip(CircleShape)
                        .background(Kleur.Kaart)
                        .semantics { contentDescription = "Terug" },
                    contentAlignment = Alignment.Center,
                ) {
                    Icoon(Icoontjes.terug, 24.dp)
                }
                Tekst(plek.label, 28.sp)
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlekRondje(plek, 96.dp, Modifier.padding(top = 4.dp, bottom = 6.dp))
                Tekst("Komt later", 24.sp)
                Tekst(
                    // Vaste spatie: "fase 1" blijft bij elkaar op één regel.
                    "Dit scherm bouwen we na fase\u00A01.",
                    17.sp,
                    dikte = FontWeight.Bold,
                    kleur = Kleur.InktGrijs,
                    uitlijning = TextAlign.Center,
                )
            }

            GroteKnop("Naar Thuis", onClick = onTerug, icoon = Icoontjes.huis)
        }
    }
}

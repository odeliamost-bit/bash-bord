package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.MicKnop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onthoud.OnthoudRegel
import nl.odelia.dashboard.ui.theme.Kleur

/** Tabje "Onthoud": de eerstvolgende afspraken en de knoppen om iets vast te leggen. */
@Composable
fun OnthoudTab(staat: AppStaat, acties: AppActies, onGaNaar: (Scherm) -> Unit) {
    val vandaag = staat.nu.toLocalDate()
    val open = staat.onthoud.filter { !it.klaar }
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Tekst("Onthoud", 20.sp, Modifier.weight(1f))
                Tekst(
                    "alles",
                    14.sp,
                    Modifier
                        .clip(CircleShape)
                        .background(Kleur.KaartPil)
                        .glans(CircleShape, licht = 0.8f, donker = 0f, boven = 2.dp)
                        .clickable { onGaNaar(Scherm.Lijst()) }
                        .heightIn(min = 36.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    dikte = FontWeight.ExtraBold,
                    kleur = Kleur.InktZacht,
                )
            }
            if (open.isEmpty()) {
                Tekst("Nog niks om te onthouden. Tik hieronder op Iets onthouden, of op 🎤.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
            }
            open.take(4).forEach { o ->
                OnthoudRegel(o, vandaag, onKlik = { onGaNaar(Scherm.Toevoegen(id = o.id)) })
            }
            if (open.size > 4) {
                Tekst("en nog ${open.size - 4} meer …", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GroteKnop("Iets onthouden", onClick = { onGaNaar(Scherm.Toevoegen()) }, Modifier.weight(1f))
            MicKnop(onClick = { onGaNaar(Scherm.Toevoegen(inspreken = true)) }, maat = 60.dp)
        }
    }
}

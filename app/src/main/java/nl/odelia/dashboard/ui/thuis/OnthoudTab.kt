package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.RondKnopje
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.tik
import nl.odelia.dashboard.ui.onderdelen.voet
import nl.odelia.dashboard.ui.onthoud.AfspraakRegel
import nl.odelia.dashboard.ui.theme.Kleur

/** Tabje "Onthoud": de eerstvolgende afspraken, en knoppen om iets nieuws te onthouden. */
@Composable
fun OnthoudTab(
    onthoud: OnthoudStaat,
    onOpen: (Afspraak) -> Unit,
    onAlles: () -> Unit,
    onNieuw: () -> Unit,
    onInspreken: () -> Unit,
) {
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
                        .tik(onClick = onAlles)
                        .heightIn(min = 36.dp)
                        .clip(CircleShape)
                        .background(Kleur.KaartPil)
                        .glans(CircleShape, licht = 0.8f, donker = 0f, boven = 2.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    dikte = FontWeight.ExtraBold,
                    kleur = Kleur.InktZacht,
                )
            }
            val eerst = onthoud.open.take(5)
            if (eerst.isEmpty()) {
                Tekst(
                    "Nog niets om te onthouden. Tik op \"Iets onthouden\" of op de 🎤.",
                    16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs,
                )
            }
            eerst.forEach { a -> AfspraakRegel(a, onthoud, onOpen = { onOpen(a) }) }
            val meer = onthoud.open.size - eerst.size
            if (meer > 0) Tekst("en nog $meer…", 15.sp, Modifier.tik(onClick = onAlles), dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GroteKnop("Iets onthouden", onClick = onNieuw, modifier = Modifier.weight(1f))
            RondKnopje(Icoontjes.microfoon, "Inspreken", onClick = onInspreken, maat = 60.dp)
        }
    }
}

/** Grote koraal knop met icoontje (standaard een plusje) en 3D-voetje. */
@Composable
fun GroteKnop(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier, icoon: List<Vorm> = Icoontjes.plus) {
    val vorm = RoundedCornerShape(24.dp)
    Row(
        modifier
            .tik(onClick = onClick)
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .voet(Kleur.KoraalDiep, vorm, 6.dp)
            .shadow(12.dp, vorm, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal)
            .clip(vorm)
            .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal))
            .glans(vorm, licht = 0.4f, donker = 0f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        Icoon(icoon, 24.dp)
        Tekst(tekst, 19.sp, kleur = Color.White)
    }
}

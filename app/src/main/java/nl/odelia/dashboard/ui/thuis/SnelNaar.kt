package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.onderdelen.IcoonBlok
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.tik
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels

/** Tegels onderaan het hoofdscherm: snel naar andere schermen. */
@Composable
fun SnelNaar(onGaNaar: (Plek) -> Unit, onMelding: (String) -> Unit) {
    val vorm = RoundedCornerShape(26.dp)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            Modifier
                .tik { onGaNaar(Plek.Advies) }
                .fillMaxWidth()
                .klei(Pastels.Abrikoos.kaart, vorm, Pastels.Abrikoos.donker, hoogte = 10.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IcoonBlok(Pastels.Abrikoos.licht, Pastels.Abrikoos.donker, Icoontjes.sleutel, maat = 56.dp, hoek = 18.dp, icoonMaat = 30.dp)
            Column {
                Tekst("Advies", 20.sp)
                Tekst("maken · repareren · vragen", 14.sp, dikte = FontWeight.Bold, kleur = Pastels.Abrikoos.label)
            }
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Tegel(Pastels.Lila, Icoontjes.praatwolk, "Mail-hulp", Modifier.weight(1f)) { onGaNaar(Plek.Mail) }
            Tegel(Pastels.Salie, Icoontjes.mensen, "Mensen", Modifier.weight(1f)) { onGaNaar(Plek.Mensen) }
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Tegel(Pastels.Blauw, Icoontjes.week, "Week", Modifier.weight(1f)) { onMelding("Het weekoverzicht komt later.") }
            Tegel(Pastels.Roze, Icoontjes.hart, "Wensen", Modifier.weight(1f)) { onGaNaar(Plek.Wensen) }
        }
    }
}

@Composable
private fun Tegel(p: Pastel, icoon: List<Vorm>, naam: String, modifier: Modifier, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(26.dp)
    Column(
        modifier
            .tik(onClick = onClick)
            .fillMaxHeight()
            .klei(p.kaart, vorm, p.donker, hoogte = 10.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IcoonBlok(p.licht, p.donker, icoon, maat = 52.dp, hoek = 17.dp)
        Tekst(naam, 18.sp)
    }
}

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.onderdelen.IcoonBlok
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.voet
import nl.odelia.dashboard.ui.theme.Kleur

/** Tabje "Onthoud": lijstje afspraken en de knop "Iets onthouden". */
@Composable
fun OnthoudTab(onMelding: (String) -> Unit) {
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
                        .clickable { onMelding("De hele lijst komt in stap 3.") }
                        .heightIn(min = 36.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    dikte = FontWeight.ExtraBold,
                    kleur = Kleur.InktZacht,
                )
            }
            Voorbeeld.afspraken.forEach { a ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IcoonBlok(a.kleur.licht, a.kleur.donker, a.icoon, icoonMaat = 26.dp)
                    Column(Modifier.weight(1f)) {
                        Tekst(a.wat, 17.sp)
                        Tekst(a.wie, 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                    }
                    Tekst(
                        a.wanneer,
                        14.sp,
                        Modifier
                            .background(a.kleur.kaart, RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        kleur = a.kleur.label,
                        regel = 16.sp,
                        uitlijning = TextAlign.Center,
                    )
                }
            }
        }

        GroteKnop("Iets onthouden", onClick = { onMelding("Snel vastleggen komt in stap 3.") })
    }
}

/** Grote koraal knop met plusje en 3D-voetje. */
@Composable
fun GroteKnop(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val vorm = RoundedCornerShape(24.dp)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .voet(Kleur.KoraalDiep, vorm, 6.dp)
            .shadow(12.dp, vorm, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal)
            .clip(vorm)
            .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal))
            .glans(vorm, licht = 0.4f, donker = 0f)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        Icoon(Icoontjes.plus, 24.dp)
        Tekst(tekst, 19.sp, kleur = Color.White)
    }
}

package nl.odelia.dashboard.ui.leren

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.onderdelen.IcoonBlok
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.rechthoek
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels

/** Waar een tegel op het Leren-tegelscherm heen gaat. */
enum class LerenTegel { Simulator, Kaarten, Quiz, Toetsen, Tutor, Maken, Formules, Overzicht }

private val wit = Kleur.Kaart
private val icoonBliksem = listOf(Vorm("M13 2L5 13.5h6L10 22l8-11.5h-6z", vul = wit))
private val icoonKaarten = listOf(Vorm(rechthoek(8f, 3f, 12f, 15f, 2f), vul = Color.White), Vorm(rechthoek(4f, 6f, 12f, 15f, 2f), vul = wit))
private val icoonVraag = listOf(Vorm("M9 9a3 3 0 1 1 4.2 2.8c-.8.4-1.2 1-1.2 1.9V15", lijn = wit, dikte = 2.6f), Vorm("M12 19.2h.01", lijn = wit, dikte = 3f))
private val icoonAgenda = listOf(Vorm(rechthoek(4f, 5f, 16f, 15f, 2.5f), vul = wit), Vorm("M8 3v4M16 3v4", lijn = wit, dikte = 2.4f))
private val icoonPraat = listOf(Vorm("M4 5h16v11H9l-5 4z", vul = wit))
private val icoonPotlood = listOf(Vorm("M4 20l1-5L16 4l4 4L9 19z", vul = wit))
private val icoonFormule = listOf(Vorm("M6 19c3 0 3-14 7-14M8 11h6M14 13l5 6M19 13l-5 6", lijn = wit, dikte = 2.4f))
private val icoonStaaf = listOf(Vorm("M5 20V12M12 20V6M19 20v-9", lijn = wit, dikte = 3f))

/** Geel voor de Simulator, net als het bliksem-icoontje in het ontwerp. */
private val Zon = Pastel(0xFFFBE7C3, 0xFFF7D27A, 0xFFE3A93E, 0xFF6E5410, 0xFF4E3B08, 0xFF8A6A10)

/** Leren als tegelscherm (gekozen door Odelia, okt 2026): kies wat je wilt doen. */
@Composable
fun LerenTegelsScherm(onTerug: () -> Unit, onKies: (LerenTegel) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TerugBalk("Leren", onTerug)
            BreedTegel(Zon, icoonBliksem, "Simulator", "schema's, groepenkast, aders", nieuw = true) { onKies(LerenTegel.Simulator) }
            Rij(
                { Tegel(Pastels.Koraal, icoonKaarten, "Kaarten", "oefenen", it) { onKies(LerenTegel.Kaarten) } },
                { Tegel(Pastels.Blauw, icoonVraag, "Quiz", "test jezelf", it) { onKies(LerenTegel.Quiz) } },
            )
            Rij(
                { Tegel(Pastels.Abrikoos, icoonAgenda, "Toetsen", "planning", it) { onKies(LerenTegel.Toetsen) } },
                { Tegel(Pastels.Roze, icoonPraat, "Tutor", "komt nog", it) { onKies(LerenTegel.Tutor) } },
            )
            Rij(
                { Tegel(Pastels.Lila, icoonPotlood, "Maken", "eigen kaarten", it) { onKies(LerenTegel.Maken) } },
                { Tegel(Pastels.Salie, icoonFormule, "Formules", "formulekaart", it) { onKies(LerenTegel.Formules) } },
            )
            BreedTegel(Pastels.Blauw, icoonStaaf, "Overzicht", "voortgang per vak") { onKies(LerenTegel.Overzicht) }
        }
    }
}

@Composable
private fun Rij(links: @Composable (Modifier) -> Unit, rechts: @Composable (Modifier) -> Unit) {
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        links(Modifier.weight(1f))
        rechts(Modifier.weight(1f))
    }
}

@Composable
private fun BreedTegel(p: Pastel, icoon: List<Vorm>, naam: String, sub: String, nieuw: Boolean = false, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(26.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .klei(p.kaart, vorm, p.donker, hoogte = 10.dp)
            .clip(vorm)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IcoonBlok(p.licht, p.donker, icoon, maat = 56.dp, hoek = 18.dp, icoonMaat = 30.dp)
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tekst(naam, 20.sp, kleur = p.groot)
                if (nieuw) {
                    Box(Modifier.klei(p.donker, RoundedCornerShape(8.dp), p.donker, hoogte = 0.dp).padding(horizontal = 7.dp, vertical = 2.dp)) {
                        Tekst("nieuw", 12.sp, kleur = Color.White)
                    }
                }
            }
            Tekst(sub, 14.sp, dikte = FontWeight.Bold, kleur = p.label)
        }
    }
}

@Composable
private fun Tegel(p: Pastel, icoon: List<Vorm>, naam: String, sub: String, modifier: Modifier, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(26.dp)
    Column(
        modifier
            .fillMaxHeight()
            .klei(p.kaart, vorm, p.donker, hoogte = 10.dp)
            .clip(vorm)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IcoonBlok(p.licht, p.donker, icoon, maat = 50.dp, hoek = 16.dp)
        Tekst(naam, 18.sp, kleur = p.groot)
        Tekst(sub, 13.sp, dikte = FontWeight.Bold, kleur = p.label)
    }
}

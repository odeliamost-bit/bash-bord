package nl.odelia.dashboard.ui.thuis

import nl.odelia.dashboard.logica.TegelSoort
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.onderdelen.IcoonBlok
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.OnlineVraag
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.rechthoek
import nl.odelia.dashboard.ui.onderdelen.rememberVraag
import nl.odelia.dashboard.ui.theme.Kleur
import androidx.compose.ui.graphics.Color
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels

/** Boeken en Simulator staan (nog) als pagina op claude.ai, zoals in het ontwerp. */
private const val BoekenLink = "https://claude.ai/code/artifact/e23f1beb-e479-44cd-83c7-2713d32d0692"
private const val SimulatorLink = "https://claude.ai/code/artifact/5e73741c-be08-4cc6-bf2e-f07182ff4db3"

private val Zon = Pastel(0xFFFBEBC4, 0xFFF7DA8A, 0xFFE5B54E, 0xFF6E5410, 0xFF4E3B08, 0xFF8A6A10)
private val Leerkleur = Pastel(0xFFFFF1B8, 0xFFF7DA8A, 0xFFD9A633, 0xFF6E5410, 0xFF4E3B08, 0xFF6E5410)
private val Turkoois = Pastel(0xFFD4EEF0, 0xFF9AD1D4, 0xFF5FA8AD, 0xFF2F5E62, 0xFF1F4A4E, 0xFF3F8A8F)

private val icoonBoeken = listOf(
    Vorm(rechthoek(4f, 4f, 5f, 16f, 1.2f), vul = Kleur.Kaart),
    Vorm(rechthoek(10.5f, 6f, 4.5f, 14f, 1.2f), vul = Color.White),
    Vorm("M16 7.5l3.2-.9 2.6 12.6-3.2.8z", vul = Kleur.Kaart),
)
private val icoonBliksem = listOf(Vorm("M13 2L5 13.5h6L10 22l8-11.5h-6z", vul = Kleur.Kaart))
private val icoonBoek = listOf(
    Vorm("M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2z", vul = Kleur.Kaart),
    Vorm("M4 21a2 2 0 0 1 2-2h13v2z", vul = Color.White),
)

/** Tegels onderaan het hoofdscherm (ontwerp/Main.dc.html): snel naar andere schermen. */
@Composable
fun SnelNaar(
    tegels: List<TegelSoort>,
    onGaNaar: (Scherm) -> Unit,
    onPlek: (Plek) -> Unit,
    onLink: (String) -> Unit = {},
) {
    val vorm = RoundedCornerShape(26.dp)
    val vraag = rememberVraag()
    fun openPagina(naam: String, link: String) = vraag.stel(
        OnlineVraag(
            titel = "$naam openen?",
            uitleg = "Chrome gaat open met je $naam-pagina op claude.ai.\nDe app zelf gaat niet online.",
            ja = "Ja, open",
            doe = { onLink(link) },
        ),
    )
    @Composable
    fun Klein(t: TegelSoort, modifier: Modifier) = when (t) {
        TegelSoort.Mail -> Tegel(Pastels.Lila, Icoontjes.praatwolk, t.naam, modifier) { onPlek(Plek.Mail) }
        TegelSoort.Mensen -> Tegel(Pastels.Salie, Icoontjes.mensen, t.naam, modifier) { onPlek(Plek.Mensen) }
        TegelSoort.Week -> Tegel(Pastels.Blauw, Icoontjes.week, t.naam, modifier) { onGaNaar(Scherm.Lijst()) }
        TegelSoort.Wensen -> Tegel(Pastels.Roze, Icoontjes.hart, t.naam, modifier) { onPlek(Plek.Wensen) }
        TegelSoort.Boeken -> Tegel(Zon, icoonBoeken, t.naam, modifier) { openPagina("Boeken", BoekenLink) }
        TegelSoort.Simulator -> Tegel(Turkoois, icoonBliksem, t.naam, modifier) { openPagina("Simulator", SimulatorLink) }
        TegelSoort.Advies, TegelSoort.Leren -> Unit
    }

    // Brede tegels krijgen een eigen rij; kleine tegels staan met twee naast elkaar.
    val rijen = mutableListOf<List<TegelSoort>>()
    tegels.forEach { t ->
        val laatste = rijen.lastOrNull()
        if (!t.breed && laatste != null && laatste.size == 1 && !laatste[0].breed) rijen[rijen.lastIndex] = laatste + t
        else rijen += listOf(t)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        rijen.forEach { rij ->
            val t = rij[0]
            when (t) {
                TegelSoort.Advies -> BreedTegel(Pastels.Abrikoos, Icoontjes.sleutel, t.naam, "maken · repareren · vragen") { onPlek(Plek.Advies) }
                TegelSoort.Leren -> BreedTegel(Leerkleur, icoonBoek, t.naam, "kaartjes · quiz · school") { onPlek(Plek.Leren) }
                else -> Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    rij.forEach { Klein(it, Modifier.weight(1f)) }
                    if (rij.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
    vraag.Toon()
}

@Composable
private fun BreedTegel(p: Pastel, icoon: List<Vorm>, naam: String, sub: String, onClick: () -> Unit) {
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
        Column {
            Tekst(naam, 20.sp)
            Tekst(sub, 14.sp, dikte = FontWeight.Bold, kleur = p.label)
        }
    }
}

@Composable
private fun Tegel(p: Pastel, icoon: List<Vorm>, naam: String, modifier: Modifier, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(26.dp)
    Column(
        modifier
            .fillMaxHeight()
            .klei(p.kaart, vorm, p.donker, hoogte = 10.dp)
            .clip(vorm)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IcoonBlok(p.licht, p.donker, icoon, maat = 52.dp, hoek = 17.dp)
        Tekst(naam, 18.sp)
    }
}

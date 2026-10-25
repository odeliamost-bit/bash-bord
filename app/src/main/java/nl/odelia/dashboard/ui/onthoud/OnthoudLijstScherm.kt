package nl.odelia.dashboard.ui.onthoud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.MicKnop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

private val persoonKleuren = listOf(
    Color(0xFFF6B3C8) to Color(0xFFDE7FA1), Color(0xFFD2BDF0) to Color(0xFFA88AD8),
    Color(0xFFAFCBEA) to Color(0xFF7FA5D2), Color(0xFFA8D6B4) to Color(0xFF6FAE84),
    Color(0xFFF8C29C) to Color(0xFFE9956A),
)

/**
 * Alles terugvinden (ontwerp/Mensen.dc.html): zoekbalk, groepjes, en per persoon wat je hebt afgesproken.
 * [perPersoon] = eerst een persoon kiezen; anders de hele lijst van de groep.
 */
@Composable
fun OnthoudLijstScherm(
    perPersoon: Boolean,
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onGaNaar: (Scherm) -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vandaag = staat.nu.toLocalDate()
    var zoek by remember { mutableStateOf("") }
    var groep by remember { mutableStateOf<String?>(null) }
    var persoon by remember { mutableStateOf<String?>(null) }
    val vink: (Onthoud) -> (Boolean) -> Unit = { o -> { klaar -> acties.bewaar(o.copy(klaar = klaar)) } }

    val groepen = staat.onthoud.mapNotNull { it.groep }.distinct().sorted()
    val inGroep = staat.onthoud.filter { groep == null || it.groep == groep }
    val mensen = inGroep.mapNotNull { it.wie }.distinct().sorted()
    val gekozenPersoon = persoon?.takeIf { it in mensen } ?: if (perPersoon) mensen.firstOrNull() else null

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk(if (perPersoon) "Mensen" else "Onthoud", onTerug)

            // Zoekbalk
            Invoer(zoek, onWijzig = { zoek = it }, hint = "Zoek: naam, opdracht, woord…") {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Icoon(Icoontjes.zoek, 24.dp) }
                MicKnop(onClick = {
                    if (!acties.inspreken { zoek = it.trim() }) onMelding("Inspreken lukt niet op deze telefoon.")
                }, maat = 44.dp)
            }

            if (zoek.isNotBlank()) {
                val gevonden = zoeken(staat.onthoud, zoek)
                Kaart {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Tekst("🔍 ${gevonden.size} gevonden", 19.sp, Modifier.weight(1f), kleur = Color(0xFF5A3426))
                        ZachteKnop("✕ wissen", onClick = { zoek = "" })
                    }
                    if (gevonden.isEmpty()) Tekst("Niks gevonden. Probeer een ander woord.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                    gevonden.forEach { o ->
                        OnthoudRegel(o, vandaag, onKlik = { onGaNaar(Scherm.Toevoegen(id = o.id)) }, onVink = vink(o), wit = true)
                    }
                }
            } else {
                // Groepjes
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Kleur.Kaart.copy(alpha = 0.7f), CircleShape)
                        .padding(6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (listOf<String?>(null) + groepen).forEach { g ->
                        GroepKnop(g ?: "Alles", groep == g, onClick = { groep = g; persoon = null })
                    }
                }

                if (perPersoon) {
                    // Personen als rondjes
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        mensen.forEachIndexed { i, naam ->
                            PersoonRondje(naam, i, naam == gekozenPersoon, onClick = { persoon = naam })
                        }
                        if (mensen.isEmpty()) {
                            Tekst("Nog geen namen. Kies bij iets onthouden bij 'Wie?' een naam.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                        }
                    }
                }

                val lijst = if (perPersoon) inGroep.filter { gekozenPersoon != null && it.wie == gekozenPersoon } else inGroep
                val nogDoen = lijst.count { !it.klaar }
                Kaart {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Tekst(gekozenPersoon ?: groep ?: "Alles", 22.sp, kleur = Color(0xFF5A3426), regel = 26.sp)
                            if (perPersoon) Tekst("🗣️ Wat hebben we afgesproken?", 15.sp, dikte = FontWeight.Bold, kleur = Color(0xFF8A6A5A))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Tekst("$nogDoen", 28.sp, kleur = Kleur.Koraal, regel = 30.sp)
                            Tekst("nog doen", 12.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF8A6A5A))
                        }
                    }
                    if (lijst.isEmpty()) Tekst("Hier staat nog niks.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                    lijst.sortedWith(compareBy<Onthoud> { it.klaar }.thenBy { it.wanneer == null }.thenBy { it.wanneer }).forEach { o ->
                        OnthoudRegel(o, vandaag, onKlik = { onGaNaar(Scherm.Toevoegen(id = o.id)) }, onVink = vink(o), wit = true)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GroteKnop(
                    if (perPersoon) "Nieuwe afspraak" else "Iets onthouden",
                    onClick = { onGaNaar(Scherm.Toevoegen(wie = gekozenPersoon, groep = groep)) },
                    Modifier.weight(1f),
                )
                MicKnop(onClick = { onGaNaar(Scherm.Toevoegen(inspreken = true, wie = gekozenPersoon, groep = groep)) }, maat = 60.dp)
            }
        }
    }
}

/** Zoekt in alles wat er bij een afspraak staat. */
fun zoeken(lijst: List<Onthoud>, zoek: String): List<Onthoud> {
    val woorden = zoek.lowercase().split(" ").filter { it.isNotBlank() }
    return lijst.filter { o ->
        val alles = listOfNotNull(o.tekst, o.wie, o.wat, o.waar, o.groep).joinToString(" ").lowercase()
        woorden.all { it in alles }
    }
}

@Composable
private fun Kaart(inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { inhoud() }
}

@Composable
private fun GroepKnop(tekst: String, aan: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .heightIn(min = 46.dp)
            .then(if (aan) Modifier.shadow(6.dp, CircleShape, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal) else Modifier)
            .clip(CircleShape)
            .then(if (aan) Modifier.background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal)) else Modifier)
            .selectable(selected = aan, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 16.sp, kleur = if (aan) Color.White else Color(0xFF7A4A38))
    }
}

@Composable
private fun PersoonRondje(naam: String, nummer: Int, aan: Boolean, onClick: () -> Unit) {
    val (licht, donker) = persoonKleuren[nummer % persoonKleuren.size]
    Column(
        Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = aan, role = Role.Tab, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(64.dp)
                .then(if (aan) Modifier.border(3.dp, Kleur.Koraal, CircleShape).padding(5.dp) else Modifier)
                .background(hoekVerloop(licht, donker), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Tekst(naam.trim().split(" ").last().firstOrNull()?.uppercase() ?: "?", 24.sp, kleur = Color.White)
        }
        Tekst(naam, 13.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF5A3426), uitlijning = TextAlign.Center, regel = 15.sp)
    }
}

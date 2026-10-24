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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.Groep
import nl.odelia.dashboard.onthoud.OnthoudActies
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.onthoud.Persoon
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.menu.PlekRondje
import nl.odelia.dashboard.ui.onderdelen.Invoerveld
import nl.odelia.dashboard.ui.onderdelen.Knop
import nl.odelia.dashboard.ui.onderdelen.SchermKop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.kies
import nl.odelia.dashboard.ui.onderdelen.tik
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.VoorleesKnop
import nl.odelia.dashboard.ui.thuis.datumKort
import nl.odelia.dashboard.ui.thuis.datumTekst

/** Mensen en wat je met ze afgesproken hebt (ontwerp/Mensen.dc.html). */
@Composable
fun MensenScherm(
    staat: OnthoudStaat,
    acties: OnthoudActies,
    onOpen: (Afspraak) -> Unit,
    onNieuweAfspraak: (persoonId: String) -> Unit,
    onVoorlezen: (String) -> Unit,
    onTerug: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var zoek by rememberSaveable { mutableStateOf("") }
    var groep by rememberSaveable { mutableStateOf(staat.personen.firstOrNull()?.groep ?: Groep.School) }
    var gekozen by rememberSaveable { mutableStateOf<String?>(null) }
    var nieuw by rememberSaveable { mutableStateOf(false) }

    val inGroep = staat.personen.filter { it.groep == groep }
    val persoon = inGroep.firstOrNull { it.id == gekozen } ?: inGroep.firstOrNull()

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SchermKop("Mensen", onTerug) { PlekRondje(Plek.Mensen, 52.dp) }
            Zoekbalk(zoek, { zoek = it }, acties)
            if (zoek.isNotBlank()) Gevonden(staat, staat.zoek(zoek), acties, onOpen, onWissen = { zoek = "" })

            GroepTabjes(groep) { groep = it; gekozen = null; nieuw = false }

            Row(Modifier.horizontalScroll(rememberScrollState()).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                inGroep.forEach { p ->
                    PersoonKnop(p, aan = p.id == persoon?.id) { gekozen = p.id; nieuw = false }
                }
                NieuwKnop { nieuw = !nieuw }
            }

            if (nieuw) {
                NieuwePersoonKaart(groep) { naam, rol ->
                    acties.nieuwePersoon(naam, groep, rol) { p -> gekozen = p.id }
                    nieuw = false
                }
            }

            when {
                persoon != null -> PersoonKaart(persoon, staat, acties, onOpen, onNieuweAfspraak, onVoorlezen)
                !nieuw -> LegeKaart("Nog niemand bij ${groepTekst(groep)}. Tik op + om iemand toe te voegen.")
            }
        }
    }
}

/** 🎒 School · 🏭 Werk · 🏠 Thuis */
@Composable
private fun GroepTabjes(gekozen: Groep, onKies: (Groep) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Kleur.Kaart.copy(alpha = 0.7f), CircleShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Groep.entries.forEach { g ->
            val aan = g == gekozen
            Box(
                Modifier
                    .weight(1f)
                    .kies(aan) { onKies(g) }
                    .heightIn(min = 48.dp)
                    .then(
                        if (aan) {
                            Modifier.shadow(4.dp, CircleShape, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal)
                                .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal), CircleShape)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Tekst(groepTekst(g), 15.sp, kleur = if (aan) Color.White else Color(0xFF7A4A38), eenRegel = true)
            }
        }
    }
}

@Composable
private fun PersoonKnop(p: Persoon, aan: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.width(76.dp).kies(aan, onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(72.dp)
                .border(3.dp, if (aan) Kleur.Koraal else Color.Transparent, CircleShape)
                .padding(5.dp),
            contentAlignment = Alignment.Center,
        ) {
            PersoonRondje(p, 60.dp, Modifier.shadow(6.dp, CircleShape))
        }
        Tekst(p.naam, 13.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF5A3426), uitlijning = TextAlign.Center, regel = 15.sp)
    }
}

@Composable
private fun NieuwKnop(onClick: () -> Unit) {
    Column(
        Modifier.width(76.dp).tik(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier.size(72.dp).padding(5.dp).border(3.dp, Color(0xFFD9A08A), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Tekst("+", 30.sp, kleur = Kleur.Koraal)
        }
        Tekst("nieuw", 13.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF8A5A48))
    }
}

@Composable
private fun NieuwePersoonKaart(groep: Groep, onKlaar: (naam: String, rol: String) -> Unit) {
    var naam by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf("") }
    Kaart {
        Tekst("Nieuw bij ${groepTekst(groep)}", 19.sp)
        Invoerveld(naam, { naam = it }, "Naam, bv. Mevr. de Boer", eenRegel = true)
        Invoerveld(rol, { rol = it }, "Wie is het? bv. mentor (mag leeg)", eenRegel = true)
        Knop("Toevoegen", onClick = { if (naam.isNotBlank()) onKlaar(naam, rol) }, modifier = Modifier.fillMaxWidth(), hoofd = true)
    }
}

/** Kaart van één persoon: wat er is afgesproken, met vinkjes. */
@Composable
private fun PersoonKaart(
    p: Persoon,
    staat: OnthoudStaat,
    acties: OnthoudActies,
    onOpen: (Afspraak) -> Unit,
    onNieuweAfspraak: (String) -> Unit,
    onVoorlezen: (String) -> Unit,
) {
    val lijst = staat.vanPersoon(p.id).sortedBy { it.klaar }
    val s = staat.samenvatting(lijst)
    Kaart {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PersoonRondje(p, 58.dp)
            Column(Modifier.weight(1f)) {
                Tekst(p.naam, 22.sp, kleur = Color(0xFF5A3426), regel = 24.sp)
                if (p.rol.isNotBlank()) Tekst(p.rol, 15.sp, dikte = FontWeight.Bold, kleur = Color(0xFF8A6A5A))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Tekst(s.open.toString(), 28.sp, kleur = Kleur.Koraal, regel = 30.sp)
                Tekst("nog doen", 12.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF8A6A5A))
            }
        }
        Tekst("🗣️ Wat hebben we afgesproken?", 17.sp, kleur = Color(0xFF5A3426))
        if (lijst.isEmpty()) {
            Tekst("Nog niets met ${p.naam} afgesproken.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        }
        lijst.forEach { a ->
            Column {
                AfspraakRegel(a, staat, onOpen = { onOpen(a) }, onKlaar = { acties.zetKlaar(a.id, it) }, metPersoon = false)
                Tekst(
                    "gezegd op ${datumKort(a.gemaakt.toLocalDate())}",
                    13.sp, Modifier.padding(start = 60.dp), dikte = FontWeight.Bold, kleur = Color(0xFFA8968A),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Knop("+ Afspraak", onClick = { onNieuweAfspraak(p.id) }, hoofd = true)
            val zin = buildString {
                append("${p.naam}. ")
                val open = lijst.filter { !it.klaar }
                if (open.isEmpty()) append("Niets meer te doen.")
                open.forEach { a -> append("${a.wat}${a.deadline?.let { ", " + datumTekst(it) } ?: ""}. ") }
            }
            VoorleesKnop({ onVoorlezen(zin) })
        }
    }
}

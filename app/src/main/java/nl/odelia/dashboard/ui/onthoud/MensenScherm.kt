package nl.odelia.dashboard.ui.onthoud

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoerveld
import nl.odelia.dashboard.ui.onderdelen.KeuzeBalk
import nl.odelia.dashboard.ui.onderdelen.Knop
import nl.odelia.dashboard.ui.onderdelen.SchermKop
import nl.odelia.dashboard.ui.onderdelen.Tekst
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
    // Meteen de aanpas-kaart open (voor voorbeelden)
    beginAanpassen: Boolean = false,
) {
    var zoek by rememberSaveable { mutableStateOf("") }
    var groep by rememberSaveable { mutableStateOf(staat.personen.firstOrNull()?.groep ?: Groep.School) }
    var gekozen by rememberSaveable { mutableStateOf<String?>(null) }
    var nieuw by rememberSaveable { mutableStateOf(false) }
    var aanpassen by rememberSaveable { mutableStateOf(beginAanpassen) }

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

            GroepTabjes(groep) { groep = it; gekozen = null; nieuw = false; aanpassen = false }

            Row(Modifier.horizontalScroll(rememberScrollState()).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                inGroep.forEach { p ->
                    PersoonKnop(p, aan = p.id == persoon?.id) { gekozen = p.id; nieuw = false; aanpassen = false }
                }
                NieuwKnop { nieuw = !nieuw; aanpassen = false }
            }

            if (nieuw) {
                PersoonInvullen("Nieuw bij ${groepTekst(groep)}", "Toevoegen", begin = null) { naam, rol, _ ->
                    acties.nieuwePersoon(naam, groep, rol) { p -> gekozen = p.id }
                    nieuw = false
                }
            }

            when {
                persoon != null && aanpassen -> key(persoon.id) {
                    PersoonInvullen(
                        "${persoon.naam} aanpassen", "Bewaren", begin = persoon,
                        onAnnuleer = { aanpassen = false },
                        onVerwijder = {
                            acties.verwijderPersoon(persoon.id)
                            gekozen = null
                            aanpassen = false
                        },
                    ) { naam, rol, nieuweGroep ->
                        acties.pasPersoonAan(persoon.copy(naam = naam.trim(), rol = rol.trim(), groep = nieuweGroep))
                        groep = nieuweGroep
                        gekozen = persoon.id
                        aanpassen = false
                    }
                }
                persoon != null -> PersoonKaart(persoon, staat, acties, onOpen, onNieuweAfspraak, onVoorlezen, onAanpassen = { aanpassen = true; nieuw = false })
                !nieuw -> LegeKaart("Nog niemand bij ${groepTekst(groep)}. Tik op + om iemand toe te voegen.")
            }
        }
    }
}

/** 🎒 School · 🏭 Werk · 🏠 Thuis */
@Composable
private fun GroepTabjes(gekozen: Groep, onKies: (Groep) -> Unit) =
    KeuzeBalk(Groep.entries.map(::groepTekst), gekozen.ordinal) { onKies(Groep.entries[it]) }

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

/**
 * Naam en beschrijving invullen: voor een nieuwe persoon, of om [begin] aan te passen.
 * Bij aanpassen kun je ook de groep kiezen en de persoon weghalen (eerst "weet je het zeker?").
 */
@Composable
private fun PersoonInvullen(
    titel: String,
    knop: String,
    begin: Persoon?,
    onAnnuleer: (() -> Unit)? = null,
    onVerwijder: (() -> Unit)? = null,
    onKlaar: (naam: String, rol: String, groep: Groep) -> Unit,
) {
    var naam by rememberSaveable { mutableStateOf(begin?.naam ?: "") }
    var rol by rememberSaveable { mutableStateOf(begin?.rol ?: "") }
    var groep by rememberSaveable { mutableStateOf(begin?.groep ?: Groep.School) }
    // Bij aanpassen staan de velden al vol: dan een kopje erboven, zodat je ziet wat wat is.
    val kopjes = begin != null
    Kaart {
        Tekst(titel, 19.sp)
        MetKopje("Naam".takeIf { kopjes }) { Invoerveld(naam, { naam = it }, "Naam, bv. Mevr. de Boer", eenRegel = true) }
        MetKopje("Beschrijving".takeIf { kopjes }) {
            Invoerveld(rol, { rol = it }, "Beschrijving, bv. mentor (mag leeg)", eenRegel = true)
        }
        if (begin != null) MetKopje("Groep") { GroepTabjes(groep) { groep = it } }
        Knop(knop, onClick = { if (naam.isNotBlank()) onKlaar(naam, rol, groep) }, modifier = Modifier.fillMaxWidth(), hoofd = true)
        if (onAnnuleer != null) Knop("Annuleren", onClick = onAnnuleer, modifier = Modifier.fillMaxWidth())
        if (onVerwijder != null) {
            var zeker by rememberSaveable { mutableStateOf(false) }
            Knop(
                if (zeker) "Weet je het zeker? Tik nog een keer" else "Persoon weghalen",
                onClick = { if (zeker) onVerwijder() else zeker = true },
                modifier = Modifier.fillMaxWidth(),
                hoofd = zeker,
            )
            if (zeker) Tekst("De afspraken blijven staan, zonder naam.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        }
    }
}

/** Invulveld met een klein kopje erboven (of zonder, als [kopje] leeg is). */
@Composable
private fun MetKopje(kopje: String?, veld: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (kopje != null) Tekst(kopje, 15.sp, Modifier.padding(start = 6.dp), dikte = FontWeight.ExtraBold, kleur = Color(0xFF8A6A5A))
        veld()
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
    onAanpassen: () -> Unit,
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
        Knop("Persoon aanpassen", onClick = onAanpassen, modifier = Modifier.fillMaxWidth(), icoon = Icoontjes.potlood)
    }
}

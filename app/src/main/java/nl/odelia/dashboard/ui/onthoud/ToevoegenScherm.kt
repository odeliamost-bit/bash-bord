package nl.odelia.dashboard.ui.onthoud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.Bron
import nl.odelia.dashboard.onthoud.Concept
import nl.odelia.dashboard.onthoud.Groep
import nl.odelia.dashboard.onthoud.OnthoudActies
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.onthoud.Soort
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoerveld
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Knop
import nl.odelia.dashboard.ui.onderdelen.Kopje
import nl.odelia.dashboard.ui.onderdelen.RondKnopje
import nl.odelia.dashboard.ui.onderdelen.Schakelaar
import nl.odelia.dashboard.ui.onderdelen.SchermKop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.kies
import nl.odelia.dashboard.ui.onderdelen.tik
import nl.odelia.dashboard.ui.onderdelen.voet
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.GroteKnop
import nl.odelia.dashboard.ui.thuis.dagKort

/**
 * "Onthoud dit": snel iets vastleggen (ontwerp/Toevoegen.dc.html).
 * Alleen "wat" is nodig; de rest mag leeg blijven. Met [bestaand] pas je een afspraak aan.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToevoegenScherm(
    staat: OnthoudStaat,
    acties: OnthoudActies,
    bestaand: Afspraak?,
    concept: Concept,
    nu: LocalDateTime,
    onBewaard: (String) -> Unit,
    onTerug: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var wat by rememberSaveable { mutableStateOf(bestaand?.wat ?: concept.tekst) }
    var notitie by rememberSaveable { mutableStateOf(bestaand?.notitie ?: "") }
    var persoonId by rememberSaveable { mutableStateOf(bestaand?.persoonId ?: concept.persoonId) }
    var groep by rememberSaveable { mutableStateOf(bestaand?.groep) }
    var project by rememberSaveable { mutableStateOf(bestaand?.project) }
    var soort by rememberSaveable { mutableStateOf(bestaand?.soort) }
    var deadline by rememberSaveable { mutableStateOf(bestaand?.deadline) }
    var seintje by rememberSaveable { mutableStateOf(bestaand?.seintje ?: true) }
    var bron by rememberSaveable { mutableStateOf(bestaand?.bron ?: concept.bron) }
    var nieuwePersoon by rememberSaveable { mutableStateOf(false) }
    var nieuwProject by rememberSaveable { mutableStateOf(false) }
    var leegMelding by rememberSaveable { mutableStateOf(false) }
    val klembord = LocalClipboardManager.current

    fun inspreken() = acties.inspreken { gezegd ->
        wat = if (wat.isBlank()) gezegd.replaceFirstChar { it.uppercase() } else "$wat $gezegd"
        bron = Bron.Ingesproken
    }

    // Geopend met de 🎤 van de widget: meteen beginnen met inspreken.
    LaunchedEffect(Unit) { if (concept.inspreken && bestaand == null) inspreken() }

    fun bewaar() {
        val tekst = wat.trim().ifEmpty { soort?.label ?: "" }
        if (tekst.isEmpty()) {
            leegMelding = true
            return
        }
        acties.bewaar(
            (bestaand ?: Afspraak(id = UUID.randomUUID().toString(), wat = tekst, gemaakt = nu)).copy(
                wat = tekst, notitie = notitie, persoonId = persoonId, groep = groep, project = project,
                soort = soort, deadline = deadline, seintje = seintje && deadline != null, bron = bron,
            ),
        )
        onBewaard(if (bestaand == null) "Bewaard ✓" else "Aangepast ✓")
    }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SchermKop(if (bestaand == null) "Onthoud dit" else "Aanpassen", onTerug) {
                MicKnop(onClick = ::inspreken)
            }

            // Wat?
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Kopje(Icoontjes.kopWat, "Wat?")
                Invoerveld(wat, { wat = it; leegMelding = false }, "Wat wil je onthouden?", minHoogte = 72.dp)
                if (leegMelding) {
                    Tekst("Schrijf of spreek eerst wat je wilt onthouden.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.Koraal)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Knop("Plakken", onClick = {
                        klembord.getText()?.text?.takeIf { it.isNotBlank() }?.let {
                            wat = if (wat.isBlank()) it.trim() else "$wat ${it.trim()}"
                            bron = Bron.Geplakt
                        }
                    }, icoon = Icoontjes.plakken)
                    Knop("Inspreken", onClick = ::inspreken, icoon = Icoontjes.microfoonPaars)
                }
                // Soort mag leeg: nog een keer tikken = weer uit. Drie naast elkaar, zoals in het ontwerp.
                Soort.entries.chunked(3).forEach { rij ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rij.forEach { s ->
                            SoortKnop(s, aan = soort == s, Modifier.weight(1f)) { soort = if (soort == s) null else s }
                        }
                    }
                }
            }

            // Wie?
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Kopje(Icoontjes.kopWie, "Wie?")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    staat.personen.forEach { p ->
                        Keuze(p.naam, aan = persoonId == p.id, onClick = { persoonId = if (persoonId == p.id) null else p.id }) {
                            PersoonRondje(p, 38.dp)
                        }
                    }
                    PlusRondje("Nieuwe persoon") { nieuwePersoon = !nieuwePersoon }
                }
                if (nieuwePersoon) {
                    NieuwePersoon(onKlaar = { naam, g ->
                        acties.nieuwePersoon(naam, g, "") { p -> persoonId = p.id }
                        nieuwePersoon = false
                    })
                }
            }

            // Waar hoort het bij?
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Kopje(Icoontjes.kopWaar, "Waar hoort het bij?")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val persoonGroep = staat.persoon(persoonId)?.groep
                    Groep.entries.forEach { g ->
                        Keuze(groepTekst(g), aan = (groep ?: persoonGroep) == g, onClick = { groep = if (groep == g) null else g })
                    }
                    (staat.projecten + listOfNotNull(project)).distinct().forEach { pr ->
                        Keuze(pr, aan = project == pr, onClick = { project = if (project == pr) null else pr }, achtergrond = Color(0xFFFFF1C6))
                    }
                    Keuze("+ project", aan = false, onClick = { nieuwProject = !nieuwProject })
                }
                if (nieuwProject) {
                    var naam by rememberSaveable { mutableStateOf("") }
                    Invoerveld(naam, { naam = it }, "Naam van het project, bv. PVB", eenRegel = true) {
                        Knop("OK", onClick = {
                            if (naam.isNotBlank()) project = naam.trim()
                            nieuwProject = false
                        }, hoofd = true)
                    }
                }
            }

            // Wanneer klaar?
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Kopje(Icoontjes.kopWanneer, "Wanneer klaar?")
                WanneerKeuze(nu.toLocalDate(), deadline) { deadline = it }
            }

            Invoerveld(notitie, { notitie = it }, "notitie (mag leeg)")

            if (deadline != null) {
                Row(
                    Modifier
                        .tik { seintje = !seintje }
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFFC48C3C), spotColor = Color(0xFFC48C3C))
                        .background(Color(0xFFFFF1C6), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .semantics { contentDescription = if (seintje) "Seintje de dag ervoor: aan" else "Seintje de dag ervoor: uit" },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.size(44.dp).background(Color.White, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        Icoon(Icoontjes.seintje, 24.dp)
                    }
                    Tekst("Seintje de dag ervoor", 16.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Color(0xFF4E3B08))
                    Schakelaar(seintje)
                }
            }

            GroteKnop("Bewaren", onClick = ::bewaar, icoon = Icoontjes.vinkje)

            if (bestaand != null) {
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Knop(
                        if (bestaand.klaar) "Toch niet klaar" else "Klaar ✓",
                        onClick = { acties.zetKlaar(bestaand.id, !bestaand.klaar); onTerug() },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    var zeker by rememberSaveable { mutableStateOf(false) }
                    Knop(
                        if (zeker) "Echt weg? Tik nog eens" else "Weghalen",
                        onClick = { if (zeker) { acties.verwijder(bestaand.id); onTerug() } else zeker = true },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        hoofd = zeker,
                    )
                }
            }
        }
    }
}

/** Grote ronde koraal 🎤-knop rechtsboven (ontwerp: 64 px, wit randje, voetje). */
@Composable
private fun MicKnop(onClick: () -> Unit) {
    Box(
        Modifier
            .tik(onClick = onClick)
            .size(64.dp)
            .voet(Kleur.KoraalDiep, CircleShape, 5.dp)
            .shadow(8.dp, CircleShape, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal)
            .clip(CircleShape)
            .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal))
            .border(5.dp, Color.White, CircleShape)
            .semantics { contentDescription = "Inspreken" },
        contentAlignment = Alignment.Center,
    ) {
        Icoon(Icoontjes.microfoon, 28.dp)
    }
}

/** Gestippeld rondje met een plusje. */
@Composable
private fun PlusRondje(uitleg: String, onClick: () -> Unit) {
    Box(
        Modifier
            .tik(onClick = onClick)
            .size(52.dp)
            .border(3.dp, Color(0xFFB9A3F0), CircleShape)
            .semantics { contentDescription = uitleg },
        contentAlignment = Alignment.Center,
    ) {
        Icoon(Icoontjes.plusPaars, 22.dp)
    }
}

/** Vakje met een soort: wit icoontje-blokje en de naam eronder. */
@Composable
private fun SoortKnop(soort: Soort, aan: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .kies(aan, onClick)
            .height(84.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(soortKleur(soort))
            .border(3.dp, if (aan) Kleur.Koraal else Color.Transparent, RoundedCornerShape(20.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.size(40.dp).background(Color.White, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icoon(soortIcoon(soort), 24.dp)
        }
        Tekst(soort.label, 14.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt, eenRegel = true)
    }
}

/** Naam typen en kiezen bij welke groep iemand hoort. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NieuwePersoon(onKlaar: (String, Groep) -> Unit) {
    var naam by rememberSaveable { mutableStateOf("") }
    var groep by rememberSaveable { mutableStateOf(Groep.School) }
    Column(
        Modifier
            .fillMaxWidth()
            .background(Kleur.Kaart, RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Invoerveld(naam, { naam = it }, "Naam, bv. Mevr. de Boer", eenRegel = true)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Groep.entries.forEach { g -> Keuze(groepTekst(g), aan = groep == g, onClick = { groep = g }) }
        }
        Knop("Persoon toevoegen", onClick = { if (naam.isNotBlank()) onKlaar(naam, groep) }, modifier = Modifier.fillMaxWidth(), hoofd = true)
    }
}

/** Vandaag · morgen · volgende maandag · kies… (dan een rij met de komende 4 weken). */
@Composable
private fun WanneerKeuze(vandaag: LocalDate, gekozen: LocalDate?, onKies: (LocalDate?) -> Unit) {
    val maandag = vandaag.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).let { if (it == vandaag.plusDays(1)) it.plusWeeks(1) else it }
    val vast = listOf("vandaag" to vandaag, "morgen" to vandaag.plusDays(1), dagKort(maandag.dayOfWeek) to maandag)
    var kiezen by rememberSaveable { mutableStateOf(gekozen != null && vast.none { it.second == gekozen }) }
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        vast.forEach { (boven, datum) ->
            DatumVakje(boven, datum.dayOfMonth.toString(), aan = gekozen == datum, Modifier.weight(1f)) {
                onKies(if (gekozen == datum) null else datum)
                kiezen = false
            }
        }
        DatumVakje("kies", if (kiezen && gekozen != null) gekozen.dayOfMonth.toString() else "…", aan = kiezen, Modifier.weight(1f)) {
            kiezen = !kiezen
        }
    }
    if (kiezen) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (2L..28L).map { vandaag.plusDays(it) }.forEach { d ->
                DatumVakje(dagKort(d.dayOfWeek), d.dayOfMonth.toString(), aan = gekozen == d, Modifier.size(width = 64.dp, height = 68.dp)) {
                    onKies(if (gekozen == d) null else d)
                }
            }
        }
    }
}

/** Blauw vakje met klein woord boven en grote dag eronder (zoals in het ontwerp). */
@Composable
private fun DatumVakje(boven: String, groot: String, aan: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .kies(aan, onClick)
            .heightIn(min = 68.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFD4E8FF))
            .border(3.dp, if (aan) Kleur.Koraal else Color.Transparent, RoundedCornerShape(18.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Tekst(boven, 15.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF1F3F66), eenRegel = true)
        Tekst(groot, 22.sp, kleur = Color(0xFF1F3F66), regel = 24.sp)
    }
}

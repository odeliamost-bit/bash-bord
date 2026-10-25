package nl.odelia.dashboard.ui.onthoud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import nl.odelia.dashboard.logica.Herken
import nl.odelia.dashboard.logica.Instellingen
import nl.odelia.dashboard.logica.Links
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.korteDatum
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.onderdelen.DatumKiezer
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Kopje
import nl.odelia.dashboard.ui.onderdelen.MicKnop
import nl.odelia.dashboard.ui.onderdelen.Schakelaar
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.opzoekVraag
import nl.odelia.dashboard.ui.onderdelen.rememberVraag
import nl.odelia.dashboard.ui.onderdelen.routeVraag
import nl.odelia.dashboard.ui.theme.Kleur

/** Groepen die er altijd staan. Nieuwe komen er vanzelf bij. */
val VasteGroepen = listOf("Werk", "School", "Thuis")

/** Iets snel vastleggen (ontwerp/Toevoegen.dc.html): typen, plakken of inspreken. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToevoegenScherm(
    start: Scherm.Toevoegen,
    staat: AppStaat,
    acties: AppActies,
    onKlaar: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vandaag = staat.nu.toLocalDate()
    val oud = remember(start.id) { staat.onthoud.firstOrNull { it.id == start.id } }
    val mensen = remember(staat.onthoud) { staat.onthoud.mapNotNull { it.wie }.distinct().sorted() }
    val groepen = remember(staat.onthoud) { (VasteGroepen + staat.onthoud.mapNotNull { it.groep }).distinct() }

    var tekst by remember { mutableStateOf(oud?.tekst ?: start.tekst) }
    var wie by remember { mutableStateOf(oud?.wie ?: start.wie) }
    var wat by remember { mutableStateOf(oud?.wat) }
    var waar by remember { mutableStateOf(oud?.waar.orEmpty()) }
    var wanneer by remember { mutableStateOf(oud?.wanneer) }
    var groep by remember { mutableStateOf(oud?.groep ?: start.groep) }
    var seintje by remember { mutableStateOf(oud?.seintje ?: true) }
    var nieuweNaam by remember { mutableStateOf<String?>(null) }
    var nieuweGroep by remember { mutableStateOf<String?>(null) }
    var kiesDatum by remember { mutableStateOf(false) }
    val klembord = LocalClipboardManager.current
    val vraag = rememberVraag()

    /** Vul aan wat in de (ingesproken) tekst staat; wat je al gekozen had blijft staan. */
    fun vulAan(t: String) {
        if (wanneer == null) wanneer = Herken.datum(t, vandaag)
        if (wie == null) wie = Herken.uitLijst(t, mensen)
        if (wat == null) wat = Herken.soort(t)
        if (groep == null) groep = Herken.uitLijst(t, groepen)
    }

    fun spreekIn() {
        val gelukt = acties.inspreken { gehoord ->
            tekst = if (tekst.isBlank()) gehoord else "$tekst $gehoord"
            vulAan(gehoord)
        }
        if (!gelukt) onMelding("Inspreken lukt niet op deze telefoon.")
    }

    LaunchedEffect(Unit) {
        if (start.inspreken) spreekIn()
        if (start.tekst.isNotBlank() && oud == null) vulAan(start.tekst)
    }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            TerugBalk(if (oud == null) "Onthoud dit" else "Aanpassen", onKlaar) {
                MicKnop(::spreekIn, maat = 60.dp)
            }

            // Wat moet je onthouden? (vrij, zo kort of lang als je wilt)
            Invoer(
                tekst,
                onWijzig = { tekst = it },
                hint = "Wat moet je onthouden?",
                regels = 3,
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF5E8DA))
                        .clickable {
                            val geplakt = klembord.getText()?.text.orEmpty()
                            if (geplakt.isBlank()) {
                                onMelding("Er staat niks om te plakken.")
                            } else {
                                tekst = if (tekst.isBlank()) geplakt else "$tekst $geplakt"
                                vulAan(geplakt)
                            }
                        }
                        .semantics { contentDescription = "Plakken" },
                    contentAlignment = Alignment.Center,
                ) {
                    Icoon(Icoontjes.plakken, 24.dp)
                }
            }

            if (tekst.isNotBlank()) {
                ZachteKnop("🔎 Opzoeken op internet", onClick = {
                    vraag.stel(opzoekVraag(tekst) { if (!acties.openLink(Links.zoek(tekst))) onMelding("Chrome openen lukte niet.") })
                }, Modifier.fillMaxWidth())
            }

            // Wie?
            Kopje("Wie?", Icoontjes.persoonLijn)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                (mensen + listOfNotNull(wie).filter { it !in mensen }).forEach { naam ->
                    Keuze(naam, wie == naam, onClick = { wie = if (wie == naam) null else naam })
                }
                Keuze("＋ nieuw", nieuweNaam != null, onClick = { nieuweNaam = if (nieuweNaam == null) "" else null }, achter = Color(0xFFF5E8DA))
            }
            nieuweNaam?.let { naam ->
                Invoer(naam, onWijzig = { nieuweNaam = it }, hint = "Naam (bv. Mevr. Bakker)") {
                    ZachteKnop("OK", onClick = { if (naam.isNotBlank()) wie = naam.trim(); nieuweNaam = null })
                }
            }

            // Wat?
            Kopje("Wat?", Icoontjes.documentLijn)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Herken.soorten.keys.forEach { s ->
                    Keuze(s, wat == s, onClick = { wat = if (wat == s) null else s }, achter = Color(0xFFFFF6EE))
                }
            }

            // Wanneer?
            Kopje("Wanneer klaar?", Icoontjes.kalenderLijn)
            val volgendeWerkdag = generateSequence(vandaag.plusDays(2)) { it.plusDays(1) }.first { Instellingen.isWerkdag(it) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Keuze("vandaag", wanneer == vandaag, onClick = { wanneer = vandaag }, achter = Color(0xFFD4E8FF))
                Keuze("morgen", wanneer == vandaag.plusDays(1), onClick = { wanneer = vandaag.plusDays(1) }, achter = Color(0xFFD4E8FF))
                Keuze(korteDatum(volgendeWerkdag), wanneer == volgendeWerkdag, onClick = { wanneer = volgendeWerkdag }, achter = Color(0xFFD4E8FF))
                val anders = wanneer != null && wanneer !in listOf(vandaag, vandaag.plusDays(1), volgendeWerkdag)
                Keuze(if (anders) "📅 ${korteDatum(wanneer!!)}" else "📅 kies", anders, onClick = { kiesDatum = true }, achter = Color(0xFFD4E8FF))
                Keuze("geen", wanneer == null, onClick = { wanneer = null }, achter = Color(0xFFD4E8FF))
            }

            // Groep
            Kopje("Groep", Icoontjes.groepLijn)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                (groepen + listOfNotNull(groep).filter { it !in groepen }).forEach { g ->
                    Keuze(g, groep == g, onClick = { groep = if (groep == g) null else g }, achter = Color(0xFFE3EDDF))
                }
                Keuze("＋ nieuw", nieuweGroep != null, onClick = { nieuweGroep = if (nieuweGroep == null) "" else null }, achter = Color(0xFFF5E8DA))
            }
            nieuweGroep?.let { naam ->
                Invoer(naam, onWijzig = { nieuweGroep = it }, hint = "Bijvoorbeeld PVB of keuzedeel") {
                    ZachteKnop("OK", onClick = { if (naam.isNotBlank()) groep = naam.trim(); nieuweGroep = null })
                }
            }

            // Waar?
            Invoer(waar, onWijzig = { waar = it }, hint = "Waar? (mag leeg)") {
                if (waar.isNotBlank()) {
                    ZachteKnop("🗺️ Route", onClick = {
                        vraag.stel(routeVraag(waar) { if (!acties.openLink(Links.route(waar))) onMelding("Maps openen lukte niet.") })
                    })
                }
            }

            // Seintje
            Row(
                Modifier
                    .fillMaxWidth()
                    .klei(Color(0xFFFFF1C6), RoundedCornerShape(20.dp), Color(0xFFC48C3C), hoogte = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClickLabel = "Seintje aan of uit") { seintje = !seintje }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(44.dp).background(Color.White, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Icoon(Icoontjes.belLijn, 24.dp)
                }
                Column(Modifier.weight(1f)) {
                    Tekst("Seintje de dag ervoor", 16.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF4E3B08))
                    if (wanneer == null && seintje) Tekst("(alleen als er een datum is)", 13.sp, dikte = FontWeight.Bold, kleur = Color(0xFF8A6A10))
                }
                Schakelaar(seintje)
            }

            GroteKnop("Bewaren", icoon = Icoontjes.vinkje, onClick = {
                val titel = tekst.trim()
                if (titel.isBlank() && wat == null) {
                    onMelding("Vul eerst in wat je wilt onthouden.")
                } else {
                    acties.bewaar(
                        (oud ?: Onthoud(tekst = titel)).copy(
                            tekst = titel, wie = wie, wat = wat, waar = waar.trim().ifBlank { null },
                            wanneer = wanneer, groep = groep, seintje = seintje,
                        ),
                    )
                    onMelding("Bewaard ✓")
                    onKlaar()
                }
            })

            if (oud != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ZachteKnop(if (oud.klaar) "Toch nog doen" else "✓ Klaar", onClick = {
                        acties.bewaar(oud.copy(klaar = !oud.klaar))
                        onKlaar()
                    }, Modifier.weight(1f))
                    ZachteKnop("Weggooien", onClick = {
                        acties.verwijder(oud.id)
                        onMelding("Weggegooid")
                        onKlaar()
                    }, Modifier.weight(1f), kleur = Kleur.KoraalDiep)
                }
            }
        }
    }

    vraag.Toon()

    if (kiesDatum) {
        DatumKiezer(
            begin = wanneer ?: vandaag,
            onKies = { wanneer = it; kiesDatum = false },
            onSluit = { kiesDatum = false },
        )
    }
}

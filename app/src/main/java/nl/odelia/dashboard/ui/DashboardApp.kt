package nl.odelia.dashboard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlinx.coroutines.delay
import nl.odelia.dashboard.onthoud.Concept
import nl.odelia.dashboard.onthoud.OnthoudActies
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.ui.later.KomtLaterScherm
import nl.odelia.dashboard.ui.menu.Zijbalk
import nl.odelia.dashboard.ui.menu.ZijbalkRuimte
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onthoud.LijstScherm
import nl.odelia.dashboard.ui.onthoud.MensenScherm
import nl.odelia.dashboard.ui.onthoud.ToevoegenScherm
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.Tab
import nl.odelia.dashboard.ui.thuis.ThuisScherm
import nl.odelia.dashboard.ui.werk.DagScherm
import nl.odelia.dashboard.ui.werk.UrenmailScherm
import nl.odelia.dashboard.uren.KlokActies
import nl.odelia.dashboard.uren.KlokStaat
import nl.odelia.dashboard.uren.MailActies

/**
 * De hele app: zijbalk links, het gekozen scherm rechts.
 *
 * @param klok alles over in- en uitklokken, uitgerekend voor nu.
 * @param onthoud alle afspraken en personen.
 * @param onVoorlezen leest tekst voor; geeft `false` als dat (nog) niet lukt.
 * @param verzoek iets van buiten de app (gedeelde tekst, widget, melding): opent meteen het goede scherm.
 * @param beginScherm opent meteen een scherm, bv. "dag/2026-10-22" of "nieuw" (voor voorbeelden).
 */
@Composable
fun DashboardApp(
    klok: KlokStaat,
    klokActies: KlokActies,
    onthoud: OnthoudStaat,
    onthoudActies: OnthoudActies,
    mailActies: MailActies,
    kimAdres: String,
    onVoorlezen: (String) -> Boolean,
    verzoek: Verzoek? = null,
    onVerzoekGezien: () -> Unit = {},
    beginPlek: Plek = Plek.Thuis,
    beginTab: Tab = Tab.Vandaag,
    beginMenuOpen: Boolean = false,
    beginScherm: String? = null,
) {
    var plek by rememberSaveable { mutableStateOf(beginPlek) }
    var tab by rememberSaveable { mutableStateOf(beginTab) }
    var menuOpen by rememberSaveable { mutableStateOf(beginMenuOpen) }
    // Schermen die over het hoofdscherm heen open staan, bv. "lijst|bewerk/123". Terug = de laatste dicht.
    var stapel by rememberSaveable { mutableStateOf(beginScherm ?: "") }
    var concept by remember { mutableStateOf(Concept()) }
    var melding by remember { mutableStateOf<Melding?>(null) }

    val scherm = stapel.split("|").lastOrNull { it.isNotEmpty() }
    fun open(s: String) {
        stapel = if (stapel.isEmpty()) s else "$stapel|$s"
    }
    fun terug() {
        stapel = stapel.split("|").dropLast(1).joinToString("|")
    }
    fun nieuw(c: Concept) {
        concept = c
        open("nieuw")
    }

    // Terug-knop van de telefoon: eerst menu dicht, dan het bovenste scherm dicht, daarna terug naar Thuis.
    BackHandler(enabled = menuOpen || stapel.isNotEmpty() || plek != Plek.Thuis) {
        when {
            menuOpen -> menuOpen = false
            stapel.isNotEmpty() -> terug()
            else -> plek = Plek.Thuis
        }
    }

    // Tekst gedeeld vanuit een andere app, de knop van de widget of een melding: meteen het goede scherm.
    LaunchedEffect(verzoek) {
        when (verzoek) {
            is Verzoek.Onthoud -> nieuw(verzoek.concept)
            Verzoek.Urenmail -> stapel = "urenmail"
            null -> return@LaunchedEffect
        }
        menuOpen = false
        onVerzoekGezien()
    }

    // Werk & uren en Onthoud zijn tabjes op het hoofdscherm.
    val gekozen = when {
        scherm?.startsWith("dag/") == true || scherm == "urenmail" -> Plek.Werk
        scherm != null -> Plek.Onthoud
        plek != Plek.Thuis -> plek
        tab == Tab.Werk -> Plek.Werk
        tab == Tab.Onthoud -> Plek.Onthoud
        else -> Plek.Thuis
    }

    fun gaNaar(naar: Plek) {
        stapel = ""
        when (naar) {
            Plek.Thuis -> { plek = Plek.Thuis; tab = Tab.Vandaag }
            Plek.Werk -> { plek = Plek.Thuis; tab = Tab.Werk }
            Plek.Onthoud -> { plek = Plek.Thuis; tab = Tab.Onthoud }
            else -> plek = naar
        }
    }

    fun toon(tekst: String) {
        melding = Melding(tekst)
    }

    val voorlezen: (String) -> Unit = { tekst ->
        if (!onVoorlezen(tekst)) toon("Voorlezen lukt nog niet. Staat er een Nederlandse stem op je telefoon?")
    }

    Box(Modifier.fillMaxSize().background(Kleur.Achtergrond)) {
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            val inhoud = Modifier.padding(start = ZijbalkRuimte)
            when {
                scherm?.startsWith("dag/") == true -> {
                    val dag = klok.dag(LocalDate.parse(scherm.removePrefix("dag/")))
                    DagScherm(dag, klok.uren(dag), klok.nu, klokActies, onTerug = ::terug, modifier = inhoud)
                }
                scherm == "urenmail" -> UrenmailScherm(klok, kimAdres, mailActies, onMelding = ::toon, onTerug = ::terug, modifier = inhoud)
                scherm == "nieuw" || scherm?.startsWith("bewerk/") == true -> {
                    val bestaand = onthoud.afspraken.firstOrNull { it.id == scherm.removePrefix("bewerk/") }
                    if (scherm != "nieuw" && bestaand == null) {
                        // Net weggehaald: dit scherm bestaat niet meer.
                        LaunchedEffect(scherm) { terug() }
                    } else {
                        // Elk scherm zijn eigen invulvelden (anders blijft de vorige afspraak staan).
                        key(scherm) {
                            ToevoegenScherm(
                                onthoud, onthoudActies, bestaand, concept, klok.nu,
                                onBewaard = { toon(it); terug() },
                                onTerug = ::terug,
                                modifier = inhoud,
                            )
                        }
                    }
                }
                scherm?.startsWith("lijst") == true -> LijstScherm(
                    onthoud, onthoudActies,
                    onOpen = { open("bewerk/${it.id}") },
                    onNieuw = { nieuw(Concept()) },
                    onVoorlezen = voorlezen,
                    onTerug = ::terug,
                    modifier = inhoud,
                    // "lijst/project:PVB" = meteen gefilterd (voor voorbeelden)
                    beginFilter = scherm.removePrefix("lijst").removePrefix("/").ifEmpty { null },
                )
                plek == Plek.Thuis -> ThuisScherm(
                    tab = tab,
                    onTab = { tab = it },
                    klok = klok,
                    klokActies = klokActies,
                    onthoud = onthoud,
                    onVoorlezen = voorlezen,
                    onOpenDag = { open("dag/$it") },
                    onOpenAfspraak = { open("bewerk/${it.id}") },
                    onAlles = { open("lijst") },
                    onNieuw = { inspreken -> nieuw(Concept(inspreken = inspreken)) },
                    onUrenmail = { open("urenmail") },
                    modifier = inhoud,
                )
                plek == Plek.Mensen -> MensenScherm(
                    onthoud, onthoudActies,
                    onOpen = { open("bewerk/${it.id}") },
                    onNieuweAfspraak = { nieuw(Concept(persoonId = it)) },
                    onVoorlezen = voorlezen,
                    onTerug = { gaNaar(Plek.Thuis) },
                    modifier = inhoud,
                )
                else -> KomtLaterScherm(plek, onTerug = { gaNaar(Plek.Thuis) }, modifier = inhoud)
            }

            // Waas achter het open menu: tik = menu dicht
            if (menuOpen) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0x593A3046))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClickLabel = "Menu sluiten",
                        ) { menuOpen = false },
                )
            }

            Zijbalk(
                open = menuOpen,
                gekozen = gekozen,
                onWissel = { menuOpen = !menuOpen },
                onKies = { gekozenPlek ->
                    gaNaar(gekozenPlek)
                    menuOpen = false
                },
                modifier = Modifier.padding(12.dp),
            )

            melding?.let { m ->
                MeldingBalk(
                    melding = m,
                    onWeg = { if (melding === m) melding = null },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

/** Kort berichtje onderin. Elk berichtje is nieuw, ook als de tekst hetzelfde is. */
private class Melding(val tekst: String)

@Composable
private fun MeldingBalk(melding: Melding, onWeg: () -> Unit, modifier: Modifier) {
    LaunchedEffect(melding) {
        delay(3000)
        onWeg()
    }
    Row(
        modifier
            .padding(start = ZijbalkRuimte + 8.dp, end = 16.dp, bottom = 20.dp)
            .klei(Kleur.Kaart, CircleShape, Kleur.KaartSchaduw, hoogte = 14.dp)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(12.dp).background(Kleur.Koraal, CircleShape))
        Tekst(melding.tekst, 16.sp, dikte = FontWeight.ExtraBold)
    }
}

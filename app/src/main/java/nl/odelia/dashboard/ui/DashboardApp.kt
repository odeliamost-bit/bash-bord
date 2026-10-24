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
import nl.odelia.dashboard.ui.later.KomtLaterScherm
import nl.odelia.dashboard.ui.menu.Zijbalk
import nl.odelia.dashboard.ui.menu.ZijbalkRuimte
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.Tab
import nl.odelia.dashboard.ui.thuis.ThuisScherm
import nl.odelia.dashboard.ui.werk.DagScherm
import nl.odelia.dashboard.uren.KlokActies
import nl.odelia.dashboard.uren.KlokStaat

/**
 * De hele app: zijbalk links, het gekozen scherm rechts.
 *
 * @param klok alles over in- en uitklokken, uitgerekend voor nu.
 * @param onVoorlezen leest tekst voor; geeft `false` als dat (nog) niet lukt.
 * @param beginDag opent meteen het scherm om die dag aan te passen (voor voorbeelden).
 */
@Composable
fun DashboardApp(
    klok: KlokStaat,
    klokActies: KlokActies,
    onVoorlezen: (String) -> Boolean,
    beginPlek: Plek = Plek.Thuis,
    beginTab: Tab = Tab.Vandaag,
    beginMenuOpen: Boolean = false,
    beginDag: LocalDate? = null,
) {
    var plek by rememberSaveable { mutableStateOf(beginPlek) }
    var tab by rememberSaveable { mutableStateOf(beginTab) }
    var menuOpen by rememberSaveable { mutableStateOf(beginMenuOpen) }
    // Een dag die je aan het aanpassen bent (als tekst, zodat hij een draai van het scherm overleeft).
    var openDag by rememberSaveable { mutableStateOf(beginDag?.toString()) }
    var melding by remember { mutableStateOf<Melding?>(null) }

    // Terug-knop van de telefoon: eerst menu dicht, dan de dag dicht, daarna terug naar Thuis.
    BackHandler(enabled = menuOpen || openDag != null || plek != Plek.Thuis) {
        when {
            menuOpen -> menuOpen = false
            openDag != null -> openDag = null
            else -> plek = Plek.Thuis
        }
    }

    // Werk & uren en Onthoud zijn tabjes op het hoofdscherm.
    val gekozen = when {
        plek != Plek.Thuis -> plek
        tab == Tab.Werk -> Plek.Werk
        tab == Tab.Onthoud -> Plek.Onthoud
        else -> Plek.Thuis
    }

    fun gaNaar(naar: Plek) {
        openDag = null
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

    Box(Modifier.fillMaxSize().background(Kleur.Achtergrond)) {
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            val inhoud = Modifier.padding(start = ZijbalkRuimte)
            val dag = openDag?.let { klok.dag(LocalDate.parse(it)) }
            when {
                dag != null -> DagScherm(
                    dag = dag,
                    uren = klok.uren(dag),
                    nu = klok.nu,
                    acties = klokActies,
                    onTerug = { openDag = null },
                    modifier = inhoud,
                )
                plek == Plek.Thuis -> ThuisScherm(
                    tab = tab,
                    onTab = { tab = it },
                    klok = klok,
                    klokActies = klokActies,
                    onVoorlezen = { tekst ->
                        if (!onVoorlezen(tekst)) toon("Voorlezen lukt nog niet. Staat er een Nederlandse stem op je telefoon?")
                    },
                    onMelding = ::toon,
                    onGaNaar = ::gaNaar,
                    onOpenDag = { openDag = it.toString() },
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

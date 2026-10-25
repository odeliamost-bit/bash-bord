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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import nl.odelia.dashboard.ui.cyclus.CyclusScherm
import nl.odelia.dashboard.ui.geld.GeldBegin
import nl.odelia.dashboard.ui.geld.GeldScherm
import nl.odelia.dashboard.ui.later.KomtLaterScherm
import nl.odelia.dashboard.ui.leren.LerenBegin
import nl.odelia.dashboard.ui.leren.LerenScherm
import nl.odelia.dashboard.ui.menu.Zijbalk
import nl.odelia.dashboard.ui.menu.ZijbalkRuimte
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onthoud.OnthoudLijstScherm
import nl.odelia.dashboard.ui.onthoud.ToevoegenScherm
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.Tab
import nl.odelia.dashboard.ui.thuis.ThuisScherm
import nl.odelia.dashboard.ui.weer.ZeilweerScherm
import nl.odelia.dashboard.ui.wensen.WensenBegin
import nl.odelia.dashboard.ui.wensen.WensenScherm
import nl.odelia.dashboard.ui.werk.UrenmailScherm

/** Welk scherm rechts van de zijbalk staat. */
sealed interface Scherm {
    data object Thuis : Scherm
    data class Toevoegen(
        val id: String? = null,
        val tekst: String = "",
        val inspreken: Boolean = false,
        val wie: String? = null,
        val groep: String? = null,
    ) : Scherm
    data class Lijst(val perPersoon: Boolean = false) : Scherm
    data object Urenmail : Scherm
    data object Zeilweer : Scherm
    data class Leren(val begin: LerenBegin = LerenBegin()) : Scherm
    data object Cyclus : Scherm
    data class Wensen(val begin: WensenBegin = WensenBegin()) : Scherm
    data class Geld(val begin: GeldBegin = GeldBegin()) : Scherm
    data class Later(val plek: Plek) : Scherm
}

/** De hele app: zijbalk links, het gekozen scherm rechts. */
@Composable
fun DashboardApp(
    staat: AppStaat,
    acties: AppActies,
    opdracht: Opdracht? = null,
    onOpdrachtGedaan: () -> Unit = {},
    beginScherm: Scherm = Scherm.Thuis,
    beginTab: Tab = Tab.Vandaag,
    beginMenuOpen: Boolean = false,
) {
    val stapel = remember { mutableStateListOf(beginScherm) }
    val scherm = stapel.last()
    var tab by remember { mutableStateOf(beginTab) }
    var menuOpen by remember { mutableStateOf(beginMenuOpen) }
    var melding by remember { mutableStateOf<Melding?>(null) }

    fun ga(naar: Scherm) {
        if (naar == Scherm.Thuis) {
            stapel.clear()
            stapel.add(Scherm.Thuis)
        } else {
            stapel.add(naar)
        }
    }

    fun terug() {
        if (stapel.size > 1) stapel.removeAt(stapel.lastIndex) else ga(Scherm.Thuis)
    }

    fun toon(tekst: String) {
        melding = Melding(tekst)
    }

    fun naarPlek(plek: Plek) {
        when (plek) {
            Plek.Thuis -> { ga(Scherm.Thuis); tab = Tab.Vandaag }
            Plek.Werk -> { ga(Scherm.Thuis); tab = Tab.Werk }
            Plek.Onthoud -> { ga(Scherm.Thuis); ga(Scherm.Lijst()) }
            Plek.Mensen -> { ga(Scherm.Thuis); ga(Scherm.Lijst(perPersoon = true)) }
            Plek.Zeilweer -> { ga(Scherm.Thuis); ga(Scherm.Zeilweer) }
            Plek.Leren -> { ga(Scherm.Thuis); ga(Scherm.Leren()) }
            Plek.Cyclus -> { ga(Scherm.Thuis); ga(Scherm.Cyclus) }
            Plek.Wensen -> { ga(Scherm.Thuis); ga(Scherm.Wensen()) }
            Plek.Geld -> { ga(Scherm.Thuis); ga(Scherm.Geld()) }
            else -> { ga(Scherm.Thuis); ga(Scherm.Later(plek)) }
        }
    }

    // Van buitenaf: melding, widget of een andere app die iets deelt.
    LaunchedEffect(opdracht) {
        val o = opdracht ?: return@LaunchedEffect
        menuOpen = false
        when (o.scherm) {
            "toevoegen" -> { ga(Scherm.Thuis); ga(Scherm.Toevoegen(tekst = o.tekst.orEmpty())) }
            "inspreken" -> { ga(Scherm.Thuis); ga(Scherm.Toevoegen(inspreken = true)) }
            "onthoud" -> naarPlek(Plek.Onthoud)
            "urenmail" -> { ga(Scherm.Thuis); tab = Tab.Werk; ga(Scherm.Urenmail) }
            "werk" -> naarPlek(Plek.Werk)
            "cyclus" -> naarPlek(Plek.Cyclus)
        }
        onOpdrachtGedaan()
    }

    // Terug-knop van de telefoon: eerst menu dicht, dan een scherm terug.
    BackHandler(enabled = menuOpen || scherm != Scherm.Thuis) {
        if (menuOpen) menuOpen = false else terug()
    }

    val gekozen = when (scherm) {
        Scherm.Thuis -> when (tab) {
            Tab.Vandaag -> Plek.Thuis
            Tab.Werk -> Plek.Werk
            Tab.Onthoud -> Plek.Onthoud
        }
        is Scherm.Toevoegen -> Plek.Onthoud
        is Scherm.Lijst -> if (scherm.perPersoon) Plek.Mensen else Plek.Onthoud
        Scherm.Urenmail -> Plek.Werk
        Scherm.Zeilweer -> Plek.Zeilweer
        is Scherm.Leren -> Plek.Leren
        Scherm.Cyclus -> Plek.Cyclus
        is Scherm.Wensen -> Plek.Wensen
        is Scherm.Geld -> Plek.Geld
        is Scherm.Later -> scherm.plek
    }

    Box(Modifier.fillMaxSize().background(Kleur.Achtergrond)) {
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            val inhoud = Modifier.padding(start = ZijbalkRuimte)
            when (scherm) {
                Scherm.Thuis -> ThuisScherm(
                    staat = staat,
                    acties = acties,
                    tab = tab,
                    onTab = { tab = it },
                    onVoorlezen = { tekst ->
                        if (!acties.voorlezen(tekst)) toon("Voorlezen lukt nog niet. Staat er een Nederlandse stem op je telefoon?")
                    },
                    onMelding = ::toon,
                    onGaNaar = ::ga,
                    onPlek = ::naarPlek,
                    modifier = inhoud,
                )
                is Scherm.Toevoegen -> ToevoegenScherm(
                    start = scherm,
                    staat = staat,
                    acties = acties,
                    onKlaar = ::terug,
                    onMelding = ::toon,
                    modifier = inhoud,
                )
                is Scherm.Lijst -> OnthoudLijstScherm(
                    perPersoon = scherm.perPersoon,
                    staat = staat,
                    acties = acties,
                    onTerug = ::terug,
                    onGaNaar = ::ga,
                    onMelding = ::toon,
                    modifier = inhoud,
                )
                Scherm.Urenmail -> UrenmailScherm(
                    staat = staat,
                    acties = acties,
                    onTerug = ::terug,
                    onNaarWerk = { naarPlek(Plek.Werk) },
                    onMelding = ::toon,
                    modifier = inhoud,
                )
                Scherm.Zeilweer -> ZeilweerScherm(staat, acties, onTerug = ::terug, modifier = inhoud)
                is Scherm.Leren -> LerenScherm(staat, acties, onTerug = ::terug, onMelding = ::toon, modifier = inhoud, begin = scherm.begin)
                Scherm.Cyclus -> CyclusScherm(staat, acties, onTerug = ::terug, onMelding = ::toon, modifier = inhoud)
                is Scherm.Wensen -> WensenScherm(staat, acties, onTerug = ::terug, onMelding = ::toon, modifier = inhoud, begin = scherm.begin)
                is Scherm.Geld -> GeldScherm(staat, acties, onTerug = ::terug, onMelding = ::toon, modifier = inhoud, begin = scherm.begin)
                is Scherm.Later -> KomtLaterScherm(scherm.plek, onTerug = ::terug, modifier = inhoud)
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
                onKies = { plek ->
                    if (!menuOpen) {
                        menuOpen = true
                    } else {
                        naarPlek(plek)
                        menuOpen = false
                    }
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

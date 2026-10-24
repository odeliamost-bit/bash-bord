package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.Ochtend
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.kies
import nl.odelia.dashboard.ui.onderdelen.tik
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.uren.KlokActies
import nl.odelia.dashboard.uren.KlokStaat

enum class Tab(val label: String) {
    Vandaag("Vandaag"),
    Werk("Werk"),
    Onthoud("Onthoud"),
}

/** Hoofdscherm (ontwerp/Main.dc.html), zonder de onderbalk: we gebruiken de zijbalk. */
@Composable
fun ThuisScherm(
    tab: Tab,
    onTab: (Tab) -> Unit,
    klok: KlokStaat,
    klokActies: KlokActies,
    onthoud: OnthoudStaat,
    onVoorlezen: (String) -> Unit,
    onMelding: (String) -> Unit,
    onGaNaar: (Plek) -> Unit,
    onOpenDag: (LocalDate) -> Unit,
    onOpenAfspraak: (Afspraak) -> Unit,
    onAlles: () -> Unit,
    onNieuw: (inspreken: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nu = klok.nu
    val groet = groet(nu)
    val onder = onderregel(nu)

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Bovenbalk(datumTekst(nu), aantal = onthoud.vandaagTeDoen.size, onBel = onAlles)
            WelkomKaart(groet, onder, onVoorlezen = { onVoorlezen("$groet, Odelia! $onder. ${Ochtend.voorlees(onthoud)}") })
            Tabjes(tab, onTab)
            when (tab) {
                Tab.Vandaag -> VandaagTab(klok, klokActies, onthoud, onOpenDag, onWeek = { onTab(Tab.Werk) }, onOnthoud = { onTab(Tab.Onthoud) })
                Tab.Werk -> WerkTab(klok, klokActies, onOpenDag)
                Tab.Onthoud -> OnthoudTab(onthoud, onOpenAfspraak, onAlles, onNieuw = { onNieuw(false) }, onInspreken = { onNieuw(true) })
            }
            SnelNaar(onGaNaar, onMelding)
        }
    }
}

@Composable
private fun Bovenbalk(datum: String, aantal: Int, onBel: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape, ambientColor = Color(0xFF6E9C79), spotColor = Color(0xFF6E9C79))
                .background(hoekVerloop(Color(0xFFCFE3D2), Color(0xFF9EC4A6)), CircleShape)
                .glans(CircleShape, licht = 0.6f, donker = 0.08f)
                .border(3.dp, Kleur.Kaart, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icoon(Icoontjes.persoon, 30.dp)
        }
        Column(Modifier.weight(1f)) {
            Tekst("Dashboard", 26.sp, regel = 29.sp)
            Tekst(datum, 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
        }
        Box {
            Box(
                Modifier
                    .tik(onClick = onBel)
                    .size(52.dp)
                    .shadow(8.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                    .clip(CircleShape)
                    .background(Kleur.Kaart)
                    .glans(CircleShape, licht = 0.9f, donker = 0f, boven = 2.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                    .semantics { contentDescription = if (aantal == 0) "Vandaag niets te doen" else "Vandaag $aantal te doen" },
                contentAlignment = Alignment.Center,
            ) {
                Icoon(Icoontjes.bel, 26.dp)
            }
            // Rood bolletje: hoeveel er vandaag (of te laat) moet. Tik op de bel = de lijst.
            if (aantal > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(22.dp)
                        .background(hoekVerloop(Color(0xFFF09A84), Kleur.Koraal), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Tekst(if (aantal > 9) "9+" else aantal.toString(), 13.sp, kleur = Color.White)
                }
            }
        }
    }
}

@Composable
private fun Tabjes(gekozen: Tab, onKies: (Tab) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Op een smalle telefoon past icoon + woord niet naast elkaar: dan onder elkaar.
        val smal = maxWidth < 340.dp
        Row(
            Modifier
                .fillMaxWidth()
                .background(Kleur.Kaart.copy(alpha = 0.7f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(if (smal) 4.dp else 8.dp),
        ) {
            Tab.entries.forEach { t ->
                val aan = t == gekozen
                val icoon: List<Vorm> = when (t) {
                    Tab.Vandaag -> Icoontjes.tabVandaag
                    Tab.Werk -> Icoontjes.tabWerk
                    Tab.Onthoud -> Icoontjes.tabOnthoud
                }
                Box(
                    Modifier
                        .weight(1f)
                        .kies(aan) { onKies(t) }
                        .heightIn(min = if (smal) 60.dp else 52.dp)
                        .then(
                            if (aan) {
                                Modifier
                                    .shadow(6.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                                    .background(Kleur.Kaart, CircleShape)
                                    .glans(CircleShape, licht = 0.9f, donker = 0f)
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    val kleur = if (aan) Kleur.Inkt else Kleur.InktZacht
                    if (smal) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 6.dp)) {
                            Icoon(icoon, 22.dp)
                            Tekst(t.label, 15.sp, kleur = kleur, eenRegel = true)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icoon(icoon, 22.dp)
                            Tekst(t.label, 17.sp, kleur = kleur, eenRegel = true)
                        }
                    }
                }
            }
        }
    }
}

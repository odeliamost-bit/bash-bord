package nl.odelia.dashboard.ui.leren

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import nl.odelia.dashboard.logica.Ader
import nl.odelia.dashboard.logica.KastStaat
import nl.odelia.dashboard.logica.KringStaat
import nl.odelia.dashboard.logica.MotorStaat
import nl.odelia.dashboard.logica.Onderdeel
import nl.odelia.dashboard.logica.Simulator
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastels

/** De onderdelen van de Simulator. */
enum class SimDeel(val label: String) {
    Tekenen("✏️ Tekenen"),
    Kast("Groepenkast"),
    Kring("Stroomkring"),
    Motor("Motor"),
    Aders("Aderkleuren"),
    Formules("Formules"),
}

private val Aan = Color(0xFF5E9A70)
private val Uit = Color(0xFFC9BDB2)
private val Licht = Color(0xFFF7D27A)

/**
 * Simulator voor elektrotechniek (okt 2026): tik op een onderdeel voor uitleg,
 * zet schakelaars om en kijk wat er gebeurt. Alles werkt zonder internet.
 */
@Composable
fun SimulatorScherm(
    begin: SimDeel,
    onTerug: () -> Unit,
    onVoorlezen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var deel by remember { mutableStateOf(begin) }
    var gekozen by remember { mutableStateOf<Onderdeel?>(null) }
    var kast by remember { mutableStateOf(KastStaat()) }
    var kring by remember { mutableStateOf(KringStaat()) }
    var motor by remember { mutableStateOf(MotorStaat()) }

    @Composable
    fun Kop() {
        TerugBalk(if (begin == SimDeel.Formules) "Formules" else "Simulator", onTerug)
        if (begin != SimDeel.Formules) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SimDeel.entries.filter { it != SimDeel.Formules }.forEach { d ->
                    Keuze(d.label, d == deel, onClick = { deel = d; gekozen = null })
                }
            }
        }
    }

    // Tekenen vult het hele scherm (niet scrollen: daar sleep je onderdelen).
    if (deel == SimDeel.Tekenen) {
        Column(
            modifier.fillMaxSize().padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Kop()
            Tekenvlak(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(22.dp)))
        }
        return
    }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Kop()
            val kies: (Onderdeel) -> Unit = { gekozen = it }
            when (deel) {
                SimDeel.Kast -> KastDeel(kast, { kast = it }, kies)
                SimDeel.Kring -> KringDeel(kring, { kring = it }, kies)
                SimDeel.Motor -> MotorDeel(motor, { motor = it }, kies)
                SimDeel.Aders -> Simulator.aders.forEach { AderRegel(it) }
                SimDeel.Tekenen -> Unit
                SimDeel.Formules -> Simulator.formules.forEach { f ->
                    Paneel {
                        Tekst(f.titel, 20.sp)
                        f.regels.forEach { Tekst(it, 22.sp, kleur = Pastels.Blauw.groot) }
                        Tekst(f.uitleg, 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
                        ZachteKnop("🔊 Voorlezen", onClick = { onVoorlezen("${f.titel}. ${f.regels.joinToString(". ")}. ${f.uitleg}") })
                    }
                }
            }
            gekozen?.let { o -> InfoKaart(o, onVoorlezen = { onVoorlezen("${o.naam}. ${o.kort} ${o.uitleg}") }, onSluit = { gekozen = null }) }
            when (deel) {
                SimDeel.Kast -> Onderdelen(Simulator.Kast.onderdelen, kies)
                SimDeel.Kring -> Onderdelen(Simulator.Kring.onderdelen, kies)
                SimDeel.Motor -> Onderdelen(Simulator.Motor.onderdelen + Simulator.Stuur.onderdelen, kies)
                else -> Unit
            }
        }
    }
}

@Composable
private fun Paneel(inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(26.dp), Kleur.KaartSchaduw, hoogte = 10.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { inhoud() }
}

/** Een module in de kast of een knop: tik = omzetten. Groen hendeltje = aan. */
@Composable
private fun Module(naam: String, sub: String, aan: Boolean, modifier: Modifier = Modifier, onTik: () -> Unit) {
    val vorm = RoundedCornerShape(14.dp)
    Column(
        modifier
            .heightIn(min = 76.dp)
            .clip(vorm)
            .background(Color.White)
            .border(2.dp, if (aan) Aan else Uit, vorm)
            .clickable(onClick = onTik)
            .semantics { contentDescription = "$naam, ${if (aan) "aan" else "uit"}" }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.width(18.dp).height(22.dp).clip(RoundedCornerShape(5.dp)).background(Kleur.KaartPil), contentAlignment = if (aan) Alignment.TopCenter else Alignment.BottomCenter) {
            Box(Modifier.size(18.dp, 11.dp).clip(RoundedCornerShape(5.dp)).background(if (aan) Aan else Uit))
        }
        Tekst(naam, 12.sp, eenRegel = true)
        Tekst(sub, 10.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs, eenRegel = true)
    }
}

@Composable
private fun Lampje(naam: String, aan: Boolean, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .size(30.dp)
                .background(if (aan) Brush.radialGradient(listOf(Color(0xFFFFF4C2), Licht)) else Brush.radialGradient(listOf(Uit, Uit)), CircleShape)
                .semantics { contentDescription = "$naam: ${if (aan) "heeft stroom" else "geen stroom"}" },
        )
        Tekst(naam, 11.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht, eenRegel = true)
    }
}

@Composable
private fun KastDeel(kast: KastStaat, onKast: (KastStaat) -> Unit, onKies: (Onderdeel) -> Unit) {
    val s = Simulator.Kast
    Tekst("Tik op een schakelaar om hem om te zetten. Kijk welke lampjes uitgaan.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
    Paneel {
        Module("Hoofd", "0 / 1", kast.hoofd, Modifier.width(96.dp)) { onKast(kast.wisselHoofd()); onKies(s.deel("hoofdschakelaar")) }
        KastStaat.groepen.forEachIndexed { al, namen ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Module("Aardlek", "30 mA", kast.aardlek[al], Modifier.weight(1.2f)) { onKast(kast.wisselAardlek(al)); onKies(s.deel("als")) }
                namen.indices.forEach { g ->
                    Module("B16", "groep ${al * 3 + g + 1}", kast.automaten[al][g], Modifier.weight(1f)) { onKast(kast.wisselAutomaat(al, g)); onKies(s.deel("automaat")) }
                }
            }
            Row {
                Box(Modifier.weight(1.2f))
                namen.forEachIndexed { g, n -> Lampje(n, kast.heeftSpanning(al, g), Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun KringDeel(kring: KringStaat, onKring: (KringStaat) -> Unit, onKies: (Onderdeel) -> Unit) {
    val s = Simulator.Kring
    fun getal(x: Double, dec: Int) = String.format(Locale("nl", "NL"), "%.${dec}f", x)
    Tekst("Doe de schakelaar dicht. Kijk wat de meters zeggen.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
    Paneel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Module("Bron", "12 V", true, Modifier.weight(1f)) { onKies(s.deel("bron")) }
            Module("S1", if (kring.dicht) "dicht" else "open", kring.dicht, Modifier.weight(1f)) { onKring(kring.copy(dicht = !kring.dicht)); onKies(s.deel("schakelaar")) }
            Module("F1", if (kring.zekeringHeel) "heel" else "kapot", kring.zekeringHeel, Modifier.weight(1f)) { onKring(kring.copy(zekeringHeel = true)); onKies(s.deel("zekering")) }
            Lampje("Lamp H1", kring.stroomt, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Meter("A", "${getal(kring.stroom, 2)} A", "stroom", Modifier.weight(1f)) { onKies(s.deel("amp")) }
            Meter("V", "${getal(kring.spanningR1, 1)} V", "over R1 (4 Ω)", Modifier.weight(1f)) { onKies(s.deel("volt")) }
        }
        if (kring.stroomt) Tekst("I = U / R = 12 / (4 + 20) = 0,5 A", 15.sp, kleur = Pastels.Salie.groot)
        ZachteKnop(if (kring.zekeringHeel) "⚡ Maak kortsluiting" else "🔧 Vervang zekering F1", onClick = {
            onKring(kring.copy(zekeringHeel = !kring.zekeringHeel)); onKies(s.deel("zekering"))
        })
    }
}

@Composable
private fun Meter(letter: String, waarde: String, sub: String, modifier: Modifier, onTik: () -> Unit) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Pastels.Blauw.kaart)
            .clickable(onClick = onTik)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(32.dp).border(2.dp, Pastels.Blauw.donker, CircleShape), contentAlignment = Alignment.Center) {
            Tekst(letter, 15.sp, kleur = Pastels.Blauw.groot)
        }
        Column {
            Tekst(waarde, 18.sp, kleur = Pastels.Blauw.groot)
            Tekst(sub, 11.sp, dikte = FontWeight.Bold, kleur = Pastels.Blauw.label)
        }
    }
}

@Composable
private fun MotorDeel(m: MotorStaat, onMotor: (MotorStaat) -> Unit, onKies: (Onderdeel) -> Unit) {
    val h = Simulator.Motor
    val st = Simulator.Stuur
    Tekst("Druk op start. Laat los: blijft de motor draaien? Dat is zelfhouding.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
    Paneel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Lampje("H1", m.k1, Modifier.weight(1f))
            Column(Modifier.weight(3f)) {
                Tekst(if (m.draait) "⚙️ Motor M1 draait" else "Motor M1 staat stil", 18.sp, kleur = if (m.draait) Pastels.Salie.groot else Kleur.InktZacht)
                Tekst(
                    when {
                        m.f2Uit -> "F2 is afgeslagen: te lang te veel stroom."
                        !m.q1 -> "Q1 staat uit: alles is spanningsloos."
                        m.k1 -> "K1 is aangetrokken en houdt zichzelf vast."
                        else -> "K1 is afgevallen."
                    },
                    13.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Module("Start S2", "maakcontact", m.k1, Modifier.weight(1f)) { onMotor(m.start()); onKies(st.deel("s2")) }
            Module("Stop S1", "verbreek", !m.k1, Modifier.weight(1f)) { onMotor(m.stop()); onKies(st.deel("s1")) }
            Module("Q1", if (m.q1) "aan" else "uit", m.q1, Modifier.weight(1f)) { onMotor(m.wisselQ1()); onKies(h.deel("q1")) }
            Module("F2", if (m.f2Uit) "eruit" else "goed", !m.f2Uit, Modifier.weight(1f)) {
                onMotor(if (m.f2Uit) m.herstel() else m.overbelasting()); onKies(h.deel("f2"))
            }
        }
    }
}

@Composable
private fun AderRegel(a: Ader) {
    Paneel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val vulling = when (a.kleur) {
                "bruin" -> Brush.linearGradient(listOf(Color(0xFF8A4B1F), Color(0xFF8A4B1F)))
                "zwart" -> Brush.linearGradient(listOf(Color(0xFF232323), Color(0xFF232323)))
                "grijs" -> Brush.linearGradient(listOf(Color(0xFF8D949C), Color(0xFF8D949C)))
                "blauw" -> Brush.linearGradient(listOf(Color(0xFF1D5BBF), Color(0xFF1D5BBF)))
                else -> Brush.linearGradient(0f to Color(0xFFE2C11B), 0.5f to Color(0xFFE2C11B), 0.5f to Color(0xFF2F8A3C), 1f to Color(0xFF2F8A3C))
            }
            Box(Modifier.size(width = 22.dp, height = 44.dp).clip(RoundedCornerShape(11.dp)).background(vulling))
            Column {
                Tekst(a.naam, 19.sp)
                Tekst(a.uitleg, 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
            }
        }
    }
}

@Composable
private fun InfoKaart(o: Onderdeel, onVoorlezen: () -> Unit, onSluit: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Pastels.Geel.kaart, RoundedCornerShape(26.dp), Pastels.Geel.donker, hoogte = 10.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tekst(o.naam, 20.sp, kleur = Pastels.Geel.groot)
        Tekst(o.kort, 16.sp, kleur = Pastels.Geel.label)
        Tekst(o.uitleg, 15.sp, dikte = FontWeight.Bold, kleur = Pastels.Geel.klein)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ZachteKnop("🔊 Voorlezen", onClick = onVoorlezen)
            ZachteKnop("Sluiten", onClick = onSluit)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Onderdelen(lijst: List<Onderdeel>, onKies: (Onderdeel) -> Unit) {
    Tekst("Tik op een onderdeel voor uitleg", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lijst.forEach { o -> Keuze(o.naam, false, onClick = { onKies(o) }) }
    }
}

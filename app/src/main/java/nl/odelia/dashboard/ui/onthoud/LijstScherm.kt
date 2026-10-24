package nl.odelia.dashboard.ui.onthoud

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.Groep
import nl.odelia.dashboard.onthoud.OnthoudActies
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.onthoud.Samenvatting
import nl.odelia.dashboard.onthoud.Wanneer
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoerveld
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.KeuzeBalk
import nl.odelia.dashboard.ui.onderdelen.Knop
import nl.odelia.dashboard.ui.onderdelen.RondKnopje
import nl.odelia.dashboard.ui.onderdelen.SchermKop
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastels
import nl.odelia.dashboard.ui.thuis.VoorleesKnop
import nl.odelia.dashboard.ui.thuis.datumTekst

/** Alles wat je onthouden hebt: zoeken, alleen afspraken of to-do's, filteren per groep of project, en afvinken. */
@Composable
fun LijstScherm(
    staat: OnthoudStaat,
    acties: OnthoudActies,
    onOpen: (Afspraak) -> Unit,
    onNieuw: () -> Unit,
    onVoorlezen: (String) -> Unit,
    onTerug: () -> Unit,
    modifier: Modifier = Modifier,
    beginFilter: String? = null,
) {
    var zoek by rememberSaveable { mutableStateOf("") }
    // 0 = alles, 1 = alleen afspraken, 2 = alleen to-do's
    var welke by rememberSaveable { mutableIntStateOf(if (beginFilter == "todo") 2 else 0) }
    // "groep:School" of "project:PVB"; leeg = alles
    var filter by rememberSaveable { mutableStateOf(beginFilter?.takeIf { it != "todo" }) }
    var toonKlaar by rememberSaveable { mutableStateOf(false) }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SchermKop("Onthoud", onTerug) { RondKnopje(Icoontjes.plus, "Iets onthouden", onNieuw, maat = 52.dp) }
            Zoekbalk(zoek, { zoek = it }, acties)

            if (zoek.isNotBlank()) {
                Gevonden(staat, staat.zoek(zoek), acties, onOpen, onWissen = { zoek = "" })
                return@Column
            }

            KeuzeBalk(listOf("Alles", "Afspraken", "To-do"), welke) { welke = it }

            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Keuze("Alle groepen", aan = filter == null, onClick = { filter = null })
                Groep.entries.forEach { g ->
                    val sleutel = "groep:${g.name}"
                    Keuze(groepTekst(g), aan = filter == sleutel, onClick = { filter = if (filter == sleutel) null else sleutel })
                }
                staat.projecten.forEach { p ->
                    val sleutel = "project:$p"
                    Keuze(p, aan = filter == sleutel, onClick = { filter = if (filter == sleutel) null else sleutel }, achtergrond = Color(0xFFFFF1C6))
                }
            }

            val lijst = staat.afspraken.filter { a ->
                val type = when (welke) {
                    1 -> !a.todo
                    2 -> a.todo
                    else -> true
                }
                type && when {
                    filter == null -> true
                    filter!!.startsWith("groep:") -> staat.groep(a)?.name == filter!!.removePrefix("groep:")
                    else -> a.project == filter!!.removePrefix("project:")
                }
            }
            val titel = listOfNotNull(
                listOf(null, "📅 Afspraken", "✅ To-do")[welke],
                filter?.let { f -> if (f.startsWith("groep:")) groepTekst(Groep.valueOf(f.removePrefix("groep:"))) else f.removePrefix("project:") },
            ).joinToString(" · ")
            if (titel.isNotEmpty()) SamenvattingKaart(titel, staat.samenvatting(lijst), onVoorlezen)

            val open = lijst.filter { !it.klaar }
            if (open.isEmpty()) {
                Tekst(
                    if (lijst.isEmpty()) "Hier staat nog niets. Tik op + om iets te onthouden." else "Alles is klaar! 🎉",
                    17.sp, Modifier.padding(horizontal = 8.dp), dikte = FontWeight.Bold, kleur = Kleur.InktGrijs,
                )
            }
            Wanneer.entries.filter { it != Wanneer.Klaar }.forEach { w ->
                val stuk = open.filter { staat.wanneer(it) == w }.sortedWith(compareBy({ it.deadline }, { it.wat }))
                if (stuk.isNotEmpty()) Stuk(w.kop, stuk, staat, acties, onOpen)
            }
            val klaar = lijst.filter { it.klaar }
            if (klaar.isNotEmpty()) {
                Knop(if (toonKlaar) "Klaar verbergen" else "✓ Klaar (${klaar.size})", onClick = { toonKlaar = !toonKlaar }, modifier = Modifier.fillMaxWidth())
                if (toonKlaar) Stuk("Klaar", klaar.sortedByDescending { it.deadline }, staat, acties, onOpen)
            }
        }
    }
}

/** Zoekbalk met vergrootglas en 🎤 (ontwerp/Mensen.dc.html). */
@Composable
fun Zoekbalk(zoek: String, onZoek: (String) -> Unit, acties: OnthoudActies) {
    Invoerveld(zoek, onZoek, "Zoek een woord…", eenRegel = true) {
        if (zoek.isEmpty()) Icoon(Icoontjes.zoek, 24.dp)
        RondKnopje(Icoontjes.microfoon, "Zoeken met je stem", onClick = { acties.inspreken(onZoek) })
    }
}

/** "🔍 3 gevonden" met de afspraken eronder. */
@Composable
fun Gevonden(staat: OnthoudStaat, gevonden: List<Afspraak>, acties: OnthoudActies, onOpen: (Afspraak) -> Unit, onWissen: () -> Unit) {
    Kaart {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tekst("🔍 ${gevonden.size} gevonden", 19.sp, Modifier.weight(1f), kleur = Color(0xFF5A3426))
            Knop("✕ wissen", onClick = onWissen)
        }
        if (gevonden.isEmpty()) {
            Tekst("Niets gevonden. Probeer een ander woord.", 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        }
        gevonden.forEach { a -> AfspraakRegel(a, staat, onOpen = { onOpen(a) }, onKlaar = { acties.zetKlaar(a.id, it) }) }
    }
}

@Composable
private fun Stuk(kop: String, lijst: List<Afspraak>, staat: OnthoudStaat, acties: OnthoudActies, onOpen: (Afspraak) -> Unit) {
    Kaart {
        Tekst(kop, 19.sp, kleur = if (kop == Wanneer.TeLaat.kop) Kleur.Koraal else Kleur.Inkt)
        lijst.forEach { a -> AfspraakRegel(a, staat, onOpen = { onOpen(a) }, onKlaar = { acties.zetKlaar(a.id, it) }) }
    }
}

/** Samenvatting per onderwerp: hoeveel nog te doen, hoeveel klaar, en wat het eerst moet. */
@Composable
private fun SamenvattingKaart(titel: String, s: Samenvatting, onVoorlezen: (String) -> Unit) {
    val p = Pastels.Geel
    val eerst = s.eerstvolgende
    val zin = buildString {
        append("$titel. ")
        append(if (s.open == 1) "1 ding nog doen" else "${s.open} dingen nog doen")
        if (s.klaar > 0) append(", ${s.klaar} klaar")
        append(". ")
        if (eerst != null) append("Eerst: ${eerst.wat}, ${datumTekst(eerst.deadline!!)}.")
    }
    Column(
        Modifier
            .fillMaxWidth()
            .klei(p.kaart, RoundedCornerShape(30.dp), p.donker)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tekst(titel, 22.sp, kleur = p.groot)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Getal(s.open, "nog doen", Kleur.Koraal)
            Getal(s.klaar, "klaar", Color(0xFF3F7A58))
        }
        if (eerst != null) {
            Tekst("Eerst: ${eerst.wat} (${datumTekst(eerst.deadline!!)})", 16.sp, dikte = FontWeight.Bold, kleur = p.label)
        }
        VoorleesKnop({ onVoorlezen(zin) })
    }
}

@Composable
private fun Getal(n: Int, onder: String, kleur: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Tekst(n.toString(), 30.sp, kleur = kleur, regel = 32.sp)
        Tekst(onder, 13.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktGrijs)
    }
}

/** Crème klei-kaart met ruimte tussen de regels. */
@Composable
fun Kaart(inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        inhoud()
    }
}

/** Grote lege-plek-tekst in een lichte kaart. */
@Composable
fun LegeKaart(tekst: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Kleur.Kaart.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
            .padding(18.dp),
    ) {
        Tekst(tekst, 16.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
    }
}

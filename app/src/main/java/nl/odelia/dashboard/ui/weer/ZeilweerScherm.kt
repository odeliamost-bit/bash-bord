package nl.odelia.dashboard.ui.weer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlin.math.roundToInt
import nl.odelia.dashboard.logica.Plek
import nl.odelia.dashboard.logica.Uurweer
import nl.odelia.dashboard.logica.Weermodel
import nl.odelia.dashboard.logica.Zeilweer
import nl.odelia.dashboard.logica.alsKlok
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.MicKnop
import nl.odelia.dashboard.ui.onderdelen.OnlineVraag
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.rememberVraag
import nl.odelia.dashboard.ui.theme.Kleur

private val Diep = Color(0xFF23465E)
private val Zacht = Color(0xFF4F6B7E)
private val dagNamen = listOf("Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag", "Zondag")

/**
 * Zeilweer (ontwerp/Weer.dc.html): wind, stoten en regen voor de middag, van 5 weermodellen naast elkaar.
 * Online gaan gebeurt alleen nadat Odelia "ja" heeft gezegd.
 */
@Composable
fun ZeilweerScherm(staat: AppStaat, acties: AppActies, onTerug: () -> Unit, modifier: Modifier = Modifier) {
    val z = staat.zeilweer
    val vraag = rememberVraag()
    var plekKiezen by remember { mutableStateOf(false) }

    fun haalWeer() = vraag.stel(
        OnlineVraag(
            titel = "Mag de app even online?",
            uitleg = "Om het weer op te halen bij Open-Meteo (gratis weerdienst).\nAlleen je plek gaat mee: ${z.plek?.naam.orEmpty()}.",
            ja = "Ja, haal op",
            doe = acties::haalZeilweer,
        ),
    )

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk("Zeilweer ⛵", onTerug)

            val plek = z.plek
            if (plek == null || plekKiezen) {
                PlekKiezen(z.plekken, z.bezig, z.fout, acties, onVraag = vraag::stel, onGekozen = {
                    acties.kiesPlek(it)
                    plekKiezen = false
                })
                if (plek != null) {
                    ZachteKnop("Toch ${plek.naam} houden", onClick = { plekKiezen = false }, Modifier.fillMaxWidth())
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Tekst("📍 ${plek.naam}", 18.sp, kleur = Diep)
                        Tekst("🕛 ${Zeilweer.UREN.first}:00 – ${Zeilweer.UREN.last}:00", 15.sp, dikte = FontWeight.Bold, kleur = Zacht)
                    }
                    ZachteKnop("andere plek", onClick = { plekKiezen = true }, kleur = Zacht)
                }

                val bericht = z.bericht
                if (bericht == null) {
                    Kaart {
                        Tekst("Nog geen weer opgehaald.", 17.sp, kleur = Diep)
                        Tekst("De app gaat pas online als jij op de knop drukt en \"ja\" zegt.", 15.sp, dikte = FontWeight.Bold, kleur = Zacht)
                    }
                    GroteKnop(if (z.bezig) "Even ophalen…" else "Weer ophalen", onClick = { if (!z.bezig) haalWeer() })
                } else {
                    WeerBericht(bericht, staat.nu.toLocalDate())
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Tekst(
                            "Opgehaald om ${alsKlok(bericht.opgehaald.toLocalTime())}",
                            15.sp, Modifier.weight(1f), dikte = FontWeight.Bold, kleur = Zacht,
                        )
                        ZachteKnop(if (z.bezig) "bezig…" else "🔄 Ververs", onClick = { if (!z.bezig) haalWeer() }, kleur = Diep)
                    }
                }
                z.fout?.let { Tekst(it, 16.sp, kleur = Kleur.KoraalDiep) }
            }

            Tekst(
                "Bron: Open-Meteo. Dit is een voorspelling; het kan anders uitpakken. Kijk ook naar de lucht en luister naar je club. ⛵",
                13.sp, dikte = FontWeight.Bold, kleur = Zacht,
            )
        }
    }
    vraag.Toon()
}

@Composable
private fun PlekKiezen(
    plekken: List<Plek>,
    bezig: Boolean,
    fout: String?,
    acties: AppActies,
    onVraag: (OnlineVraag) -> Unit,
    onGekozen: (Plek) -> Unit,
) {
    var naam by remember { mutableStateOf("") }
    fun zoek() {
        if (naam.isBlank()) return
        onVraag(
            OnlineVraag(
                titel = "Mag de app even online?",
                uitleg = "Om de plek \"${naam.trim()}\" op te zoeken bij Open-Meteo. Alleen deze naam gaat mee.",
                ja = "Ja, zoek",
                doe = { acties.zoekPlek(naam) },
            ),
        )
    }
    Kaart {
        Tekst("Waar ga je zeilen?", 20.sp, kleur = Diep)
        Tekst("Een plaats of meer is genoeg. Je plek blijft op je telefoon.", 15.sp, dikte = FontWeight.Bold, kleur = Zacht)
        Invoer(naam, onWijzig = { naam = it }, hint = "Bijvoorbeeld een meer of dorp") {
            MicKnop(onClick = { acties.inspreken { naam = it.trim() } }, maat = 44.dp)
        }
        GroteKnop(if (bezig) "Even zoeken…" else "Zoeken", onClick = { if (!bezig) zoek() }, icoon = Icoontjes.vinkje)
        fout?.let { Tekst(it, 16.sp, kleur = Kleur.KoraalDiep) }
        plekken.forEach { p ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .clickable(onClickLabel = "Kies deze plek") { onGekozen(p) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Tekst("📍 ${p.naam}", 17.sp, kleur = Diep)
                if (p.uitleg.isNotBlank()) Tekst(p.uitleg, 14.sp, dikte = FontWeight.Bold, kleur = Zacht)
            }
        }
    }
}

@Composable
private fun WeerBericht(bericht: nl.odelia.dashboard.logica.Weerbericht, vandaag: LocalDate) {
    val dagen = bericht.perModel.values.flatten().map { it.tijd.toLocalDate() }.distinct().sorted().filter { !it.isBefore(vandaag) }.take(3)
    var dag by remember { mutableStateOf(dagen.firstOrNull() ?: vandaag) }
    var model by remember { mutableStateOf<Weermodel?>(null) }
    val middag = Zeilweer.middag(bericht, dag)
    val gekozen = model?.let { m -> middag.filterKeys { it == m } } ?: middag
    val uren = if (gekozen.size == 1) gekozen.values.first() else Zeilweer.samen(gekozen)
    val oordeel = Zeilweer.oordeel(uren)
    val gemWind = uren.mapNotNull { it.wind }.average().takeIf { !it.isNaN() } ?: 0.0
    val (achter, inkt) = when (oordeel.emoji) {
        "⚠️" -> Color(0xFFFBE0E0) to Color(0xFFA23B3B)
        "😴" -> Color(0xFFEEF0F4) to Color(0xFF4F6B7E)
        "🌧️" -> Color(0xFFE3ECF8) to Color(0xFF2F5670)
        else -> Color(0xFFE3F2E5) to Color(0xFF2F6B45)
    }

    // Dagen
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        dagen.forEach { d ->
            val naam = when (d) {
                vandaag -> "Vandaag"
                vandaag.plusDays(1) -> "Morgen"
                else -> dagNamen[d.dayOfWeek.value - 1]
            }
            Knopje(naam, d == dag, Modifier.weight(1f)) { dag = d }
        }
    }

    // Modellen
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Knopje("✨ Alles", model == null) { model = null }
        middag.keys.forEach { m -> Knopje("${m.vlag} ${m.naam}", model == m) { model = m } }
    }

    // Oordeel
    Column(
        Modifier
            .fillMaxWidth()
            .klei(achter, RoundedCornerShape(26.dp), inkt, hoogte = 8.dp)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Tekst("${oordeel.emoji} ${oordeel.tekst}", 24.sp, kleur = inkt)
        Tekst(
            "gemiddeld ${gemWind.roundToInt()} km/u · ${Zeilweer.bft(gemWind)} Bft",
            16.sp, dikte = FontWeight.ExtraBold, kleur = inkt,
        )
        if (model == null && middag.size > 1) {
            val eens = Zeilweer.eens(middag)
            Tekst(
                if (eens) "✓ de modellen zijn het eens" else "≠ de modellen verschillen",
                15.sp, dikte = FontWeight.ExtraBold, kleur = if (eens) Color(0xFF2F6B45) else Color(0xFFA0583C),
            )
        }
    }

    // Per uur
    Kaart {
        Tekst(if (model == null) "Per uur (alle modellen samen)" else "Per uur (${model?.naam})", 17.sp, kleur = Diep)
        if (uren.isEmpty()) Tekst("Geen weer voor deze middag.", 15.sp, dikte = FontWeight.Bold, kleur = Zacht)
        uren.forEach { UurRegel(it) }
        Tekst("Bft = Beaufort · ↑ = windstoten in km/u · 💧 = regen in mm", 13.sp, dikte = FontWeight.Bold, kleur = Zacht)
    }

    // Modellen naast elkaar
    if (middag.size > 1) {
        Kaart {
            Tekst("⚖️ Modellen naast elkaar", 17.sp, kleur = Diep)
            Row {
                Tekst("model", 13.sp, Modifier.weight(1.6f), dikte = FontWeight.ExtraBold, kleur = Zacht)
                Tekst("wind", 13.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Zacht)
                Tekst("stoot", 13.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Zacht)
                Tekst("regen", 13.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Zacht)
            }
            middag.forEach { (m, u) ->
                val wind = u.mapNotNull { it.wind }.average().takeIf { !it.isNaN() }
                val stoot = u.mapNotNull { it.stoot }.maxOrNull()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (model == m) Color(0xFFDCEBF5) else Color.White)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Tekst("${m.vlag} ${m.naam}", 15.sp, Modifier.weight(1.6f), kleur = Diep)
                    Tekst(wind?.let { "${Zeilweer.bft(it)} Bft" } ?: "–", 15.sp, Modifier.weight(1f), kleur = Diep)
                    Tekst(
                        stoot?.roundToInt()?.toString() ?: "–", 15.sp, Modifier.weight(1f),
                        kleur = if ((stoot ?: 0.0) > Zeilweer.TE_VEEL_STOOT) Color(0xFFC0392B) else Diep,
                    )
                    Tekst("${"%.1f".format(u.sumOf { it.regen ?: 0.0 })} mm", 15.sp, Modifier.weight(1f), kleur = Diep)
                }
            }
        }
    }
}

@Composable
private fun UurRegel(u: Uurweer) {
    val teVeel = (u.stoot ?: 0.0) > Zeilweer.TE_VEEL_STOOT
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (teVeel) Color(0xFFFBE0E0) else Color.White)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tekst("${u.tijd.hour}:00", 16.sp, Modifier.width(52.dp), kleur = Diep)
        u.richting?.let { r ->
            Icoon(Icoontjes.windPijl, 22.dp, Modifier.rotate((r + 180).toFloat()))
            Tekst(Zeilweer.richtingNaam(r), 15.sp, Modifier.width(30.dp), kleur = Zacht)
        }
        Tekst(u.wind?.let { "${Zeilweer.bft(it)} Bft" } ?: "–", 16.sp, Modifier.weight(1f), kleur = Diep)
        Tekst(u.wind?.let { "${it.roundToInt()}" } ?: "", 15.sp, dikte = FontWeight.Bold, kleur = Zacht)
        Tekst(u.stoot?.let { "↑${it.roundToInt()}" } ?: "", 15.sp, kleur = if (teVeel) Color(0xFFC0392B) else Zacht)
        Tekst(u.regen?.let { "💧${"%.1f".format(it)}" } ?: "", 14.sp, dikte = FontWeight.Bold, kleur = Zacht)
    }
}

@Composable
private fun Kaart(inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(26.dp), Kleur.KaartSchaduw)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { inhoud() }
}

@Composable
private fun Knopje(tekst: String, aan: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .background(if (aan) hoekVerloop(Color(0xFF7FB8DE), Color(0xFF3F7FB0)) else androidx.compose.ui.graphics.SolidColor(Kleur.Kaart))
            .selectable(selected = aan, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 15.sp, kleur = if (aan) Color.White else Color(0xFF2F5670))
    }
}

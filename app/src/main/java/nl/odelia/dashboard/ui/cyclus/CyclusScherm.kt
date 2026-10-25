package nl.odelia.dashboard.ui.cyclus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin
import nl.odelia.dashboard.logica.CyclusStand
import nl.odelia.dashboard.logica.Gevoel
import nl.odelia.dashboard.logica.ringDagen
import nl.odelia.dashboard.logica.roodTot
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Schakelaar
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.cirkel
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.rechthoek
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.thuis.VoorleesKnop

private val Bruin = Color(0xFF5A3426)
private val BruinZacht = Color(0xFF8A5A48)
private val Roze = Color(0xFFF07FA0)
private val RozeDiep = Color(0xFFC2457A)
private val Leeg = Color(0xFFF3E3D3)
private val maanden = listOf("jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec")

private val druppel = "M12 3s6 6.5 6 11a6 6 0 0 1-12 0c0-4.5 6-11 6-11z"

/** "10 nov" */
fun kortDatum(d: LocalDate) = "${d.dayOfMonth} ${maanden[d.monthValue - 1]}"

/** De tekst onder "dag 12" in de ring. */
fun middenTekst(s: CyclusStand): String {
    val tot = s.dagenTot
    return when {
        s.dag == null -> "tik op de knop als je ongesteld wordt"
        s.ongesteld -> "je bent ongesteld"
        tot == null -> ""
        tot > 1 -> "over $tot dagen ongesteld"
        tot == 1L -> "morgen misschien ongesteld"
        else -> "kan elk moment komen"
    }
}

/** Wat de 🔊-knop voorleest. */
fun cyclusVoorlees(s: CyclusStand): String = buildString {
    if (s.dag == null) {
        append("Je hebt nog niets ingevuld. Tik op Ik ben ongesteld op de eerste dag.")
        return@buildString
    }
    append("Vandaag is dag ${s.dag}. ")
    append(middenTekst(s).replaceFirstChar { it.uppercase() }).append(". ")
    s.volgende?.let { append("De volgende keer is ongeveer ${it.dayOfMonth} ${maandVol[it.monthValue - 1]}. ") }
    append("Je cyclus is ongeveer ${s.lengte} dagen.")
    if (s.nogAanHetLeren) append(" De app leert nog. Na een paar keer wordt het preciezer.")
}

private val maandVol = listOf(
    "januari", "februari", "maart", "april", "mei", "juni",
    "juli", "augustus", "september", "oktober", "november", "december",
)

/**
 * Cyclus (ontwerp/Cyclus.dc.html). Een schatting, geen medisch advies.
 * Gevoelig: alles blijft op de telefoon (niet in de back-up), nooit online.
 */
@Composable
fun CyclusScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = staat.cyclus
    val vandaag = staat.nu.toLocalDate()
    val stand = data.stand(vandaag)

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            TerugBalk("Cyclus", onTerug) {
                Box(
                    Modifier
                        .size(52.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Color(0xFFDE7FA1), spotColor = Color(0xFFDE7FA1))
                        .background(hoekVerloop(Color(0xFFF6B3C8), Color(0xFFDE7FA1)), CircleShape)
                        .glans(CircleShape, licht = 0.5f),
                    contentAlignment = Alignment.Center,
                ) { Icoon(listOf(Vorm(druppel, vul = Color.White)), 26.dp) }
            }

            RingKaart(stand, onVoorlezen = {
                if (!acties.voorlezen(cyclusVoorlees(stand))) onMelding("Voorlezen lukt nog niet. Staat er een Nederlandse stem op je telefoon?")
            })

            GroteKnop(stand.ongesteld) {
                if (stand.ongesteld) {
                    acties.wijzigCyclus { it.klaar(vandaag) }
                    onMelding("Fijn! Bewaard.")
                } else {
                    acties.wijzigCyclus { it.begin(vandaag) }
                    onMelding("Bewaard: dag 1 is vandaag.")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Vakje(
                    Modifier.weight(1f), Color(0xFFFBE0E8), Color(0xFFC86482), Color(0xFF8A4A60),
                    listOf(
                        Vorm(rechthoek(3f, 5f, 18f, 16f, 4f), vul = Color(0xFFF4B0C8), lijn = Color(0xFFDE7FA1)),
                        Vorm("M3 10h18M8 3v4M16 3v4", lijn = Color(0xFFDE7FA1)),
                    ),
                    "volgende keer",
                    stand.volgende?.let { "± " + kortDatum(it) } ?: "nog niet",
                    stand.vroegst?.let { v -> stand.laatst?.let { l -> "tussen ${kortDatum(v)} en ${kortDatum(l)}" } },
                )
                Vakje(
                    Modifier.weight(1f), Color(0xFFEFE7FB), Color(0xFF8264C8), Color(0xFF5E4A8A),
                    listOf(
                        Vorm(cirkel(12f, 12f, 9f), vul = Color(0xFFD2BDF0), lijn = Color(0xFFA88AD8)),
                        Vorm("M12 7v5l3 2", lijn = Color(0xFFA88AD8), dikte = 2.2f),
                    ),
                    "mijn cyclus",
                    "${stand.lengte} dagen",
                    if (stand.nogAanHetLeren) "de app leert nog" else null,
                )
            }

            if (stand.ongesteld) {
                Gevoelens(data.gevoel[vandaag].orEmpty()) { g -> acties.wijzigCyclus { it.wisselGevoel(vandaag, g) } }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Kleur.Kaart.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
                        .stippelRand(Color(0xFFE9B8C8), 26.dp)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Tekst("😣", 28.sp)
                    Tekst("Ben je ongesteld? Dan kun je hier tikken hoe je je voelt.", 16.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF8A4A60))
                }
            }

            SeintjeKnop(data.seintje) {
                val aan = !data.seintje
                acties.wijzigCyclus { it.copy(seintje = aan) }
                onMelding(if (aan) "Seintje staat aan" else "Seintje staat uit")
            }

            Tekst(
                "🔒 Dit blijft alleen op je telefoon. Het is een schatting, geen medisch advies.",
                14.sp, Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                dikte = FontWeight.Bold, kleur = BruinZacht, uitlijning = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RingKaart(s: CyclusStand, onVoorlezen: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
            Ring(s, Modifier.size(250.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Tekst("vandaag", 16.sp, dikte = FontWeight.ExtraBold, kleur = BruinZacht)
                Tekst(s.dag?.let { "dag $it" } ?: "dag –", 48.sp, kleur = Bruin, regel = 52.sp)
                Tekst(
                    middenTekst(s), 16.sp, Modifier.widthIn(max = 150.dp).padding(top = 4.dp),
                    dikte = FontWeight.ExtraBold, kleur = RozeDiep, regel = 19.sp, uitlijning = TextAlign.Center,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(14.dp).background(Roze, RoundedCornerShape(3.dp)))
            Tekst("ongesteld", 15.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF5A4D4A))
        }
        VoorleesKnop(onVoorlezen, Modifier.heightIn(min = 48.dp))
    }
}

/** Ring met een vakje per dag. Roze = ongesteld, het vakje van vandaag is dikker, met een bolletje. */
@Composable
private fun Ring(s: CyclusStand, modifier: Modifier) {
    val dagen = s.ringDagen()
    val rood = s.roodTot()
    val vandaag = s.dag?.takeIf { it <= dagen }
    Canvas(modifier) {
        val schaal = size.minDimension / 250f
        val r = 100f * schaal
        val midden = Offset(size.width / 2, size.height / 2)
        val stap = 360f / dagen
        val gat = 3f * schaal / r * (180f / Math.PI.toFloat())
        for (i in 1..dagen) {
            val dik = (if (i == vandaag) 30f else 22f) * schaal
            drawArc(
                color = if (i <= rood && s.dag != null) Roze else Leeg,
                startAngle = -90f + (i - 1) * stap,
                sweepAngle = stap - gat,
                useCenter = false,
                topLeft = Offset(midden.x - r, midden.y - r),
                size = Size(2 * r, 2 * r),
                style = Stroke(dik),
            )
        }
        if (vandaag != null) {
            val hoek = ((vandaag - 0.5) / dagen) * 2 * Math.PI - Math.PI / 2
            val p = Offset(midden.x + r * cos(hoek).toFloat(), midden.y + r * sin(hoek).toFloat())
            drawCircle(Color(0x59964632), 15f * schaal, p.copy(y = p.y + 3f * schaal))
            drawCircle(Color.White, 15f * schaal, p)
            drawCircle(Kleur.Koraal, 15f * schaal, p, style = Stroke(5f * schaal))
        }
    }
}

@Composable
private fun GroteKnop(ongesteld: Boolean, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(30.dp)
    val (licht, donker) = if (ongesteld) Color(0xFFA8D6B4) to Color(0xFF6FAE84) else Color(0xFFF6A3BE) to Color(0xFFDE6F95)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .shadow(14.dp, vorm, ambientColor = donker, spotColor = donker)
            .clip(vorm)
            .background(hoekVerloop(licht, donker))
            .glans(vorm, licht = 0.4f, donker = 0.1f, boven = 4.dp, onder = 5.dp)
            .toggleable(value = ongesteld, role = Role.Switch, onValueChange = { onClick() })
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(54.dp).background(Color.White.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
            if (ongesteld) Icoon(Icoontjes.vinkje, 30.dp) else Icoon(listOf(Vorm(druppel, vul = Color.White)), 30.dp)
        }
        Column {
            Tekst(if (ongesteld) "Ik ben klaar" else "Ik ben ongesteld", 22.sp, kleur = Color.White, regel = 25.sp)
            Tekst(
                if (ongesteld) "tik als het over is" else "tik op de eerste dag",
                15.sp, dikte = FontWeight.Bold, kleur = Color.White.copy(alpha = 0.92f),
            )
        }
    }
}

@Composable
private fun Vakje(
    modifier: Modifier,
    kaart: Color,
    schaduw: Color,
    labelKleur: Color,
    icoon: List<Vorm>,
    label: String,
    groot: String,
    klein: String?,
) {
    Column(
        modifier
            .klei(kaart, RoundedCornerShape(26.dp), schaduw, hoogte = 10.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icoon(icoon, 34.dp, schaduw = schaduw)
        Tekst(label, 15.sp, dikte = FontWeight.ExtraBold, kleur = labelKleur)
        Tekst(groot, 24.sp, kleur = Bruin, regel = 28.sp)
        if (klein != null) Tekst(klein, 13.sp, dikte = FontWeight.Bold, kleur = labelKleur)
    }
}

@Composable
private fun Gevoelens(gekozen: Set<Gevoel>, onKies: (Gevoel) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            Tekst("Hoe voel ik me vandaag?", 21.sp, kleur = Bruin)
            Tekst("alleen als ik ongesteld ben", 15.sp, dikte = FontWeight.Bold, kleur = BruinZacht)
        }
        Gevoel.entries.chunked(3).forEach { rij ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rij.forEach { g ->
                    val aan = g in gekozen
                    val vorm = RoundedCornerShape(22.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 86.dp)
                            .shadow(6.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                            .clip(vorm)
                            .background(if (aan) Color(0xFFFBD9E3) else Color.White)
                            .border(3.dp, if (aan) Color(0xFFDE7FA1) else Color.Transparent, vorm)
                            .toggleable(value = aan, role = Role.Checkbox, onValueChange = { onKies(g) })
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    ) {
                        Tekst(g.emoji, 30.sp)
                        Tekst(g.label, 14.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt, eenRegel = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeintjeKnop(aan: Boolean, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(26.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .klei(Color(0xFFE3EDDF), vorm, Color(0xFF5A8264), hoogte = 10.dp)
            .clip(vorm)
            .toggleable(value = aan, role = Role.Switch, onValueChange = { onClick() })
            .semantics { contentDescription = "Seintje 2 dagen ervoor, " + if (aan) "aan" else "uit" }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icoon(
            listOf(
                Vorm("M6 9a6 6 0 0 1 12 0c0 6 2.5 8 2.5 8h-17S6 15 6 9z", vul = Color(0xFFA8D6B4), lijn = Color(0xFF6FAE84)),
                Vorm(cirkel(12f, 20f, 2f), vul = Color(0xFF6FAE84)),
            ),
            34.dp, schaduw = Color(0xFF6FAE84),
        )
        Column(Modifier.weight(1f)) {
            Tekst("Seintje 2 dagen ervoor", 18.sp, kleur = Color(0xFF2F5E43))
            Tekst("\"Neem verband mee\"", 14.sp, dikte = FontWeight.Bold, kleur = Color(0xFF4F7A5E))
        }
        Schakelaar(aan)
    }
}

/** Gestippeld randje, zoals CSS `border: 2px dashed`. */
private fun Modifier.stippelRand(kleur: Color, hoek: Dp): Modifier = drawBehind {
    val d = 2.dp.toPx()
    drawRoundRect(
        kleur,
        topLeft = Offset(d / 2, d / 2),
        size = Size(size.width - d, size.height - d),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(hoek.toPx()),
        style = Stroke(d, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))),
    )
}

package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.Instellingen
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.Overzicht
import nl.odelia.dashboard.logica.alsBreuk
import nl.odelia.dashboard.logica.alsKlok
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.Scherm
import nl.odelia.dashboard.ui.onderdelen.IcoonBlok
import nl.odelia.dashboard.ui.onderdelen.TijdKiezer
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.rondVerloop
import nl.odelia.dashboard.ui.onderdelen.voet
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels

/** Tabje "Vandaag": grote in/uit-knop en vier tegels. */
@Composable
fun VandaagTab(staat: AppStaat, acties: AppActies, onGaNaar: (Scherm) -> Unit) {
    val klok = staat.klok
    val overzicht = Overzicht.van(staat.onthoud, klok.nu.toLocalDate())
    val open = staat.onthoud.count { !it.klaar }
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        klok.vergeten?.let { begin -> VergetenKaart(begin, onEind = acties::sluitVergeten) }
        KlokKaart(klok, acties::wisselKlok)
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatTegel(
                    Pastels.Salie, Icoontjes.klok, "Vandaag", "${alsBreuk(klok.vandaag.uren)} u",
                    when {
                        klok.isIn -> "nog bezig"
                        klok.vandaag.eind != null -> "klaar"
                        klok.vandaag.soort == DagSoort.Vrij -> "vrij"
                        klok.vandaag.soort == DagSoort.Ziek -> "ziek"
                        else -> "nog niet begonnen"
                    },
                    Modifier.weight(1f),
                )
                StatTegel(
                    Pastels.Geel, Icoontjes.kalender, "Deze week", "${alsBreuk(klok.weekUren)} u",
                    "van ${alsBreuk(klok.weekdoel)} uur", Modifier.weight(1f),
                )
            }
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatTegel(
                    Pastels.Koraal, Icoontjes.bladwijzer, "Onthoud", "$open",
                    "${overzicht.vandaag.size} vandaag", Modifier.weight(1f),
                    onClick = { onGaNaar(Scherm.Lijst()) },
                )
                StatTegel(
                    Pastels.Blauw, Icoontjes.mail, "Mail Kim", "vr", mailMoment(klok), Modifier.weight(1f),
                    onClick = { onGaNaar(Scherm.Urenmail()) },
                )
            }
        }
    }
}

/** Wanneer staat de urenmail klaar? */
private fun mailMoment(klok: KlokStaat): String {
    if (klok.nu.dayOfWeek != DayOfWeek.FRIDAY) return "op vrijdag"
    val eind = klok.vandaag.eind ?: return if (klok.isIn) "na uitklokken" else "op vrijdag"
    val klaar = eind.plusMinutes(Instellingen.URENMAIL_NA_MINUTEN)
    return if (klok.nu.toLocalTime().isBefore(klaar)) "klaar om ${alsKlok(klaar)}" else "staat klaar"
}

/** Kleuren van de klok-kaart: groen = aan het werk, roze = niet aan het werk. */
private class KlokKleur(
    val kaart: Color, val schaduw: Color,
    val knop: List<Color>, val ring: Color, val voet: Color,
    val tekst: Color, val tekstDiep: Color,
)

private val Groen = KlokKleur(
    Color(0xFFE3EDDF), Color(0xFF5A8264),
    listOf(Color(0xFFA6D6B3), Color(0xFF6FAE84), Color(0xFF4F9168)), Color(0xFFF4F9F1), Color(0xFF3F7A58),
    Color(0xFF2F5E43), Color(0xFF24452F),
)
private val Roze = KlokKleur(
    Color(0xFFFBE0DE), Color(0xFFBE646E),
    listOf(Color(0xFFF7B9CB), Color(0xFFE27FA0), Color(0xFFC9527D)), Color(0xFFFDF3F2), Color(0xFFA93A66),
    Color(0xFF7A2D4E), Color(0xFF5C1F3A),
)

@Composable
private fun KlokKaart(klok: KlokStaat, onWissel: () -> Unit) {
    val k = if (klok.isIn) Groen else Roze
    val dag = klok.vandaag
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val smal = maxWidth < 340.dp
        Column(
            Modifier
                .fillMaxWidth()
                .klei(k.kaart, RoundedCornerShape(30.dp), k.schaduw)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                KlokKnop(klok.isIn, k, if (smal) 112.dp else 124.dp, onWissel)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    when {
                        klok.isIn -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    Modifier.size(20.dp).background(Color(0x383F9A64), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(Modifier.size(12.dp).background(Color(0xFF3F9A64), CircleShape))
                                }
                                Tekst("Aan het werk", 16.sp, dikte = FontWeight.ExtraBold, kleur = k.tekst)
                            }
                            Tekst("sinds", 15.sp, dikte = FontWeight.Bold, kleur = Color(0xFF3D5E4A))
                            Tekst(klok.sinds?.let(::alsKlok) ?: "", 44.sp, kleur = k.tekstDiep, regel = 44.sp)
                        }
                        dag.eind != null -> {
                            Tekst("Klaar voor vandaag", 16.sp, dikte = FontWeight.ExtraBold, kleur = k.tekst)
                            Tekst("gewerkt", 15.sp, dikte = FontWeight.Bold, kleur = k.tekst)
                            Tekst("${alsBreuk(dag.uren)} u", 44.sp, kleur = k.tekstDiep, regel = 44.sp)
                        }
                        dag.soort == DagSoort.Vrij || dag.soort == DagSoort.Ziek -> {
                            Tekst("Vandaag ben je", 16.sp, dikte = FontWeight.ExtraBold, kleur = k.tekst)
                            Tekst(if (dag.soort == DagSoort.Vrij) "vrij" else "ziek", 44.sp, kleur = k.tekstDiep, regel = 44.sp)
                        }
                        else -> {
                            Tekst("Nog niet begonnen", 16.sp, dikte = FontWeight.ExtraBold, kleur = k.tekst)
                            Tekst("Tik op IN als je begint.", 15.sp, dikte = FontWeight.Bold, kleur = k.tekst)
                        }
                    }
                }
            }
            if (klok.isIn) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Tekst(
                        "al ${alsBreuk(dag.uren)} uur gewerkt",
                        15.sp,
                        Modifier
                            .shadow(3.dp, CircleShape, ambientColor = k.schaduw, spotColor = k.schaduw)
                            .background(k.ring, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        dikte = FontWeight.ExtraBold,
                        kleur = k.tekst,
                    )
                    Tekst("(de 45 min pauze is er al af)", 13.sp, Modifier.padding(start = 6.dp), dikte = FontWeight.Bold, kleur = Color(0xFF3D5E4A))
                }
            }
        }
    }
}

/** De grote ronde in/uit-knop met 3D-voetje. */
@Composable
private fun KlokKnop(isIn: Boolean, k: KlokKleur, maat: Dp, onWissel: () -> Unit) {
    val rond: Shape = CircleShape
    Box(
        Modifier
            .size(maat)
            .voet(k.voet, rond, 10.dp)
            .shadow(14.dp, rond, ambientColor = k.voet, spotColor = k.voet)
            .clip(rond)
            .background(rondVerloop(0f to k.knop[0], 0.55f to k.knop[1], 1f to k.knop[2]))
            .glans(rond, licht = 0.35f, donker = 0.12f, boven = 4.dp, onder = 6.dp)
            .border(6.dp, k.ring, rond)
            .clickable(onClick = onWissel)
            .semantics { contentDescription = if (isIn) "Uitklokken" else "Inklokken" },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icoon(if (isIn) Icoontjes.uitklokken else Icoontjes.inklokken, 38.dp)
            Tekst(if (isIn) "UIT" else "IN", 24.sp, kleur = Color.White, regel = 28.sp)
        }
    }
}

/** Nog ingeklokt van een eerdere dag: vraag de eindtijd. */
@Composable
private fun VergetenKaart(begin: LocalDateTime, onEind: (LocalDateTime) -> Unit) {
    var kiezen by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Color(0xFFFFF1C6), RoundedCornerShape(26.dp), Color(0xFFC48C3C))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icoon(Icoontjes.belLijn, 28.dp)
            Tekst("Vergeten uit te klokken?", 19.sp, kleur = Color(0xFF4E3B08))
        }
        Tekst(
            "Je bent nog ingeklokt sinds ${datumTekst(begin)} ${alsKlok(begin.toLocalTime())}. Hoe laat ben je gestopt?",
            16.sp, dikte = FontWeight.Bold, kleur = Color(0xFF6E5410),
        )
        ZachteKnop("Eindtijd kiezen", onClick = { kiezen = true }, Modifier.fillMaxWidth())
    }
    if (kiezen) {
        TijdKiezer(
            titel = "Gestopt om",
            begin = LocalTime.of(15, 45),
            onKies = { tijd -> kiezen = false; onEind(begin.toLocalDate().atTime(tijd)) },
            onSluit = { kiezen = false },
        )
    }
}

@Composable
private fun StatTegel(
    p: Pastel,
    icoon: List<Vorm>,
    label: String,
    groot: String,
    klein: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val vorm = RoundedCornerShape(26.dp)
    Column(
        modifier
            .fillMaxHeight()
            .klei(p.kaart, vorm, p.donker, hoogte = 10.dp)
            .clip(vorm)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IcoonBlok(p.licht, p.donker, icoon, Modifier.padding(bottom = 4.dp))
        Tekst(label, 15.sp, dikte = FontWeight.ExtraBold, kleur = p.label)
        Tekst(groot, 32.sp, kleur = p.groot, regel = 34.sp)
        Tekst(klein, 14.sp, dikte = FontWeight.ExtraBold, kleur = p.klein)
    }
}

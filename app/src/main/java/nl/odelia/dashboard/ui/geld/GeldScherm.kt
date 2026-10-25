package nl.odelia.dashboard.ui.geld

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.BoekSoort
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.GeldBoek
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.logica.Potje
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.cirkel
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

enum class GeldTab(val emoji: String, val label: String) { Potjes("🫙", "Potjes"), Uitgaven("⭕", "Uitgaven") }

/** Hoe het scherm opent (ontwerp: Geld, GeldUitgaven, GeldGekocht, GeldKalender, GeldVerdelen). */
data class GeldBegin(
    val tab: GeldTab = GeldTab.Potjes,
    val invoer: Boolean = false,
    val kalender: Boolean = false,
    val verdelen: Boolean = false,
)

private val munt = listOf(
    Vorm(cirkel(12f, 12f, 9f), lijn = Color.White, dikte = 2.4f),
    Vorm("M15 9.5c-.5-1-1.6-1.5-3-1.5-1.7 0-3 .9-3 2s1.3 1.6 3 2 3 1 3 2-1.3 2-3 2c-1.4 0-2.5-.5-3-1.5M12 6v12", lijn = Color.White, dikte = 2.4f),
)

/**
 * Geld (ontwerp/Geld.dc.html): potjes, uitgaven, "ik heb iets gekocht" en inkomen verdelen.
 * Potjes zijn enveloppen in de app. De app maakt nooit echt geld over; bij een echte rekening
 * (ING-spaarrekening, Giro-rekening) komt alleen een geheugensteuntje. Gaat nooit online.
 */
@Composable
fun GeldScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
    begin: GeldBegin = GeldBegin(),
) {
    val data = staat.geld
    val vandaag = staat.nu.toLocalDate()
    var tab by remember { mutableStateOf(begin.tab) }
    var invoerOpen by remember { mutableStateOf(begin.invoer) }
    var verdelenOpen by remember { mutableStateOf(begin.verdelen) }
    var overzichtOpen by remember { mutableStateOf(false) }
    var invoerSoort by remember { mutableStateOf(BoekSoort.Uit) }
    var bewerk by remember { mutableStateOf<Potje?>(null) }
    var nieuwPotje by remember { mutableStateOf(false) }

    fun lees(tekst: String) {
        if (!acties.voorlezen(tekst)) onMelding("Voorlezen lukt nog niet. Staat er een Nederlandse stem op je telefoon?")
    }

    val scroll = rememberScrollState()
    Box(modifier.fillMaxSize().verticalScroll(scroll), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            TerugBalk("Geld", onTerug) {
                Box(
                    Modifier
                        .size(52.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Color(0xFF82B874), spotColor = Color(0xFF82B874))
                        .background(hoekVerloop(Color(0xFFB9DDB0), Color(0xFF82B874)), CircleShape)
                        .glans(CircleShape, licht = 0.5f, donker = 0.08f),
                    contentAlignment = Alignment.Center,
                ) { Icoon(munt, 28.dp) }
            }

            TotaalKaart(data.totaal, onLees = { lees("Totaal: ${GeldBoek.uitspraak(data.totaal)}.") })

            Segment(
                keuzes = GeldTab.entries,
                gekozen = tab,
                label = { "${it.emoji} ${it.label}" },
                onKies = { tab = it },
                achter = Color(0xB3FBF3EA),
                maat = 17.sp,
                hoogte = 50.dp,
            )

            when (tab) {
                GeldTab.Potjes -> {
                    PotjesRaster(data, onKies = { bewerk = it }, onNieuw = { nieuwPotje = true })
                    BalkKnop("🔀", "Verdelen", "loon in één tik over je potjes", LilaLicht, Lila, verdelenOpen, { verdelenOpen = !verdelenOpen })
                    if (verdelenOpen) VerdeelPaneel(data, vandaag, acties, onMelding, ::lees, onKlaar = { })
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KleineKnop("🔁", "Overboeken", Modifier.weight(1f)) {
                            invoerSoort = BoekSoort.Over
                            invoerOpen = true
                        }
                        KleineKnop("🕒", "Overzicht", Modifier.weight(1f), aan = overzichtOpen) { overzichtOpen = !overzichtOpen }
                    }
                    if (overzichtOpen) OverzichtPaneel(data, vandaag, acties, onMelding)
                }
                GeldTab.Uitgaven -> UitgavenPaneel(data, vandaag, acties, onMelding, ::lees)
            }

            BalkKnop(
                emoji = null,
                titel = "Ik heb iets gekocht",
                onder = "tik, kies, klaar",
                van = Kleur.KoraalLicht,
                naar = Kleur.Koraal,
                open = invoerOpen,
                onClick = {
                    if (!invoerOpen) invoerSoort = BoekSoort.Uit
                    invoerOpen = !invoerOpen
                },
                icoon = { Icoon(Icoontjes.plus, 30.dp) },
            )
            if (invoerOpen) {
                InvoerPaneel(
                    data = data,
                    vandaag = vandaag,
                    beginSoort = invoerSoort,
                    beginKalender = begin.kalender,
                    acties = acties,
                    onMelding = onMelding,
                    onKlaar = { soort ->
                        invoerOpen = false
                        if (soort == BoekSoort.Uit) tab = GeldTab.Uitgaven
                    },
                )
            }
        }
    }

    val p = bewerk
    if (p != null || nieuwPotje) {
        PotjeVenster(
            potje = p,
            data = data,
            onBewaar = { nieuw, saldo ->
                acties.wijzigGeld { d ->
                    val met = GeldBoek.zetPotje(d, nieuw)
                    if (saldo != null) GeldBoek.zetSaldo(met, nieuw.id, saldo) else met
                }
                bewerk = null
                nieuwPotje = false
                onMelding("Potje bewaard")
            },
            onWeg = { id ->
                acties.wijzigGeld { GeldBoek.haalPotjeWeg(it, id) }
                bewerk = null
                onMelding("Potje weggehaald")
            },
            onSluit = {
                bewerk = null
                nieuwPotje = false
            },
        )
    }
}

@Composable
private fun TotaalKaart(cent: Long, onLees: () -> Unit) {
    val vorm = RoundedCornerShape(30.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .klei(hoekVerloop(GroenLicht, Groen), vorm, Color(0xFF5A966E), hoogte = 14.dp)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Tekst("💰", 44.sp)
        Column(Modifier.weight(1f)) {
            Tekst("Totaal", 16.sp, dikte = FontWeight.ExtraBold, kleur = Color.White.copy(alpha = 0.95f))
            Tekst(Geld.bedrag(cent), 32.sp, kleur = Color.White, regel = 36.sp)
        }
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
                .clickable(onClick = onLees)
                .semantics { contentDescription = "Voorlezen" },
            contentAlignment = Alignment.Center,
        ) { Icoon(Icoontjes.afspelen, 20.dp) }
    }
}

@Composable
private fun PotjesRaster(data: GeldData, onKies: (Potje) -> Unit, onNieuw: () -> Unit) {
    val kaarten: List<Potje?> = data.potjes + null
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        kaarten.chunked(2).forEach { rij ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rij.forEach { p ->
                    val i = if (p == null) -1 else data.potjes.indexOf(p)
                    PotjeKaart(p, if (p == null) Color(0x99FBF3EA) else potKleuren[i % potKleuren.size], data, Modifier.weight(1f)) {
                        if (p == null) onNieuw() else onKies(p)
                    }
                }
                if (rij.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Ondertitel van een potje: "€ 180 per maand", "30% van de rest", of de echte rekening. */
internal fun potjeOnder(p: Potje): String = listOfNotNull(
    p.vastCent?.let { "${Geld.bedrag(it).removeSuffix(",00")} per maand" },
    p.procent?.takeIf { it > 0 && p.vastCent == null }?.let { "$it% van de rest" },
    p.rekening?.let { "🏦 $it" },
).joinToString(" · ")

@Composable
private fun PotjeKaart(p: Potje?, achter: Color, data: GeldData, modifier: Modifier, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(24.dp)
    Column(
        modifier
            .heightIn(min = 118.dp)
            .klei(achter, vorm, Kleur.KaartSchaduw, hoogte = 8.dp)
            .clip(vorm)
            .clickable(onClickLabel = if (p == null) "Nieuw potje" else "${p.naam} aanpassen", onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (p == null) {
            Tekst("➕", 28.sp)
            Tekst("Nieuw potje", 15.sp, kleur = Kleur.InktZacht, dikte = FontWeight.ExtraBold)
            return@Column
        }
        Tekst(p.emoji.ifEmpty { "🫙" }, 28.sp)
        Tekst(p.naam, 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht, regel = 17.sp)
        val onder = potjeOnder(p)
        if (onder.isNotEmpty()) Tekst(onder, 13.sp, dikte = FontWeight.Bold, kleur = Grijs)
        Spacer(Modifier.weight(1f))
        val saldo = data.saldo(p.id)
        Tekst(Geld.bedrag(saldo), 21.sp, kleur = if (saldo < 0) Color(0xFFC2453A) else Bruin)
    }
}

@Composable
private fun KleineKnop(emoji: String, tekst: String, modifier: Modifier, aan: Boolean = false, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(22.dp)
    Row(
        modifier
            .heightIn(min = 60.dp)
            .klei(if (aan) Color(0xFFF5E8DA) else Kleur.Kaart, vorm, Kleur.KaartSchaduw, hoogte = 8.dp)
            .clip(vorm)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Tekst(emoji, 18.sp)
        Tekst(tekst, 16.sp, kleur = Bruin)
    }
}

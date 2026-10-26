package nl.odelia.dashboard.ui.geld

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.UUID
import nl.odelia.dashboard.logica.BoekSoort
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.GeldBoek
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.logica.GeldStart
import nl.odelia.dashboard.logica.Potje
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.Venster
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Nunito

/**
 * Inkomen verdelen (ontwerp: GeldVerdelen). Eerst de vaste maandbedragen, dan de rest in procenten.
 * Nieuw geld (loon) of geld dat al in een los potje staat (bv. Inkomsten).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VerdeelPaneel(
    data: GeldData,
    vandaag: LocalDate,
    acties: AppActies,
    onMelding: (String) -> Unit,
    onLees: (String) -> Unit,
    onKlaar: () -> Unit,
) {
    var tekst by remember { mutableStateOf("") }
    var van by remember { mutableStateOf<String?>(null) } // null = nieuw geld
    var klaar by remember { mutableStateOf<List<String>?>(null) }
    val cent = Geld.leesBedrag(tekst) ?: 0L
    val v = Geld.verdeel(cent, data.potjes)
    val vast = data.potjes.filter { it.vastCent != null }
    val procent = data.potjes.filter { it.vastCent == null && it.procent != null && (it.procent ?: 0) >= 0 && GeldBoek.lossePotjes(data).none { l -> l.id == it.id } }
    val losse = GeldBoek.lossePotjes(data)

    fun zet(p: Potje, n: Int) {
        acties.wijzigGeld { GeldBoek.zetProcent(it, p.id, n) }
        klaar = null
    }

    Paneel {
        Tekst("💶 Hoeveel wil je verdelen?", 17.sp, kleur = Bruin)
        BedragVeld(tekst, { tekst = it; klaar = null }, "Bedrag om te verdelen", maat = 38.sp, streep = LilaLicht)

        Tekst("Waar komt het vandaan?", 15.sp, dikte = FontWeight.ExtraBold, kleur = BruinZacht)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("💶 Nieuw geld (loon)", van == null, { van = null; klaar = null }, rand = Lila)
            losse.forEach { p ->
                Chip("${p.emoji} ${p.naam} · ${Geld.bedrag(data.saldo(p.id))}", van == p.id, {
                    van = p.id
                    klaar = null
                    if (tekst.isBlank()) data.saldo(p.id).takeIf { it > 0 }?.let { tekst = invulTekst(it) }
                }, rand = Lila)
            }
        }

        if (vast.isNotEmpty()) {
            Tekst("1. Eerst de vaste potjes", 15.sp, dikte = FontWeight.ExtraBold, kleur = BruinZacht)
            vast.forEach { p ->
                WitteRegel {
                    Tekst(p.emoji, 22.sp)
                    Tekst(p.naam, 16.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold)
                    Tekst(Geld.bedrag(v.perPotje[p.id] ?: 0L), 17.sp, kleur = Bruin)
                }
            }
        }

        Tekst("${if (vast.isEmpty()) "1" else "2"}. De rest (${Geld.bedrag(v.rest)}) zo:", 15.sp, dikte = FontWeight.ExtraBold, kleur = BruinZacht)
        procent.forEachIndexed { i, p ->
            ProcentRegel(p, v.perPotje[p.id] ?: 0L, verdeelKleuren[i % verdeelKleuren.size], onZet = { zet(p, it) })
        }

        val samen = v.procentSamen
        val goed = samen == 100
        Row(
            Modifier
                .fillMaxWidth()
                .background(if (goed) Color(0xFFE3EDDF) else Color(0xFFFBD9D6), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            val kleur = if (goed) Color(0xFF2F5E43) else Color(0xFFC2453A)
            Tekst("samen", 16.sp, Modifier.weight(1f), kleur = kleur)
            Tekst("$samen% " + when {
                goed -> "✓"
                samen > 100 -> "· te veel!"
                else -> "· nog ${100 - samen}% over"
            }, 16.sp, kleur = kleur)
        }
        if (!goed && samen < 100 && cent > 0) {
            val waar = if (van != null) data.potje(van)?.naam else losse.firstOrNull()?.naam
            Tekst(
                if (waar != null) "Wat over blijft (${Geld.bedrag(v.over)}) blijft in $waar." else "Wat over blijft (${Geld.bedrag(v.over)}) wordt niet verdeeld.",
                14.sp, dikte = FontWeight.Bold, kleur = Grijs,
            )
        }

        val gedaan = klaar
        GroeneKnop(
            if (gedaan != null) "✓ Verdeeld!" else "🔀 Nu verdelen",
            onClick = {
                when {
                    gedaan != null -> Unit
                    cent <= 0L -> onMelding("Typ eerst een bedrag")
                    van != null && cent > data.saldo(van!!) -> onMelding("Zoveel zit er niet in ${data.potje(van)?.naam}")
                    else -> {
                        val bron = van
                        val boekingen = GeldBoek.verdeelBoekingen(v, vandaag, van = bron, restNaar = if (bron == null) losse.firstOrNull()?.id else null)
                        acties.wijzigGeld { d -> d.copy(boekingen = d.boekingen + boekingen) }
                        klaar = Geld.overmaken(v, data.potjes)
                        onMelding("Verdeeld over je potjes")
                        onKlaar()
                    }
                }
            },
            van = if (gedaan != null) GroenLicht else LilaLicht,
            naar = if (gedaan != null) Groen else Lila,
        )
        if (!gedaan.isNullOrEmpty()) {
            val zin = "Vergeet niet: maak in de ING-app over: " + gedaan.joinToString(", ") + "."
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF1B8), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Tekst("🏦 Nu zelf overmaken in de ING-app", 16.sp, kleur = Bruin)
                gedaan.forEach { Tekst("• $it", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht) }
                Tekst("De app maakt zelf nooit geld over.", 13.sp, dikte = FontWeight.Bold, kleur = Grijs)
                ZachteKnop("🔊 Voorlezen", { onLees(zin) })
            }
        }
    }
}

@Composable
private fun ProcentRegel(p: Potje, cent: Long, kleur: Color, onZet: (Int) -> Unit) {
    val pc = p.procent ?: 0
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tekst(p.emoji, 22.sp)
            Tekst(p.naam, 16.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold)
            Tekst(Geld.bedrag(cent), 17.sp, kleur = Bruin)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Tekst("percentage", 13.sp, Modifier.weight(1f), dikte = FontWeight.ExtraBold, kleur = Grijs)
            PlusMin("−", "${p.naam} minder") { onZet(maxOf(pc - 5, 0)) }
            Row(
                Modifier
                    .height(48.dp)
                    .border(2.dp, LilaLicht, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = pc.toString(),
                    onValueChange = { t -> onZet((t.filter(Char::isDigit).take(3).toIntOrNull() ?: 0).coerceIn(0, 100)) },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Bruin, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(Kleur.Koraal),
                    modifier = Modifier.width(38.dp).semantics { contentDescription = "${p.naam} percentage" },
                )
                Tekst("%", 16.sp, kleur = Bruin)
            }
            PlusMin("+", "${p.naam} meer") { onZet(minOf(pc + 5, 100)) }
        }
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color(0xFFF1E4D6))) {
            Box(Modifier.fillMaxWidth(minOf(pc * 2, 100) / 100f).fillMaxHeight().clip(CircleShape).background(kleur))
        }
    }
}

@Composable
private fun PlusMin(teken: String, uitleg: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Kleur.Kaart)
            .clickable(onClickLabel = uitleg, onClick = onClick)
            .semantics { contentDescription = uitleg },
        contentAlignment = Alignment.Center,
    ) { Tekst(teken, 22.sp, kleur = Color(0xFF7A4A38)) }
}

/** Overzicht: alles wat er gebeurd is, met weghalen en delen als tabel (CSV). */
@Composable
internal fun OverzichtPaneel(data: GeldData, vandaag: LocalDate, acties: AppActies, onMelding: (String) -> Unit) {
    var hoeveel by remember { mutableIntStateOf(30) }
    var weg by remember { mutableStateOf<String?>(null) }
    Paneel(ruimte = 8.dp) {
        Tekst("🕒 Alles wat er gebeurde", 19.sp, kleur = Bruin)
        if (data.boekingen.isEmpty()) {
            Tekst("Nog niks. Tik op \"Ik heb iets gekocht\".", 16.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }
        var getoond = 0
        for (dag in GeldBoek.allesPerDag(data.boekingen)) {
            if (getoond >= hoeveel) break
            Tekst(GeldBoek.dagTekst(dag.datum, vandaag), 15.sp, dikte = FontWeight.ExtraBold, kleur = BruinZacht)
            for (b in dag.boekingen) {
                if (getoond >= hoeveel) break
                getoond++
                BoekingRegel(b, data, onClick = { weg = b.id })
            }
        }
        if (data.boekingen.size > hoeveel) ZachteKnop("Meer laten zien", { hoeveel += 30 }, Modifier.fillMaxWidth())
        ZachteKnop("📤 Delen als tabel (Sheets/Drive)", {
            if (!acties.deelTekst("Geld", Geld.csv(data.boekingen, data.potjes))) onMelding("Delen lukte niet")
        }, Modifier.fillMaxWidth())
    }
    weg?.let { id -> WegVraag(id, data, acties, onMelding) { weg = null } }
}

/** Eén regel: "Ah · Lekkers · uit Vrije geld   € 10,56". */
@Composable
internal fun BoekingRegel(b: nl.odelia.dashboard.logica.Boeking, data: GeldData, onClick: () -> Unit) {
    val c = categorie(b.categorie)
    WitteRegel(onClick = onClick, uitleg = "Aanpassen of weghalen") {
        EmojiBol(c.emoji, Color(c.kleur).copy(alpha = 0.35f))
        Column(Modifier.weight(1f)) {
            Tekst(b.notitie?.takeIf { it.isNotBlank() } ?: c.label, 16.sp)
            val potje = data.potje(b.potjeId)?.naam ?: "?"
            val onder = when (b.soort) {
                BoekSoort.Uit -> "${c.label} · uit $potje"
                BoekSoort.In -> "${c.label} · in $potje"
                BoekSoort.Over -> "$potje → ${data.potje(b.naarPotjeId)?.naam ?: "?"}"
            } + if (b.tags.isNotEmpty()) " · " + b.tags.joinToString(", ") else ""
            Tekst(onder, 13.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }
        val teken = when (b.soort) {
            BoekSoort.Uit -> ""
            BoekSoort.In -> "+ "
            BoekSoort.Over -> "🔁 "
        }
        Tekst(teken + Geld.bedrag(b.cent), 17.sp, kleur = if (b.soort == BoekSoort.In) GroenDonker else Bruin)
    }
}

@Composable
internal fun WegVraag(id: String, data: GeldData, acties: AppActies, onMelding: (String) -> Unit, onSluit: () -> Unit) {
    val b = data.boekingen.find { it.id == id } ?: return onSluit()
    Venster("Weghalen?", onSluit, onKlaar = {
        acties.wijzigGeld { GeldBoek.verwijder(it, id) }
        onMelding("Weggehaald")
        onSluit()
    }, klaarTekst = "Weg") {
        Tekst(
            "${b.notitie ?: categorie(b.categorie).label}: ${Geld.bedrag(b.cent)}",
            18.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.InktZacht, uitlijning = TextAlign.Center,
        )
    }
}

/** Potje maken of aanpassen: naam, emoji, vast bedrag of procent, echte rekening, en wat er nu in zit. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PotjeVenster(
    potje: Potje?,
    data: GeldData,
    onBewaar: (Potje, Long?) -> Unit,
    onWeg: (String) -> Unit,
    onSluit: () -> Unit,
) {
    var naam by remember { mutableStateOf(potje?.naam.orEmpty()) }
    var emoji by remember { mutableStateOf(potje?.emoji?.ifEmpty { null } ?: "🫙") }
    var soort by remember {
        mutableStateOf(
            when {
                potje?.vastCent != null -> 0
                (potje?.procent ?: 0) > 0 || potje == null -> 1
                else -> 2
            },
        )
    }
    var vastTekst by remember { mutableStateOf(potje?.vastCent?.let(::invulTekst).orEmpty()) }
    var procentTekst by remember { mutableStateOf(potje?.procent?.toString() ?: "10") }
    var rekening by remember { mutableStateOf(potje?.rekening) }
    var saldoTekst by remember { mutableStateOf(potje?.let { invulTekst(data.saldo(it.id)) }.orEmpty()) }
    val emojis = listOf("🫙", "👛", "🐷", "📈", "🛟", "🩺", "📱", "🍬", "✈️", "🌿", "⛪", "🎁", "💜", "🏠", "🎓", "🐾")

    Venster(if (potje == null) "Nieuw potje" else "Potje aanpassen", onSluit, onKlaar = {
        val n = naam.trim()
        if (n.isEmpty()) return@Venster
        val nieuw = Potje(
            id = potje?.id ?: UUID.randomUUID().toString(),
            naam = n,
            emoji = emoji,
            startCent = potje?.startCent ?: 0L,
            vastCent = if (soort == 0) Geld.leesBedrag(vastTekst) ?: 0L else null,
            procent = if (soort == 1) (procentTekst.toIntOrNull() ?: 0).coerceIn(0, 100) else null,
            echteRekening = rekening != null,
            rekening = rekening,
        )
        val saldo = Geld.leesBedrag(saldoTekst.removePrefix("-"))?.let { if (saldoTekst.startsWith("-")) -it else it }
        onBewaar(nieuw, saldo.takeIf { potje == null || it != data.saldo(potje.id) })
    }, klaarTekst = "Bewaar") {
        Column(
            Modifier.widthIn(max = 420.dp).heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GeldKopje("Naam")
            Invoer(naam, { naam = it.take(40) }, "bv. Vakantie")
            GeldKopje("Plaatje")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                emojis.forEach { e -> Chip(e, emoji == e, { emoji = e }, vorm = RoundedCornerShape(14.dp)) }
            }
            GeldKopje("Bij verdelen")
            Segment(listOf(0, 1, 2), soort, { listOf("Vast €", "Procent", "Niet")[it] }, { soort = it }, maat = 14.sp)
            when (soort) {
                0 -> Invoer(vastTekst, { vastTekst = it }, "bedrag per maand, bv. 50", toetsen = KeyboardType.Decimal)
                1 -> Invoer(procentTekst, { procentTekst = it.filter(Char::isDigit).take(3) }, "procent van de rest, bv. 20", toetsen = KeyboardType.Number)
                else -> Tekst("Dit potje krijgt niks bij verdelen.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
            }
            GeldKopje("Echte rekening?")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("Nee, alleen in de app", rekening == null, { rekening = null })
                Chip("🏦 ${GeldStart.ING_SPAAR}", rekening == GeldStart.ING_SPAAR, { rekening = GeldStart.ING_SPAAR })
                Chip("🏦 ${GeldStart.GIRO}", rekening == GeldStart.GIRO, { rekening = GeldStart.GIRO })
            }
            GeldKopje("Er zit nu in (€)")
            Invoer(saldoTekst, { saldoTekst = it.filter { c -> c.isDigit() || c == ',' || c == '.' || c == '-' } }, "0,00", toetsen = KeyboardType.Decimal)
            if (potje != null) {
                if (GeldBoek.magWeg(data, potje.id)) {
                    ZachteKnop("🗑️ Potje weghalen", { onWeg(potje.id) }, Modifier.fillMaxWidth(), kleur = Color(0xFFC2453A))
                } else {
                    Tekst("Weghalen kan niet: er horen boekingen bij.", 13.sp, dikte = FontWeight.Bold, kleur = Grijs)
                }
            }
        }
    }
}

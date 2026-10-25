package nl.odelia.dashboard.ui.leren

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import nl.odelia.dashboard.logica.Kaart
import nl.odelia.dashboard.logica.Leren
import nl.odelia.dashboard.logica.LerenData
import nl.odelia.dashboard.logica.LerenInhoud
import nl.odelia.dashboard.logica.Meerkeuze
import nl.odelia.dashboard.logica.QuizRonde
import nl.odelia.dashboard.logica.QuizScore
import nl.odelia.dashboard.logica.Rekenvraag
import nl.odelia.dashboard.logica.Rondje
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastels

private val Bruin = Color(0xFF5A3426)
private val Grijs = Color(0xFF8A6A5A)
private val Groen = Color(0xFF3F7A58)
private val GoedAchter = Color(0xFFDDE9D7)
private val FoutAchter = Color(0xFFFBE7C3)

private fun naamVan(onderwerp: String) = LerenInhoud.onderwerpen.firstOrNull { it.id == onderwerp }?.naam ?: onderwerp
private fun vakVan(onderwerp: String) = LerenInhoud.onderwerpen.firstOrNull { it.id == onderwerp }?.vak ?: "elektro"

// ---- Kaarten ----

@Composable
internal fun KaartenDeel(
    data: LerenData,
    onderwerp: String,
    vandaag: LocalDate,
    rondje: Rondje?,
    omgedraaid: Boolean,
    onStart: (alles: Boolean) -> Unit,
    onDraai: () -> Unit,
    onAntwoord: (wist: Boolean) -> Unit,
    onStop: () -> Unit,
    onLees: (String) -> Unit,
) {
    val kaarten = Leren.kaarten(data, onderwerp)
    if (kaarten.isEmpty()) {
        LKaart {
            Tekst(naamVan(onderwerp), 21.sp, kleur = Bruin)
            Tekst("Hier zijn nog geen kaarten. Maak er zelf een bij ✏️ Maken.", 17.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt)
        }
        return
    }
    val nu = rondje?.nu?.let { id -> kaarten.firstOrNull { it.id == id } }
    if (rondje == null || nu == null) {
        if (rondje != null && rondje.klaar && rondje.totaal > 0) {
            Uitslag(
                "Klaar! ${rondje.totaal} kaarten geoefend. 🎉",
                if (rondje.nogOefenen > 0) "${rondje.nogOefenen}× nog oefenen. Die komen sneller terug." else "Alles in één keer goed!",
                goed = true,
            )
        }
        val v = Leren.voortgang(data, onderwerp, vandaag)
        LKaart {
            Tekst(naamVan(onderwerp), 21.sp, kleur = Bruin)
            Tekst(
                if (v.klaar > 0) "${v.klaar} kaarten klaar om te oefenen." else "Niets meer voor vandaag. Morgen komen er weer kaarten terug.",
                17.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt,
            )
            Groepenkast(data, kaarten, huidig = null)
            Tekst(
                "Wist je het? Dan gaat de kaart een groep omhoog en komt hij later terug. Nog oefenen? Dan terug naar G1.",
                14.sp, dikte = FontWeight.Bold, kleur = Grijs,
            )
        }
        if (v.klaar > 0) GroteKnop("Start (${minOf(v.klaar, Leren.MAX_RONDJE)} kaarten)", { onStart(false) }, icoon = Icoontjes.afspelen)
        ZachteKnop(if (v.klaar > 0) "Alle kaarten oefenen" else "Toch oefenen", { onStart(true) }, Modifier.fillMaxWidth())
        return
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Tekst("Nog ${rondje.rij.size} · goed ${rondje.goed}", 16.sp, Modifier.weight(1f), kleur = Grijs)
        ZachteKnop("Stoppen", onStop)
    }
    Groepenkast(data, kaarten, huidig = nu)
    Flashcard(nu, omgedraaid, onDraai)
    ZachteKnop("🔊 Voorlezen", { onLees(if (omgedraaid) nu.antwoord else nu.vraag) })
    if (omgedraaid) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KleurKnop("Nog oefenen", Pastels.Geel, { onAntwoord(false) }, Modifier.weight(1f))
            KleurKnop("Wist ik ✓", Pastels.Salie, { onAntwoord(true) }, Modifier.weight(1f))
        }
    } else {
        GroteKnop("Toon antwoord", onDraai, icoon = Icoontjes.pijlRechts)
    }
}

/** Vijf groepen, zoals in een groepenkast: G1 = vaak herhalen, G5 = zit erin. */
@Composable
private fun Groepenkast(data: LerenData, kaarten: List<Kaart>, huidig: Kaart?) {
    val tel = IntArray(5)
    kaarten.forEach { tel[Leren.bakje(data, it.id) - 1]++ }
    val nuIn = huidig?.let { Leren.bakje(data, it.id) }
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDE3D8), RoundedCornerShape(18.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tel.forEachIndexed { i, n ->
            val vorm = RoundedCornerShape(12.dp)
            Column(
                Modifier
                    .weight(1f)
                    .background(if (i >= 3) GoedAchter else Color.White, vorm)
                    .border(3.dp, if (nuIn == i + 1) Kleur.Koraal else Color.Transparent, vorm)
                    .padding(vertical = 6.dp)
                    .semantics { stateDescription = "groep ${i + 1}: $n kaarten" },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Tekst("$n", 20.sp, kleur = Kleur.Inkt)
                Tekst("G${i + 1}", 12.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
            }
        }
    }
}

@Composable
private fun Flashcard(kaart: Kaart, omgedraaid: Boolean, onDraai: () -> Unit) {
    val p = pastelVan(vakVan(kaart.onderwerp))
    val vorm = RoundedCornerShape(30.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .klei(if (omgedraaid) p.kaart else Color.White, vorm, Kleur.KaartSchaduw)
            .clip(vorm)
            .clickable(onClickLabel = if (omgedraaid) "Toon de vraag" else "Toon het antwoord", role = Role.Button, onClick = onDraai)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Tekst(if (omgedraaid) "ANTWOORD" else "VRAAG", 13.sp, dikte = FontWeight.ExtraBold, kleur = if (omgedraaid) p.label else Grijs)
        Tekst(if (omgedraaid) kaart.antwoord else kaart.vraag, 21.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt, regel = 29.sp)
        if (omgedraaid) Tekst(kaart.vraag, 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
        Tekst(if (omgedraaid) "tik voor de vraag" else "tik om om te draaien", 13.sp, dikte = FontWeight.Bold, kleur = Grijs)
    }
}

@Composable
private fun Uitslag(titel: String, tekst: String, goed: Boolean) {
    LKaart(if (goed) GoedAchter else FoutAchter) {
        Tekst(titel, 20.sp, kleur = if (goed) Color(0xFF24452F) else Color(0xFF4E3B08))
        Tekst(tekst, 16.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt)
    }
}

// ---- Quiz ----

@Composable
internal fun QuizDeel(
    onderwerp: String,
    vorige: QuizScore?,
    quiz: QuizRonde?,
    onStart: () -> Unit,
    onBeantwoord: (goed: Boolean) -> Unit,
    onVolgende: () -> Unit,
    onStop: () -> Unit,
    onLees: (String) -> Unit,
) {
    val aantal = minOf(Leren.vragen(onderwerp).size, Leren.MAX_QUIZ)
    if (aantal == 0) {
        LKaart {
            Tekst(naamVan(onderwerp), 21.sp, kleur = Bruin)
            Tekst("Hier zijn nog geen quizvragen. Oefen de kaarten 🃏.", 17.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt)
        }
        return
    }
    val vraag = quiz?.vraag
    if (quiz == null || vraag == null) {
        if (quiz != null) {
            val goed = quiz.goed * 10 >= quiz.vragen.size * 7
            Uitslag(
                "Score: ${quiz.goed} van ${quiz.vragen.size}",
                when {
                    quiz.goed == quiz.vragen.size -> "Alles goed! 🎉"
                    goed -> "Goed bezig."
                    else -> "Oefen de kaarten nog eens en probeer het opnieuw."
                },
                goed,
            )
        }
        LKaart {
            Tekst("Quiz: ${naamVan(onderwerp)}", 21.sp, kleur = Bruin)
            Tekst("$aantal vragen. Je ziet meteen of het goed is.", 17.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt)
            if (vorige != null) Tekst("Vorige keer: ${vorige.laatst} van ${vorige.van} · beste: ${vorige.beste}", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }
        GroteKnop(if (quiz != null) "Nog een keer" else "Start de quiz", onStart, icoon = Icoontjes.afspelen)
        return
    }

    // Per vraag: wat er gekozen of ingevuld is. Nieuwe vraag = opnieuw leeg.
    var gekozen by remember(quiz.nu, quiz.vragen) { mutableStateOf<Int?>(null) }
    var invoer by remember(quiz.nu, quiz.vragen) { mutableStateOf("") }
    var nagekeken by remember(quiz.nu, quiz.vragen) { mutableStateOf<Boolean?>(null) }
    var melding by remember(quiz.nu, quiz.vragen) { mutableStateOf<String?>(null) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Tekst("Vraag ${quiz.nu + 1} van ${quiz.vragen.size} · goed ${quiz.goed}", 16.sp, Modifier.weight(1f), kleur = Grijs)
        ZachteKnop("Stoppen", onStop)
    }
    LKaart(Color.White) {
        Tekst(vraag.vraag, 21.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt, regel = 29.sp)
        when (vraag) {
            is Meerkeuze -> {
                quiz.volgorde[quiz.nu].forEach { o ->
                    Optie(
                        tekst = vraag.opties[o],
                        stand = when {
                            nagekeken == null -> OptieStand.Open
                            o == vraag.goed -> OptieStand.Goed
                            o == gekozen -> OptieStand.Fout
                            else -> OptieStand.Uit
                        },
                        onClick = {
                            if (nagekeken == null) {
                                gekozen = o
                                nagekeken = o == vraag.goed
                                onBeantwoord(o == vraag.goed)
                            }
                        },
                    )
                }
            }
            is Rekenvraag -> {
                Tekst("Rekenvraag. Typ alleen het getal.", 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Invoer(
                        invoer,
                        { if (nagekeken == null) invoer = it },
                        hint = "bijv. 0,5",
                        modifier = Modifier.weight(1f),
                        toetsen = KeyboardType.Decimal,
                    )
                    Tekst(vraag.eenheid, 20.sp, Modifier.width(36.dp), kleur = Kleur.InktZacht)
                }
                melding?.let { Tekst(it, 15.sp, kleur = Kleur.KoraalDiep) }
                if (nagekeken == null) {
                    GroteKnop("Controleer", {
                        val getal = Leren.leesGetal(invoer)
                        if (getal == null) {
                            melding = "Typ een getal, bijvoorbeeld 3 of 0,5."
                        } else {
                            melding = null
                            val goed = Leren.klopt(vraag, getal)
                            nagekeken = goed
                            onBeantwoord(goed)
                        }
                    }, icoon = Icoontjes.vinkje)
                }
            }
        }
    }
    ZachteKnop("🔊 Voorlezen", {
        onLees(
            when (vraag) {
                is Meerkeuze -> vraag.vraag + " " + quiz.volgorde[quiz.nu].mapIndexed { i, o -> "Optie ${i + 1}: ${vraag.opties[o]}." }.joinToString(" ")
                is Rekenvraag -> vraag.vraag
            },
        )
    })

    val goed = nagekeken ?: return
    val juist = when (vraag) {
        is Meerkeuze -> vraag.opties[vraag.goed]
        is Rekenvraag -> "${Leren.alsGetal(vraag.antwoord)} ${vraag.eenheid}"
    }
    LKaart(if (goed) GoedAchter else FoutAchter) {
        Tekst(if (goed) "Goed! 🎉" else "Nog niet goed.", 20.sp, kleur = if (goed) Color(0xFF24452F) else Color(0xFF4E3B08))
        if (!goed) Tekst("Juist is: $juist", 17.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt)
        Tekst(vraag.uitleg, 16.sp, dikte = FontWeight.Bold, kleur = Kleur.Inkt, regel = 23.sp)
        ZachteKnop("🔊 Voorlezen", { onLees((if (goed) "Goed! " else "Nog niet goed. Juist is: $juist. ") + vraag.uitleg) })
    }
    GroteKnop(if (quiz.nu + 1 < quiz.vragen.size) "Volgende" else "Uitslag", onVolgende, icoon = Icoontjes.pijlRechts)
}

private enum class OptieStand { Open, Goed, Fout, Uit }

@Composable
private fun Optie(tekst: String, stand: OptieStand, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(18.dp)
    val (achter, rand) = when (stand) {
        OptieStand.Open -> Kleur.Kaart to Color(0xFFEEDFD3)
        OptieStand.Goed -> GoedAchter to Groen
        OptieStand.Fout -> Color(0xFFFAD8CF) to Kleur.KoraalDiep
        OptieStand.Uit -> Kleur.Kaart to Color.Transparent
    }
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .shadow(if (stand == OptieStand.Open) 3.dp else 0.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .clip(vorm)
            .background(achter)
            .border(2.dp, rand, vorm)
            .clickable(enabled = stand == OptieStand.Open, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Tekst(
            (if (stand == OptieStand.Goed) "✓ " else if (stand == OptieStand.Fout) "✗ " else "") + tekst,
            17.sp, dikte = FontWeight.ExtraBold, kleur = if (stand == OptieStand.Uit) Grijs else Kleur.Inkt, uitlijning = TextAlign.Start,
        )
    }
}

package nl.odelia.dashboard.ui.leren

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.delay
import nl.odelia.dashboard.logica.Leren
import nl.odelia.dashboard.logica.LerenData
import nl.odelia.dashboard.logica.LerenInhoud
import nl.odelia.dashboard.logica.PauzeReden
import nl.odelia.dashboard.logica.PauzeWacht
import nl.odelia.dashboard.logica.QuizRonde
import nl.odelia.dashboard.logica.Rondje
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.TerugBalk
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels

/** De vier tabjes van Leren. */
enum class LerenTab(val emoji: String, val label: String) {
    Overzicht("📊", "Overzicht"),
    Kaarten("🃏", "Kaarten"),
    Quiz("❓", "Quiz"),
    Maken("✏️", "Maken"),
}

/** Hoe het scherm opent. De extra's zijn voor de voorbeeld-plaatjes. */
data class LerenBegin(
    val tab: LerenTab = LerenTab.Overzicht,
    val onderwerp: String? = null,
    val rondje: Rondje? = null,
    val omgedraaid: Boolean = false,
    val quiz: QuizRonde? = null,
    val seintje: PauzeReden? = null,
    val pauzeTot: Instant? = null,
)

private val Bruin = Color(0xFF5A3426)
private val Grijs = Color(0xFF8A6A5A)

fun pastelVan(vak: String): Pastel = when (vak) {
    "elektro" -> Pastels.Geel
    "burgerschap" -> Pastels.Blauw
    "nederlands" -> Pastels.Roze
    else -> Pastels.Salie
}

/**
 * Leren (ontwerp/Leren.dc.html): flashcards met bakjes, een quiz en zelf kaarten maken.
 * Werkt zonder internet. Na 20 minuten, of als je langzamer wordt, stelt de app een pauze voor.
 */
@Composable
fun LerenScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
    begin: LerenBegin = LerenBegin(),
) {
    val data = staat.leren
    val vandaag = staat.nu.toLocalDate()
    var tab by remember { mutableStateOf(begin.tab) }
    var vak by remember { mutableStateOf(begin.onderwerp?.let { o -> LerenInhoud.onderwerpen.first { it.id == o }.vak } ?: data.vak) }
    var onderwerp by remember { mutableStateOf(begin.onderwerp ?: Leren.onderwerpen(vak).first().id) }
    var rondje by remember { mutableStateOf(begin.rondje) }
    var omgedraaid by remember { mutableStateOf(begin.omgedraaid) }
    var quiz by remember { mutableStateOf(begin.quiz) }
    var wacht by remember { mutableStateOf(PauzeWacht()) }
    var seintje by remember { mutableStateOf(begin.seintje) }
    var pauzeTot by remember { mutableStateOf(begin.pauzeTot) }
    var vraagSinds by remember { mutableStateOf(Instant.now()) }

    fun lees(tekst: String) {
        if (!acties.voorlezen(Leren.uitspraak(tekst))) onMelding("Voorlezen lukt nog niet. Staat er een Nederlandse stem op je telefoon?")
    }

    /** Na elk antwoord: kijken of een pauze slim is. */
    fun gemeten() {
        val nu = Instant.now()
        val (nieuw, reden) = wacht.naAntwoord(Duration.between(vraagSinds, nu), nu)
        wacht = nieuw
        if (reden != null) seintje = reden
        vraagSinds = nu
    }

    fun kiesVak(v: String) {
        vak = v
        onderwerp = Leren.onderwerpen(v).first().id
        rondje = null
        quiz = null
        acties.wijzigLeren { it.copy(vak = v) }
    }

    fun kiesOnderwerp(o: String) {
        onderwerp = o
        rondje = null
        quiz = null
    }

    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerugBalk("Leren", onTerug) {
                Box(
                    Modifier
                        .size(52.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Color(0xFFE5B54E), spotColor = Color(0xFFE5B54E))
                        .background(hoekVerloop(Color(0xFFF7DA8A), Color(0xFFE5B54E)), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Tekst("📚", 24.sp) }
            }

            val tot = pauzeTot
            if (tot != null) {
                PauzeKaart(tot, onVerder = {
                    pauzeTot = null
                    wacht = wacht.naPauze()
                    vraagSinds = Instant.now()
                }, onLees = ::lees)
                return@Column
            }

            Vakken(vak, ::kiesVak)
            Tabjes(tab) { tab = it }
            if (tab != LerenTab.Overzicht) Onderwerpen(vak, onderwerp, ::kiesOnderwerp)

            when (tab) {
                LerenTab.Overzicht -> Overzicht(data, vak, vandaag, onOefen = { o, t ->
                    kiesOnderwerp(o)
                    tab = t
                })
                LerenTab.Kaarten -> KaartenDeel(
                    data = data,
                    onderwerp = onderwerp,
                    vandaag = vandaag,
                    rondje = rondje,
                    omgedraaid = omgedraaid,
                    onStart = { alles ->
                        rondje = Leren.nieuwRondje(data, onderwerp, vandaag, alles)
                        omgedraaid = false
                        vraagSinds = Instant.now()
                    },
                    onDraai = { omgedraaid = !omgedraaid },
                    onAntwoord = { wist ->
                        val r = rondje ?: return@KaartenDeel
                        val id = r.nu ?: return@KaartenDeel
                        acties.wijzigLeren { Leren.geoefend(it, id, wist, vandaag) }
                        rondje = r.antwoord(wist)
                        omgedraaid = false
                        gemeten()
                    },
                    onStop = { rondje = null },
                    onLees = ::lees,
                )
                LerenTab.Quiz -> QuizDeel(
                    onderwerp = onderwerp,
                    vorige = data.quiz[onderwerp],
                    quiz = quiz,
                    onStart = {
                        quiz = Leren.nieuweQuiz(onderwerp)
                        vraagSinds = Instant.now()
                    },
                    onBeantwoord = { goed ->
                        quiz = quiz?.beantwoord(goed)
                        gemeten()
                    },
                    onVolgende = {
                        val q = quiz?.volgende() ?: return@QuizDeel
                        quiz = q
                        vraagSinds = Instant.now()
                        if (q.klaar) acties.wijzigLeren { Leren.quizKlaar(it, onderwerp, q.goed, q.vragen.size, vandaag) }
                    },
                    onStop = { quiz = null },
                    onLees = ::lees,
                )
                LerenTab.Maken -> MakenDeel(data, onderwerp, acties, onMelding)
            }
        }
    }

    seintje?.let { reden ->
        PauzeVraag(
            reden,
            onDoor = {
                seintje = null
                wacht = wacht.doorgaan(Instant.now())
            },
            onPauze = {
                seintje = null
                pauzeTot = Instant.now().plus(PauzeWacht.PAUZE)
            },
            onLees = ::lees,
        )
    }
}

// ---- Bovenin: vakken en tabjes ----

@Composable
private fun Vakken(gekozen: String, onKies: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LerenInhoud.vakken.chunked(2).forEach { rij ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rij.forEach { v ->
                    val p = pastelVan(v.id)
                    val aan = v.id == gekozen
                    val vorm = RoundedCornerShape(22.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 64.dp)
                            .shadow(if (aan) 10.dp else 4.dp, vorm, ambientColor = p.donker, spotColor = p.donker)
                            .clip(vorm)
                            .background(if (aan) p.kaart else Kleur.Kaart)
                            .border(3.dp, if (aan) p.donker else Color.Transparent, vorm)
                            .selectable(selected = aan, role = Role.RadioButton, onClick = { onKies(v.id) })
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    ) {
                        Tekst(v.emoji, 20.sp)
                        Tekst(v.naam, 14.sp, kleur = if (aan) p.groot else Kleur.InktZacht, eenRegel = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun Tabjes(gekozen: LerenTab, onKies: (LerenTab) -> Unit) {
    val vorm = RoundedCornerShape(24.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .background(Kleur.KaartPil, vorm)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LerenTab.entries.forEach { t ->
            val aan = t == gekozen
            val binnen = RoundedCornerShape(20.dp)
            Column(
                Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .clip(binnen)
                    .background(if (aan) Color.White else Color.Transparent)
                    .selectable(selected = aan, role = Role.Tab, onClick = { onKies(t) })
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Tekst(t.emoji, 20.sp)
                Tekst(t.label, 12.sp, kleur = if (aan) Bruin else Grijs, dikte = FontWeight.ExtraBold, eenRegel = true)
            }
        }
    }
}

@Composable
private fun Onderwerpen(vak: String, gekozen: String, onKies: (String) -> Unit) {
    val p = pastelVan(vak)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Leren.onderwerpen(vak).forEach { o ->
            val aan = o.id == gekozen
            val vorm = RoundedCornerShape(999.dp)
            Box(
                Modifier
                    .heightIn(min = 48.dp)
                    .clip(vorm)
                    .background(if (aan) p.donker else Color.White)
                    .selectable(selected = aan, role = Role.RadioButton, onClick = { onKies(o.id) })
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Tekst(o.naam, 15.sp, kleur = if (aan) Color.White else Kleur.InktZacht, dikte = FontWeight.ExtraBold, eenRegel = true)
            }
        }
    }
}

// ---- Overzicht ----

@Composable
private fun Overzicht(data: LerenData, vak: String, vandaag: LocalDate, onOefen: (String, LerenTab) -> Unit) {
    val ows = Leren.onderwerpen(vak)
    val per = ows.associate { it.id to Leren.voortgang(data, it.id, vandaag) }
    val klaar = per.values.sumOf { it.klaar }
    val eerste = ows.firstOrNull { (per[it.id]?.klaar ?: 0) > 0 } ?: ows.first()
    val p = pastelVan(vak)

    LKaart {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tekst("🎯 Vandaag", 21.sp, Modifier.weight(1f), kleur = Bruin)
            Tekst("${data.geoefend[vandaag] ?: 0} geoefend", 16.sp, kleur = Color(0xFF3F7A58))
        }
        Tekst(
            if (klaar > 0) "$klaar kaarten klaar om te oefenen." else "Alles voor vandaag gedaan. Knap! ⭐",
            18.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt,
        )
        GroteKnop("Overhoren", { onOefen(eerste.id, LerenTab.Kaarten) }, icoon = Icoontjes.afspelen)
        ZachteKnop("❓ Quiz doen", { onOefen(eerste.id, LerenTab.Quiz) }, Modifier.fillMaxWidth())
    }

    LKaart(Color(0xFFE3EDDF)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Tekst("⏱️", 30.sp)
            Column(Modifier.weight(1f)) {
                Tekst("20 min leren, dan 5 min pauze", 17.sp, kleur = Color(0xFF2F5E43))
                Tekst("De app zegt het als het tijd is. Of als je langzamer wordt.", 14.sp, dikte = FontWeight.Bold, kleur = Color(0xFF4F7A5E))
            }
        }
    }

    WeekKaart(data, vandaag)

    LKaart {
        Tekst("📈 Hoe ver ben je?", 19.sp, kleur = Bruin)
        ows.forEach { o ->
            val v = per.getValue(o.id)
            val q = data.quiz[o.id]
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .clickable(onClickLabel = "Oefenen") { onOefen(o.id, LerenTab.Kaarten) }
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Tekst(o.naam, 16.sp, Modifier.weight(1f), kleur = Kleur.Inkt)
                    Tekst("${v.bekend}/${v.kaarten}", 16.sp, kleur = p.klein)
                }
                Balk(v, p)
                Tekst(
                    if (q != null) "quiz ${q.laatst} van ${q.van} goed" else "quiz nog niet gedaan",
                    13.sp, dikte = FontWeight.Bold, kleur = Grijs,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Legenda(Color(0xFF6FAE84), "zit erin")
            Legenda(p.licht, "bezig")
        }
    }
}

@Composable
private fun Balk(v: Leren.Voortgang, p: Pastel) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(Kleur.KaartPil),
    ) {
        if (v.kaarten > 0) {
            if (v.bekend > 0) Box(Modifier.weight(v.bekend.toFloat()).fillMaxHeight().background(Color(0xFF6FAE84)))
            if (v.bezig > 0) Box(Modifier.weight(v.bezig.toFloat()).fillMaxHeight().background(p.licht))
            val rest = v.kaarten - v.bekend - v.bezig
            if (rest > 0) Box(Modifier.weight(rest.toFloat()).fillMaxHeight())
        }
    }
}

@Composable
private fun Legenda(kleur: Color, tekst: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(12.dp).background(kleur, RoundedCornerShape(3.dp)))
        Tekst(tekst, 13.sp, dikte = FontWeight.Bold, kleur = Grijs)
    }
}

@Composable
private fun WeekKaart(data: LerenData, vandaag: LocalDate) {
    val maandag = vandaag.with(DayOfWeek.MONDAY)
    LKaart {
        Tekst("⭐ Deze week geleerd", 17.sp, kleur = Bruin)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("ma", "di", "wo", "do", "vr", "za", "zo").forEachIndexed { i, d ->
                val dag = maandag.plusDays(i.toLong())
                val geleerd = (data.geoefend[dag] ?: 0) > 0
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .background(if (geleerd) Color(0xFFF7DA8A) else Color(0xFFF1E4D6), CircleShape)
                            .border(2.dp, if (dag == vandaag) Kleur.Koraal else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { if (geleerd) Tekst("⭐", 16.sp) }
                    Tekst(d, 12.sp, dikte = FontWeight.ExtraBold, kleur = Grijs)
                }
            }
        }
    }
}

// ---- Pauze ----

@Composable
private fun PauzeVraag(reden: PauzeReden, onDoor: () -> Unit, onPauze: () -> Unit, onLees: (String) -> Unit) {
    val uitleg = "Een korte pauze helpt je hoofd. Daarna onthoud je meer."
    Dialog(onDismissRequest = onDoor) {
        Column(
            Modifier
                .padding(16.dp)
                .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Tekst("😴", 44.sp)
            Tekst("Even pauze?", 24.sp, kleur = Bruin)
            Tekst(reden.tekst, 17.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt, uitlijning = TextAlign.Center)
            Tekst(uitleg, 15.sp, dikte = FontWeight.Bold, kleur = Grijs, uitlijning = TextAlign.Center)
            ZachteKnop("🔊 Voorlezen", { onLees("Even pauze? ${reden.tekst} $uitleg") })
            GroteKnop("Pauze, 5 minuten", onPauze, icoon = Icoontjes.vinkje)
            ZachteKnop("Nog even door", onDoor, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PauzeKaart(tot: Instant, onVerder: () -> Unit, onLees: (String) -> Unit) {
    var nu by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(tot) {
        while (nu.isBefore(tot)) {
            delay(1_000)
            nu = Instant.now()
        }
    }
    val over = Duration.between(nu, tot).coerceAtLeast(Duration.ZERO)
    val klaar = over.isZero
    val tips = "Drink wat water. Kijk even naar buiten. Rek je uit."
    LKaart(Color(0xFFE3EDDF)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Tekst(if (klaar) "🌱" else "☕", 48.sp)
            Tekst(if (klaar) "Pauze klaar!" else "Pauze", 26.sp, kleur = Color(0xFF2F5E43))
            if (!klaar) {
                val s = over.seconds
                Tekst("%d:%02d".format(s / 60, s % 60), 48.sp, kleur = Color(0xFF2F5E43))
            }
            Tekst(
                if (klaar) "Fijn dat je even rust nam. Klaar om verder te gaan?" else tips,
                17.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF4F7A5E), uitlijning = TextAlign.Center,
            )
            ZachteKnop("🔊 Voorlezen", { onLees(if (klaar) "Pauze klaar! Klaar om verder te gaan?" else "Pauze. $tips") })
        }
    }
    GroteKnop(if (klaar) "Verder leren" else "Toch al verder", onVerder, icoon = Icoontjes.afspelen)
}

// ---- Bouwsteentjes ----

/** Klei-kaart voor Leren. */
@Composable
internal fun LKaart(kleur: Color = Kleur.Kaart, inhoud: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(kleur, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = inhoud,
    )
}

/** Grote knop in een pastelkleur, met 3D-voetje. */
@Composable
internal fun KleurKnop(tekst: String, p: Pastel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val vorm = RoundedCornerShape(24.dp)
    Box(
        modifier
            .heightIn(min = 60.dp)
            .shadow(10.dp, vorm, ambientColor = p.donker, spotColor = p.donker)
            .clip(vorm)
            .background(hoekVerloop(p.licht, p.donker))
            .glans(vorm, licht = 0.45f, donker = 0f)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 18.sp, kleur = p.groot, uitlijning = TextAlign.Center)
    }
}

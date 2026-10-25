package nl.odelia.dashboard.logica

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

// Leren: flashcards met bakjes (Leitner), een quiz en een pauze-seintje.
// Werkt helemaal zonder internet. Alles hier is gewone rekenregels, met testjes in LerenTest.

/** Een schoolvak, zoals Elektrotechniek of Nederlands. */
data class Vak(val id: String, val naam: String, val emoji: String)

/** Een onderwerp binnen een vak, zoals "Wet van Ohm". */
data class Onderwerp(val id: String, val vak: String, val naam: String)

/** Een flashcard. [eigen] = zelf gemaakt (anders zit hij in de app). */
data class Kaart(
    val id: String,
    val onderwerp: String,
    val vraag: String,
    val antwoord: String,
    val eigen: Boolean = false,
    val updatedAt: Long = 0,
) {
    companion object {
        fun nieuw(onderwerp: String, vraag: String, antwoord: String, nu: Long = System.currentTimeMillis()) =
            Kaart(UUID.randomUUID().toString(), onderwerp, vraag.trim(), antwoord.trim(), eigen = true, updatedAt = nu)
    }
}

/** Een quizvraag: meerkeuze of rekenen. */
sealed interface Vraag {
    val id: String
    val onderwerp: String
    val vraag: String
    val uitleg: String
}

data class Meerkeuze(
    override val id: String,
    override val onderwerp: String,
    override val vraag: String,
    val opties: List<String>,
    /** Plek van het goede antwoord in [opties]. */
    val goed: Int,
    override val uitleg: String,
) : Vraag

data class Rekenvraag(
    override val id: String,
    override val onderwerp: String,
    override val vraag: String,
    val antwoord: Double,
    val eenheid: String,
    override val uitleg: String,
) : Vraag

/** In welk bakje een kaart zit (1 = vaak herhalen, 5 = zit erin) en vanaf wanneer hij terugkomt. */
data class KaartStand(val bakje: Int, val terug: LocalDate)

data class QuizScore(val laatst: Int, val van: Int, val beste: Int, val datum: LocalDate)

/** Alles van Leren dat op de telefoon bewaard wordt. */
data class LerenData(
    val standen: Map<String, KaartStand> = emptyMap(),
    val quiz: Map<String, QuizScore> = emptyMap(),
    val eigen: List<Kaart> = emptyList(),
    /** Hoeveel kaarten en vragen er per dag geoefend zijn (voor "deze week geleerd"). */
    val geoefend: Map<LocalDate, Int> = emptyMap(),
    /** Laatst gekozen vak. */
    val vak: String = LerenInhoud.vakken.first().id,
)

object Leren {
    /** Na hoeveel dagen een kaart terugkomt, per bakje (1 t/m 5). */
    val WACHTDAGEN = listOf(0L, 0L, 1L, 3L, 7L, 14L)
    const val MAX_RONDJE = 12
    const val MAX_QUIZ = 8

    fun kaarten(data: LerenData, onderwerp: String): List<Kaart> =
        LerenInhoud.kaarten.filter { it.onderwerp == onderwerp } + data.eigen.filter { it.onderwerp == onderwerp }

    fun vragen(onderwerp: String): List<Vraag> = LerenInhoud.vragen.filter { it.onderwerp == onderwerp }

    fun onderwerpen(vak: String): List<Onderwerp> = LerenInhoud.onderwerpen.filter { it.vak == vak }

    fun bakje(data: LerenData, kaartId: String): Int = data.standen[kaartId]?.bakje ?: 1

    /** Een kaart is klaar om te oefenen als hij nieuw is, of als zijn dag gekomen is. */
    fun isKlaar(data: LerenData, kaartId: String, vandaag: LocalDate): Boolean =
        data.standen[kaartId]?.let { !it.terug.isAfter(vandaag) } ?: true

    /** Nieuwe stand na "Wist ik" of "Nog oefenen". */
    fun naAntwoord(oud: KaartStand?, wist: Boolean, vandaag: LocalDate): KaartStand {
        val bakje = if (wist) minOf(5, (oud?.bakje ?: 1) + 1) else 1
        return KaartStand(bakje, vandaag.plusDays(WACHTDAGEN[bakje]))
    }

    /** Bewaar een antwoord op een kaart. */
    fun geoefend(data: LerenData, kaartId: String, wist: Boolean, vandaag: LocalDate): LerenData =
        data.copy(
            standen = data.standen + (kaartId to naAntwoord(data.standen[kaartId], wist, vandaag)),
            geoefend = telOp(data.geoefend, vandaag),
        )

    /** Bewaar de uitslag van een quiz. */
    fun quizKlaar(data: LerenData, onderwerp: String, goed: Int, van: Int, vandaag: LocalDate): LerenData {
        val oud = data.quiz[onderwerp]
        val beste = maxOf(goed, if (oud != null && oud.van == van) oud.beste else 0)
        return data.copy(
            quiz = data.quiz + (onderwerp to QuizScore(goed, van, beste, vandaag)),
            geoefend = telOp(data.geoefend, vandaag, van),
        )
    }

    fun metEigen(data: LerenData, kaart: Kaart): LerenData =
        data.copy(eigen = data.eigen.filterNot { it.id == kaart.id } + kaart)

    fun zonderEigen(data: LerenData, id: String): LerenData =
        data.copy(eigen = data.eigen.filterNot { it.id == id }, standen = data.standen - id)

    /** Tel [aantal] op bij vandaag. Alleen de laatste 2 weken bewaren. */
    private fun telOp(per: Map<LocalDate, Int>, vandaag: LocalDate, aantal: Int = 1): Map<LocalDate, Int> =
        (per + (vandaag to (per[vandaag] ?: 0) + aantal)).filterKeys { !it.isBefore(vandaag.minusDays(13)) }

    /** Hoe ver je bent met een onderwerp. */
    data class Voortgang(val kaarten: Int, val bekend: Int, val bezig: Int, val klaar: Int)

    fun voortgang(data: LerenData, onderwerp: String, vandaag: LocalDate): Voortgang {
        val ks = kaarten(data, onderwerp)
        val bakjes = ks.map { bakje(data, it.id) }
        return Voortgang(
            kaarten = ks.size,
            bekend = bakjes.count { it >= 4 },
            bezig = bakjes.count { it in 2..3 },
            klaar = ks.count { isKlaar(data, it.id, vandaag) },
        )
    }

    /** Kaarten voor een nieuw rondje: eerst de laagste bakjes, hoogstens [MAX_RONDJE]. */
    fun nieuwRondje(data: LerenData, onderwerp: String, vandaag: LocalDate, alles: Boolean, kans: Random = Random): Rondje {
        val ks = kaarten(data, onderwerp).filter { alles || isKlaar(data, it.id, vandaag) }
        val rij = ks.shuffled(kans).sortedBy { bakje(data, it.id) }.take(MAX_RONDJE).map { it.id }
        return Rondje(rij, totaal = rij.size)
    }

    /** Een quiz: hoogstens [MAX_QUIZ] vragen, meerkeuze-opties door elkaar. */
    fun nieuweQuiz(onderwerp: String, kans: Random = Random): QuizRonde {
        val vs = vragen(onderwerp).shuffled(kans).take(MAX_QUIZ)
        return QuizRonde(vs, vs.map { v -> if (v is Meerkeuze) v.opties.indices.shuffled(kans) else emptyList() })
    }

    /** Leest een getal zoals "0,5" of "115". Geeft null als het geen getal is. */
    fun leesGetal(invoer: String): Double? =
        invoer.trim().replace(" ", "").replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

    /** Goed als het getal binnen 1% van het antwoord zit. */
    fun klopt(v: Rekenvraag, invoer: Double): Boolean =
        abs(invoer - v.antwoord) <= maxOf(abs(v.antwoord) * 0.01, 0.01)

    /** Getal op z'n Nederlands: 0.5 → "0,5", 3.0 → "3". */
    fun alsGetal(d: Double): String =
        if (d == Math.rint(d) && abs(d) < 1e15) d.toLong().toString() else d.toString().replace('.', ',')

    /** Zo leest de voorleesstem formules goed voor: "U = I × R" → "U is I keer R". */
    fun uitspraak(tekst: String): String = tekst
        .replace("×", " keer ")
        .replace("÷", " gedeeld door ")
        .replace("²", " kwadraat")
        .replace("Ω", " ohm")
        .replace("=", " is ")
        .replace(Regex("(\\d),(\\d)"), "$1 komma $2")
        .replace(Regex("(\\d) ?mA\\b"), "$1 milliampère")
        .replace(Regex("(\\d) ?A\\b"), "$1 ampère")
        .replace(Regex("(\\d) ?V\\b"), "$1 volt")
        .replace(Regex("(\\d) ?W\\b"), "$1 watt")
        .replace(Regex(" +"), " ")
        .trim()
}

/** Een rondje kaarten. [rij] = de kaarten die nog komen; de eerste is nu aan de beurt. */
data class Rondje(
    val rij: List<String>,
    val totaal: Int,
    val goed: Int = 0,
    val nogOefenen: Int = 0,
) {
    val klaar: Boolean get() = rij.isEmpty()
    val nu: String? get() = rij.firstOrNull()

    /** "Wist ik" = kaart is klaar voor dit rondje. "Nog oefenen" = hij komt over 3 kaarten terug. */
    fun antwoord(wist: Boolean): Rondje {
        val id = rij.firstOrNull() ?: return this
        val rest = rij.drop(1)
        return if (wist) {
            copy(rij = rest, goed = goed + 1)
        } else {
            val plek = minOf(3, rest.size)
            copy(rij = rest.take(plek) + id + rest.drop(plek), nogOefenen = nogOefenen + 1)
        }
    }
}

/** Een quiz die bezig is. [volgorde] = in welke volgorde de meerkeuze-opties staan. */
data class QuizRonde(
    val vragen: List<Vraag>,
    val volgorde: List<List<Int>>,
    val nu: Int = 0,
    val goed: Int = 0,
) {
    val klaar: Boolean get() = nu >= vragen.size
    val vraag: Vraag? get() = vragen.getOrNull(nu)

    fun beantwoord(isGoed: Boolean): QuizRonde = copy(goed = goed + if (isGoed) 1 else 0)
    fun volgende(): QuizRonde = copy(nu = nu + 1)
}

/** Waarom de app een pauze voorstelt. */
enum class PauzeReden(val tekst: String) {
    Tijd("Je bent al 20 minuten bezig. Knap!"),
    Langzamer("Je antwoorden gaan wat langzamer. Misschien word je moe."),
}

/**
 * Let op tekenen dat je moe wordt. De app kan niet echt zien hoe je je voelt;
 * hij kijkt alleen naar hoe lang je bezig bent en hoe snel je antwoordt.
 */
data class PauzeWacht(
    /** Wanneer je begon (of na de laatste pauze). Null = nog niet begonnen. */
    val begin: Instant? = null,
    /** Hoe lang elk antwoord duurde, in milliseconden. */
    val tijden: List<Long> = emptyList(),
    /** Na "Nog even door": tot dan geen seintje. */
    val stilTot: Instant? = null,
) {
    /** Je hebt iets beantwoord. Geeft de nieuwe stand en, als het tijd is, een reden voor een pauze. */
    fun naAntwoord(duur: Duration, nu: Instant): Pair<PauzeWacht, PauzeReden?> {
        val ms = duur.toMillis()
        // Telefoon even weggelegd? Dat telt niet als langzaam.
        val nieuw = copy(begin = begin ?: nu.minus(duur), tijden = if (ms in 1..MAX_TELT) tijden + ms else tijden)
        if (nieuw.stilTot != null && nu.isBefore(nieuw.stilTot)) return nieuw to null
        val reden = when {
            Duration.between(nieuw.begin, nu) >= LEERTIJD -> PauzeReden.Tijd
            nieuw.wordtLangzamer() -> PauzeReden.Langzamer
            else -> null
        }
        return nieuw to reden
    }

    /** "Nog even door": 10 minuten geen seintje, en opnieuw beginnen met kijken hoe snel je bent. */
    fun doorgaan(nu: Instant): PauzeWacht = copy(tijden = emptyList(), stilTot = nu.plus(STIL))

    /** Na een pauze begint alles opnieuw. */
    fun naPauze(): PauzeWacht = PauzeWacht()

    private fun wordtLangzamer(): Boolean {
        if (tijden.size < BEGIN + LAATST) return false
        val gewoon = tijden.take(BEGIN).sorted()[BEGIN / 2]
        val laatst = tijden.takeLast(LAATST).average()
        return laatst >= gewoon * 2 && laatst >= MIN_LANGZAAM
    }

    companion object {
        val LEERTIJD: Duration = Duration.ofMinutes(20)
        val PAUZE: Duration = Duration.ofMinutes(5)
        val STIL: Duration = Duration.ofMinutes(10)
        private const val MAX_TELT = 3 * 60_000L
        private const val MIN_LANGZAAM = 10_000.0
        private const val BEGIN = 5
        private const val LAATST = 3
    }
}

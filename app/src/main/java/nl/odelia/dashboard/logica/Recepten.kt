package nl.odelia.dashboard.logica

import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

/** De soorten uit ontwerp/Recepten.dc.html. */
enum class ReceptSoort(val emoji: String, val label: String) {
    Avond("🍝", "Avondeten"),
    Lunch("🥪", "Lunch"),
    Bakken("🍰", "Bakken"),
    Idee("💡", "Ideeën"),
}

/** Eén stap van een recept, met een plaatje (emoji) erbij. */
data class Stap(val emoji: String, val tekst: String)

data class Recept(
    val id: String,
    val soort: ReceptSoort,
    val emoji: String,
    val naam: String,
    val tijd: String = "",
    val personen: String = "",
    val ingredienten: List<String> = emptyList(),
    val stappen: List<Stap> = emptyList(),
    /** Link naar een recept op internet (opent in Chrome). */
    val link: String = "",
    val updatedAt: Long = 0,
) {
    val isIdee: Boolean get() = soort == ReceptSoort.Idee
}

data class ReceptenData(val recepten: List<Recept> = emptyList())

object Recepten {
    /** Recepten van één soort (of alles), gefilterd op een zoekwoord in naam en ingrediënten. */
    fun zoek(data: ReceptenData, soort: ReceptSoort?, zoek: String): List<Recept> {
        val woorden = zoek.lowercase().split(' ', ',').map { it.trim() }.filter { it.isNotEmpty() }
        return data.recepten.filter { r ->
            (soort == null || r.soort == soort) && woorden.all { w ->
                w in r.naam.lowercase() || r.ingredienten.any { w in it.lowercase() } || w in r.tijd.lowercase() ||
                    (w == "snel" && minuten(r.tijd)?.let { it <= 15 } == true)
            }
        }
    }

    /** "15 min" → 15, "1 uur" → 60. Onbekend → null. */
    fun minuten(tijd: String): Int? {
        val t = tijd.lowercase()
        val getal = Regex("\\d+").find(t)?.value?.toIntOrNull() ?: return null
        return if ("uur" in t) getal * 60 else if ("min" in t) getal else null
    }

    /** Wat je nog niet hebt, in de volgorde van het recept. */
    fun mis(recept: Recept, heb: Set<String>): List<String> = recept.ingredienten.filterNot { it in heb }

    /** Tekst voor de boodschappenlijst (komt in Onthoud). */
    fun boodschappen(recept: Recept, mis: List<String>): String =
        "Boodschappen voor ${recept.naam}: ${mis.joinToString(", ")}"

    /** Wat de voorleesknop zegt. */
    fun voorlezen(recept: Recept, stap: Int): String = buildString {
        append(recept.naam).append(". ")
        if (recept.ingredienten.isNotEmpty()) append("Je hebt nodig: ").append(recept.ingredienten.joinToString(", ")).append(". ")
        recept.stappen.getOrNull(stap)?.let { append("Stap ${stap + 1}: ").append(it.tekst) }
    }

    /** Eén regel per ingrediënt; lege regels en streepjes ervoor gaan weg. */
    fun regels(tekst: String): List<String> =
        tekst.lines().map { it.trim().trimStart('-', '*', '•').trim() }.filter { it.isNotEmpty() }

    /** Eén stap per regel. Begint de regel met een emoji, dan hoort die bij de stap. */
    fun stappen(tekst: String, soort: ReceptSoort): List<Stap> = regels(tekst).map { r ->
        val eerste = r.codePointAt(0)
        if (!Character.isLetterOrDigit(eerste) && r.length > 2 && ' ' in r) {
            val spatie = r.indexOf(' ')
            Stap(r.substring(0, spatie), r.substring(spatie + 1).trim())
        } else {
            Stap(if (soort == ReceptSoort.Idee) "💡" else "👩‍🍳", r)
        }
    }

    fun nieuw(
        soort: ReceptSoort,
        naam: String,
        tijd: String,
        ingredienten: String,
        stappen: String,
        link: String,
        id: String = UUID.randomUUID().toString(),
        nu: Long = System.currentTimeMillis(),
    ): Recept {
        val schoon = link.trim().takeIf { it.startsWith("https://") }.orEmpty()
        val st = stappen(stappen, soort).ifEmpty {
            listOf(Stap("💡", if (schoon.isNotEmpty()) "Open het recept met de knop hieronder." else "Idee: een recept zoeken en bewaren."))
        }
        return Recept(
            id = id,
            soort = soort,
            emoji = soort.emoji,
            naam = naam.trim(),
            tijd = tijd.trim().ifEmpty { if (soort == ReceptSoort.Idee) "nog proberen" else "" },
            personen = if (soort == ReceptSoort.Idee) "idee" else "",
            ingredienten = regels(ingredienten),
            stappen = st,
            link = schoon,
            updatedAt = nu,
        )
    }

    /** Zoekt de eerste https-link in geplakte tekst. */
    fun linkUit(tekst: String): String? = Regex("https://\\S+").find(tekst)?.value?.trimEnd('.', ',', ')')

    fun bewaar(data: ReceptenData, recept: Recept): ReceptenData {
        val i = data.recepten.indexOfFirst { it.id == recept.id }
        return if (i < 0) data.copy(recepten = data.recepten + recept)
        else data.copy(recepten = data.recepten.toMutableList().also { it[i] = recept })
    }

    fun wis(data: ReceptenData, id: String): ReceptenData = data.copy(recepten = data.recepten.filterNot { it.id == id })
}

/** De recepten uit het ontwerp: zo begint de app de eerste keer. Daarna mag Odelia alles aanpassen. */
object ReceptenStart {
    val recepten: List<Recept> = listOf(
        Recept(
            "start-pesto", ReceptSoort.Avond, "🍝", "Pasta pesto", "15 min", "2 pers.",
            listOf("pasta", "pesto", "cherrytomaatjes", "kaas"),
            listOf(
                Stap("💧", "Kook water in een grote pan."),
                Stap("🍝", "Doe de pasta erin. 10 minuten koken."),
                Stap("🍅", "Snij de tomaatjes doormidden."),
                Stap("🥄", "Giet de pasta af en roer de pesto erdoor."),
                Stap("🧀", "Tomaatjes en kaas erop. Eet smakelijk!"),
            ),
        ),
        Recept(
            "start-pannenkoek", ReceptSoort.Avond, "🥞", "Pannenkoeken", "30 min", "2 pers.",
            listOf("bloem", "melk", "eieren", "boter"),
            listOf(
                Stap("🥣", "Doe bloem, melk en eieren in een kom."),
                Stap("🌀", "Klop tot er geen klontjes meer zijn."),
                Stap("🍳", "Smelt boter in de pan, schep beslag erin."),
                Stap("🔄", "Omdraaien als de bovenkant droog is."),
            ),
        ),
        Recept(
            "start-tosti", ReceptSoort.Lunch, "🥪", "Tosti", "5 min", "1 pers.",
            listOf("brood", "kaas", "ham"),
            listOf(Stap("🍞", "Leg kaas en ham tussen 2 boterhammen."), Stap("🔥", "In het tosti-ijzer, 4 minuten.")),
        ),
        Recept(
            "start-oats", ReceptSoort.Lunch, "🥣", "Overnight oats", "5 min", "1 pers.",
            listOf("havermout", "yoghurt", "fruit"),
            listOf(
                Stap("🥣", "Havermout en yoghurt in een potje."),
                Stap("🍓", "Fruit erop, deksel dicht."),
                Stap("🌙", "Een nacht in de koelkast. Klaar!"),
            ),
        ),
        Recept(
            "start-banaan", ReceptSoort.Bakken, "🍌", "Bananenbrood", "1 uur", "1 cake",
            listOf("3 bananen", "bloem", "eieren", "suiker", "bakpoeder"),
            listOf(
                Stap("🍌", "Prak de bananen fijn."),
                Stap("🥣", "Meng alles in een kom."),
                Stap("🍞", "In een cakevorm doen."),
                Stap("⏲️", "50 minuten in de oven op 180 graden."),
            ),
        ),
        Recept(
            "start-sushi", ReceptSoort.Idee, "🍣", "Zelf sushi maken", "nog proberen", "idee",
            listOf("sushirijst", "nori", "komkommer"),
            listOf(Stap("💡", "Idee: een recept zoeken en bewaren.")),
        ),
    )
}

/** Recepten naar tekst (JSON) en terug. Kapotte stukjes worden overgeslagen. */
object ReceptenJson {
    private const val VERSIE = 1

    fun schrijf(d: ReceptenData): String = JSONObject().apply {
        put("versie", VERSIE)
        put("recepten", JSONArray().apply {
            d.recepten.forEach { r ->
                put(
                    JSONObject()
                        .put("id", r.id)
                        .put("soort", r.soort.name)
                        .put("emoji", r.emoji)
                        .put("naam", r.naam)
                        .put("tijd", r.tijd)
                        .put("personen", r.personen)
                        .put("ingredienten", JSONArray(r.ingredienten))
                        .put("stappen", JSONArray().apply { r.stappen.forEach { s -> put(JSONObject().put("emoji", s.emoji).put("tekst", s.tekst)) } })
                        .put("link", r.link)
                        .put("updatedAt", r.updatedAt),
                )
            }
        })
    }.toString()

    fun lees(tekst: String): ReceptenData {
        val o = try {
            JSONObject(tekst)
        } catch (_: Exception) {
            return ReceptenData(ReceptenStart.recepten)
        }
        val a = o.optJSONArray("recepten") ?: return ReceptenData(ReceptenStart.recepten)
        val lijst = (0 until a.length()).mapNotNull { n ->
            try {
                val x = a.getJSONObject(n)
                val soort = ReceptSoort.entries.firstOrNull { it.name == x.optString("soort") } ?: ReceptSoort.Idee
                val ingr = x.optJSONArray("ingredienten")
                val st = x.optJSONArray("stappen")
                Recept(
                    id = x.getString("id"),
                    soort = soort,
                    emoji = x.optString("emoji").ifBlank { soort.emoji },
                    naam = x.getString("naam"),
                    tijd = x.optString("tijd"),
                    personen = x.optString("personen"),
                    ingredienten = if (ingr == null) emptyList() else (0 until ingr.length()).map { ingr.getString(it) },
                    stappen = if (st == null) emptyList() else (0 until st.length()).map {
                        val s = st.getJSONObject(it)
                        Stap(s.optString("emoji"), s.getString("tekst"))
                    },
                    link = x.optString("link").takeIf { it.startsWith("https://") }.orEmpty(),
                    updatedAt = x.optLong("updatedAt"),
                )
            } catch (_: Exception) {
                null
            }
        }
        return ReceptenData(lijst)
    }
}

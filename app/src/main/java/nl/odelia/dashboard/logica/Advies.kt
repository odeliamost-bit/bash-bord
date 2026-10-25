package nl.odelia.dashboard.logica

import org.json.JSONArray
import org.json.JSONObject

/**
 * Advies (ontwerp/Advies.dc.html): stap-voor-stap uitleg voor maken en repareren.
 * Vaste inhoud, geen AI. Een eigen vraag zoekt in deze onderwerpen, of opent Google in Chrome.
 */
object Advies {
    data class Nodig(val emoji: String, val label: String)
    data class Stap(val emoji: String, val tekst: String)
    data class Uitleg(
        val id: String,
        val soort: String,
        val vraag: String,
        val intro: String,
        val nodig: List<Nodig>,
        val stappen: List<Stap>,
        /** Extra zoekwoorden, zodat "band plakken" ook de fietsband vindt. */
        val woorden: List<String> = emptyList(),
    )

    data class Soort(val id: String, val emoji: String, val label: String)

    val soorten = listOf(
        Soort("repareren", "🔧", "Repareren"),
        Soort("maken", "🧶", "Maken"),
        Soort("koken", "🍳", "Koken"),
        Soort("planten", "🪴", "Planten"),
    )

    val uitleg = listOf(
        Uitleg(
            "fietsband", "repareren", "Mijn fietsband is lek 🚲", "Geen zorgen! We doen het samen, stap voor stap. 💪",
            listOf(Nodig("🔧", "bandenlichter"), Nodig("🩹", "plakset"), Nodig("🪣", "bak water"), Nodig("💨", "pomp")),
            listOf(
                Stap("🔧", "Haal de band met de bandenlichter van de velg."),
                Stap("🎈", "Trek de binnenband eruit en pomp hem een beetje op."),
                Stap("🪣", "Houd hem in water. Waar bubbels komen, zit het gat."),
                Stap("🩹", "Maak het droog, smeer lijm en plak de pleister erop."),
                Stap("💨", "Doe de band terug en pomp hem hard op. Klaar!"),
            ),
            listOf("fiets", "band", "lek", "plakken", "binnenband"),
        ),
        Uitleg(
            "haakrondje", "maken", "Hoe haak ik een rondje? 🧶", "Leuk! Pak je haakpen erbij. 🧶",
            listOf(Nodig("🧶", "wol"), Nodig("🪡", "haaknaald"), Nodig("✂️", "schaar")),
            listOf(
                Stap("🔁", "Maak een magische ring om je vinger."),
                Stap("🪡", "Haak 6 vasten in de ring."),
                Stap("🎯", "Trek het eindje aan. De ring gaat dicht."),
                Stap("✨", "Sluit af met een halve vaste. Klaar!"),
            ),
            listOf("haken", "haak", "rondje", "magische ring", "wol"),
        ),
        Uitleg(
            "rijst", "koken", "Hoe kook ik rijst? 🍚", "Makkelijk! Dit kun jij. 🍳",
            listOf(Nodig("🍚", "rijst"), Nodig("🍲", "pan met deksel"), Nodig("💧", "water")),
            listOf(
                Stap("🍚", "Doe 1 kopje rijst en 2 kopjes water in de pan."),
                Stap("🔥", "Zet het vuur hoog tot het kookt."),
                Stap("⏲️", "Deksel erop, vuur laag, 12 minuten wachten."),
                Stap("😋", "Vuur uit, nog 5 minuten laten staan. Klaar!"),
            ),
            listOf("rijst", "koken", "pan"),
        ),
        Uitleg(
            "plant", "planten", "Mijn plant hangt slap 🪴", "Even kijken wat je plant nodig heeft. 🌱",
            listOf(Nodig("💧", "gieter"), Nodig("👆", "je vinger")),
            listOf(
                Stap("👆", "Voel met je vinger in de aarde. Is het droog?"),
                Stap("💧", "Droog? Geef water tot het onderin eruit loopt."),
                Stap("☀️", "Zet de plant op een lichte plek, niet in de felle zon."),
                Stap("⏳", "Wacht een dag. Meestal staat hij weer rechtop!"),
            ),
            listOf("plant", "slap", "water", "gieten", "aarde"),
        ),
    )

    fun van(id: String): Uitleg? = uitleg.firstOrNull { it.id == id }

    fun eersteVan(soort: String): Uitleg = uitleg.firstOrNull { it.soort == soort } ?: uitleg.first()

    /** Zoek een vaste uitleg bij een eigen vraag. Beste treffer eerst; leeg als er niks past. */
    fun zoek(vraag: String): List<Uitleg> {
        val woorden = vraag.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 3 }
        if (woorden.isEmpty()) return emptyList()
        return uitleg.map { u ->
            val alles = (listOf(u.vraag, u.intro) + u.woorden + u.nodig.map { it.label } + u.stappen.map { it.tekst })
                .joinToString(" ").lowercase()
            val score = woorden.map { w ->
                when {
                    u.woorden.any { it == w || w.startsWith(it) || it.startsWith(w) } -> 3
                    w in alles -> 1
                    else -> 0
                }
            }.sum()
            u to score
        }.filter { it.second >= 2 }.sortedByDescending { it.second }.map { it.first }
    }

    /** Alles van één uitleg om voor te lezen. */
    fun voorleesTekst(u: Uitleg): String =
        u.vraag.filter { it.isLetterOrDigit() || it.isWhitespace() || it in ".,?!" }.trim() + ". " +
            "Dit heb je nodig: " + u.nodig.joinToString(", ") { it.label } + ". " +
            u.stappen.mapIndexed { i, s -> "Stap ${i + 1}. ${s.tekst}" }.joinToString(" ")
}

/** Wat Advies op de telefoon bewaart: je ⭐-favorieten. */
data class AdviesData(val favorieten: List<String> = emptyList()) {
    fun wissel(id: String) = copy(favorieten = if (id in favorieten) favorieten - id else favorieten + id)
}

/** Advies-gegevens naar JSON en terug (advies.json). */
object AdviesJson {
    fun schrijf(d: AdviesData): String = JSONObject()
        .put("versie", 1)
        .put("favorieten", JSONArray().apply { d.favorieten.forEach { put(it) } })
        .toString()

    fun lees(tekst: String): AdviesData {
        val o = try {
            JSONObject(tekst)
        } catch (_: Exception) {
            return AdviesData()
        }
        val a = o.optJSONArray("favorieten") ?: return AdviesData()
        val ids = (0 until a.length()).mapNotNull { a.optString(it).takeIf { id -> Advies.van(id) != null } }.distinct()
        return AdviesData(ids)
    }
}

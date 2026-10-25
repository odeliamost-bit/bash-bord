package nl.odelia.dashboard.logica

/**
 * Mail-hulp (ontwerp/Mail.dc.html): vaste voorbeeldmails per onderwerp en toon.
 * Geen AI: de teksten staan hier vast. De app zet alleen een concept klaar; versturen doet Odelia zelf.
 */
object MailHulp {
    data class Onderwerp(val id: String, val emoji: String, val label: String)
    data class Toon(val id: String, val emoji: String, val label: String)

    /** Een concept: onderwerpregel en tekst. */
    data class Concept(val onderwerp: String, val tekst: String)

    val onderwerpen = listOf(
        Onderwerp("ziek", "🤒", "Ziek melden"),
        Onderwerp("leraar", "🎒", "Vraag aan leraar"),
        Onderwerp("uren", "⏰", "Uren naar Kim"),
        Onderwerp("anders", "💬", "Iets anders"),
    )

    val tonen = listOf(
        Toon("officieel", "👔", "Officieel"),
        Toon("gewoon", "😊", "Gewoon"),
    )

    const val NAAM_LANG = "Odelia van der Most"
    const val NAAM_KORT = "Odelia"

    /** Tekst voor "Iets anders" zolang je nog niks getypt of ingesproken hebt. */
    const val ANDERS_UITLEG = "Vertel me in het typveld of met 🎤 waar je mail over gaat. Dan schrijf ik hem voor je."

    private fun officieel(id: String) = id != "gewoon"

    /** "Beste meneer/mevrouw," of "Hoi," — Kim krijgt altijd "Hoi Kim,". */
    fun aanhef(onderwerp: String, toon: String): String = when {
        onderwerp == "uren" -> "Hoi Kim,"
        officieel(toon) -> "Beste meneer/mevrouw,"
        else -> "Hoi,"
    }

    fun groet(onderwerp: String, toon: String, korter: Boolean = false): String = when {
        onderwerp == "uren" -> "Met hartelijke groet,\n$NAAM_LANG"
        korter && officieel(toon) -> "Groet,\n$NAAM_LANG"
        korter -> "Groetjes,\n$NAAM_KORT"
        officieel(toon) -> "Met vriendelijke groet,\n$NAAM_LANG"
        else -> "Groetjes,\n$NAAM_KORT"
    }

    /** De middenstukken van de vaste mails: [lang, korter]. */
    private val middens = mapOf(
        "ziek" to mapOf(
            "officieel" to listOf(
                "Hierbij meld ik mij vandaag ziek. Ik laat het weten zodra ik weer beter ben.",
                "Ik meld mij vandaag ziek.",
            ),
            "gewoon" to listOf(
                "Ik ben vandaag helaas ziek. Ik laat weten wanneer ik weer beter ben.",
                "Ik ben vandaag ziek.",
            ),
        ),
        "leraar" to mapOf(
            "officieel" to listOf(
                "Ik heb een vraag over de opdracht. Kunt u mij uitleggen wat er precies bedoeld wordt?\n\nAlvast bedankt.",
                "Kunt u de opdracht nog een keer uitleggen?",
            ),
            "gewoon" to listOf(
                "Ik snap de opdracht nog niet helemaal. Kunt u het nog een keer uitleggen?\n\nDank u wel!",
                "Kunt u de opdracht nog een keer uitleggen?",
            ),
        ),
    )

    private val onderwerpRegels = mapOf("ziek" to "Ziekmelding", "leraar" to "Vraag over de opdracht", "uren" to Urenmail.ONDERWERP)

    /** Aanhef, middenstuk en groet aan elkaar. */
    fun opbouw(aanhef: String, midden: String, groet: String): String = "$aanhef\n\n${midden.trim()}\n\n$groet"

    /**
     * Het concept bij een onderwerp en toon.
     * [eigen] = wat Odelia zelf typte of insprak: dat wordt het middenstuk, met aanhef en groet eromheen.
     * [urenTekst] = de echte urenmail (Urenmail.tekst met [eigen] als opmerking), voor "Uren naar Kim".
     */
    fun concept(onderwerp: String, toon: String, korter: Boolean = false, eigen: String = "", urenTekst: String? = null): Concept {
        val eigenTekst = eigen.trim()
        val regel = onderwerpRegels[onderwerp] ?: onderwerpVan(eigenTekst)
        if (onderwerp == "uren") {
            // De echte urenmail (met tabel). Je eigen tekst is dan de opmerking voor Kim.
            val midden = listOf("Hier de uren van afgelopen week.", eigenTekst).filter { it.isNotEmpty() }.joinToString("\n")
            return Concept(regel, urenTekst ?: opbouw(aanhef("uren", toon), midden, groet("uren", toon)))
        }
        if (eigenTekst.isNotEmpty()) {
            return Concept(regel, opbouw(aanhef(onderwerp, toon), eigenTekst, groet(onderwerp, toon, korter)))
        }
        val midden = middens[onderwerp]?.get(toon) ?: return Concept(regel, ANDERS_UITLEG)
        return Concept(regel, opbouw(aanhef(onderwerp, toon), midden[if (korter) 1 else 0], groet(onderwerp, toon, korter)))
    }

    /** Is dit concept nog maar uitleg (dus nog geen echte mail)? */
    fun isUitleg(c: Concept) = c.tekst == ANDERS_UITLEG

    /** Onderwerpregel uit je eigen tekst: de eerste woorden (max 6), of "Bericht". */
    fun onderwerpVan(tekst: String): String {
        val eerste = tekst.trim().split(Regex("[.!?\\n]")).firstOrNull().orEmpty().trim()
        val woorden = eerste.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (woorden.isEmpty()) return "Bericht"
        val kort = woorden.take(6).joinToString(" ")
        return kort.replaceFirstChar { it.uppercase() } + if (woorden.size > 6) "…" else ""
    }
}

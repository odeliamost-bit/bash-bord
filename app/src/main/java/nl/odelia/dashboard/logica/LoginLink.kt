package nl.odelia.dashboard.logica

/**
 * Inloggen met de link uit de mail (nov 2026, gekozen door Odelia).
 *
 * Nieuwe Supabase-projecten sturen alleen een link, geen code (de mail aanpassen kan pas met een eigen mailserver).
 * Odelia tikt op de link → Supabase controleert hem → stuurt door naar [TERUG] met
 * `#access_token=…&refresh_token=…` erachter → Android opent de app.
 * Ging het mis (verlopen, al gebruikt), dan staat er `error=…&error_description=…`.
 */
object LoginLink {
    const val SCHEMA = "bashbord"
    const val HOST = "login"
    const val TERUG = "$SCHEMA://$HOST"

    sealed interface Uitkomst {
        data class Ok(val ververs: String) : Uitkomst
        data class Fout(val bericht: String) : Uitkomst
    }

    /** [deel] = alles na de `#` (of na de `?`) van de link. */
    fun lees(deel: String?): Uitkomst {
        val w = waarden(deel.orEmpty())
        w["refresh_token"]?.takeIf { it.isNotBlank() && it.length <= 2000 && it.none(Char::isWhitespace) }
            ?.let { return Uitkomst.Ok(it) }
        val verlopen = w["error_code"] == "otp_expired" || "expired" in w["error_description"].orEmpty().lowercase()
        return Uitkomst.Fout(
            if (verlopen) "⌛ De link is verlopen of al gebruikt. Vraag een nieuwe mail aan."
            else "😕 Inloggen met de link lukte niet. Vraag een nieuwe mail aan.",
        )
    }

    fun waarden(deel: String): Map<String, String> =
        deel.removePrefix("#").removePrefix("?").split('&').mapNotNull { stuk ->
            val i = stuk.indexOf('=')
            if (i <= 0) null
            else stuk.substring(0, i) to try {
                java.net.URLDecoder.decode(stuk.substring(i + 1), "UTF-8")
            } catch (_: IllegalArgumentException) {
                ""
            }
        }.toMap()
}

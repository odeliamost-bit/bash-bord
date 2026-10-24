package nl.odelia.dashboard.uren

/** Wat je met de urenmail kunt doen. Niets hiervan verstuurt zelf een mail. */
class MailActies(
    /** Gmail (of een andere mail-app) openen met aan, onderwerp en tekst al ingevuld. */
    val openMail: (aan: String, onderwerp: String, tekst: String) -> Unit,
    /** Het "Delen"-menu van Android. */
    val deel: (onderwerp: String, tekst: String) -> Unit,
    val zetAdres: (String) -> Unit,
) {
    companion object {
        /** Doet niets (voor voorbeelden). */
        val niets = MailActies({ _, _, _ -> }, { _, _ -> }, {})
    }
}

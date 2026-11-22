package nl.odelia.dashboard.logica

/**
 * Melding **alleen** als een potje net onder nul gaat (bouwnacht 1, nov 2026).
 * Dat mag (het is virtueel geld), maar dan krijgt Odelia een advies om geld te verschuiven.
 */
object PotjeAlarm {
    data class Alarm(val potje: Potje, val saldo: Long, val advies: String)

    /** Potjes die vóór de wijziging 0 of meer hadden en nu onder nul staan. */
    fun nieuwOnderNul(oud: GeldData, nieuw: GeldData): List<Alarm> = nieuw.potjes.mapNotNull { p ->
        val voor = oud.potje(p.id)?.let { Geld.saldo(it, oud.boekingen) } ?: 0L
        val na = Geld.saldo(p, nieuw.boekingen)
        if (voor >= 0 && na < 0) Alarm(p, na, advies(nieuw, p, -na)) else null
    }

    /** Uit welk potje kan het tekort komen? Het potje met het meeste geld (liever niet Investeren, Noodpot of Inkomsten). */
    fun advies(d: GeldData, tekort: Potje, cent: Long): String {
        val kandidaten = d.potjes.filter { it.id != tekort.id }.map { it to Geld.saldo(it, d.boekingen) }.filter { it.second >= cent }
        val liefst = kandidaten.filter { it.first.id !in setOf("inv", "nood", "inkomsten") }.ifEmpty { kandidaten }
        val bron = liefst.maxByOrNull { it.second }
        return if (bron != null) {
            "Tip: zet ${Geld.bedrag(cent)} van ${bron.first.emoji} ${bron.first.naam} naar ${tekort.emoji} ${tekort.naam}."
        } else {
            "Geen enkel potje heeft genoeg. Wacht op je volgende loon, of verschuif een beetje uit meerdere potjes."
        }
    }
}

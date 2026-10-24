package nl.odelia.dashboard.ui.thuis

import androidx.compose.ui.graphics.Color
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels

/**
 * Verzonnen voorbeeld-getallen uit het ontwerp.
 * In stap 2 (klokken) en stap 3 (onthoud) komen hier echte gegevens.
 */
object Voorbeeld {
    class Dag(val naam: String, val uren: Float, val ring: Color, val staaf: List<Color>)

    val week = listOf(
        Dag("ma", 7.25f, Color(0xFFF7A8C4), listOf(Color(0xFFF6BCAE), Color(0xFFEE9F8E), Color(0xFFDD8574))),
        Dag("di", 7.25f, Color(0xFFF6CF5A), listOf(Color(0xFFF8D995), Color(0xFFF2C66D), Color(0xFFE0AE50))),
        Dag("wo", 7f, Color(0xFF8EC5F5), listOf(Color(0xFFBFD3EA), Color(0xFF9DBBDE), Color(0xFF7FA2CC))),
        Dag("do", 7.25f, Color(0xFFB9A3F0), listOf(Color(0xFFDAC8EA), Color(0xFFC6AEDD), Color(0xFFAE93CB))),
        Dag("vr", 5.5f, Color(0xFF9ACFAE), listOf(Color(0xFFB4D8BE), Color(0xFF8EC09C), Color(0xFF74A883))),
    )
    const val VANDAAG = 4 // vrijdag
    const val WEEKDOEL = 35f

    class Afspraak(val wat: String, val wie: String, val wanneer: String, val kleur: Pastel, val icoon: List<Vorm>)

    val afspraken = listOf(
        Afspraak("Verslag inleveren", "Dhr. Jansen", "ma\n26", Pastels.Koraal, Icoontjes.document),
        Afspraak("Tandarts bellen", "voor 17:00", "vandaag", Pastels.Geel, Icoontjes.telefoon),
        Afspraak("Formulier stage", "Mevr. Bakker", "do\n29", Pastels.Lila, Icoontjes.iemand),
    )
}

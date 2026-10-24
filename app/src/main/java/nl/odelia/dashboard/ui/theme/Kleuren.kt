package nl.odelia.dashboard.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Vaste kleuren uit het ontwerp (ontwerp/Main.dc.html en Navigatie.dc.html). */
object Kleur {
    // Achtergrond: zacht verloop perzik → crème
    val Perzik = Color(0xFFF4B29C)
    val PerzikLicht = Color(0xFFF8CDB9)
    val Zalm = Color(0xFFFBE6D9)
    val Creme = Color(0xFFFDF6EF)

    // Kaarten
    val Kaart = Color(0xFFFBF3EA)
    val KaartPil = Color(0xFFF5E8DA)
    val KaartSchaduw = Color(0xFFAA6E50)

    // Tekst
    val Inkt = Color(0xFF4A3A30)
    val InktZacht = Color(0xFF6E4A3A)
    val InktGrijs = Color(0xFF7A6656)

    // Hoofdkleur koraal
    val KoraalLicht = Color(0xFFF4A58B)
    val Koraal = Color(0xFFD9694F)
    val KoraalDiep = Color(0xFFB9533C)

    val Achtergrond = Brush.verticalGradient(
        0f to Perzik,
        0.24f to PerzikLicht,
        0.58f to Zalm,
        1f to Creme,
    )
}

/** Een pastel-setje: kaartkleur, icoon-verloop (licht → donker) en drie tekstkleuren. */
class Pastel(kaart: Long, licht: Long, donker: Long, label: Long, groot: Long, klein: Long) {
    val kaart = Color(kaart)
    val licht = Color(licht)
    val donker = Color(donker)
    val label = Color(label)
    val groot = Color(groot)
    val klein = Color(klein)
}

object Pastels {
    val Salie = Pastel(0xFFDDE9D7, 0xFF8EC09C, 0xFF5E9A70, 0xFF3D5E4A, 0xFF24452F, 0xFF3F7A58)
    val Geel = Pastel(0xFFFBE7C3, 0xFFF7D27A, 0xFFE3A93E, 0xFF6E5410, 0xFF4E3B08, 0xFF8A6A10)
    val Koraal = Pastel(0xFFFAD8CF, 0xFFF4A58B, 0xFFDC7258, 0xFF7A3A28, 0xFF5A2A1C, 0xFF9A4A34)
    val Blauw = Pastel(0xFFDCE6F1, 0xFF9DBBDE, 0xFF6E95C3, 0xFF2C4A70, 0xFF1F3F66, 0xFF3A5D8A)
    val Lila = Pastel(0xFFEADFF4, 0xFFC9B3E3, 0xFF9F84C9, 0xFF4F3D80, 0xFF3A2C66, 0xFF5E4A96)
    val Roze = Pastel(0xFFFBE0E4, 0xFFF5A9B8, 0xFFE07890, 0xFF7A2D4E, 0xFF5C1F3A, 0xFF9A4060)
    val Abrikoos = Pastel(0xFFFBE0CF, 0xFFF8C29C, 0xFFE28E5E, 0xFF7A3E12, 0xFF5A2E0C, 0xFF7A3E12)
}

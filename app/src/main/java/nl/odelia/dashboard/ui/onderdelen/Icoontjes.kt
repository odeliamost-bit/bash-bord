package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.ui.graphics.Color
import nl.odelia.dashboard.ui.theme.Kleur

/**
 * Alle icoontjes van het hoofdscherm, overgenomen uit ontwerp/Main.dc.html.
 * Een paar paden stonden in het ontwerp net uit het midden; die zijn met [verschuif] rechtgezet.
 */
object Icoontjes {
    private val creme = Kleur.Kaart
    private val wit = Color.White

    val persoon = listOf(
        Vorm(cirkel(12f, 9f, 4.5f), vul = creme),
        Vorm("M3.5 21a8.5 8.5 0 0 1 17 0z", vul = creme),
    ).verschuif(y = -0.75f)
    val bel = listOf(
        Vorm("M6 9a6 6 0 0 1 12 0c0 6 2.5 8 2.5 8h-17S6 15 6 9z", vul = Color(0xFF5E9070)),
        Vorm(cirkel(12f, 20f, 2f), vul = Color(0xFF5E9070)),
    )
    val zon = listOf(
        Vorm(cirkel(12f, 12f, 5f), vul = Color(0xFFF5C24E)),
        Vorm(
            "M12 2v2.5M12 19.5V22M2 12h2.5M19.5 12H22M4.9 4.9l1.8 1.8M17.3 17.3l1.8 1.8M4.9 19.1l1.8-1.8M17.3 6.7l1.8-1.8",
            lijn = Color(0xFFF5C24E), dikte = 2.2f,
        ),
    )
    val afspelen = listOf(Vorm("M7 4.5v15l12-7.5z", vul = wit))
    val plus = listOf(Vorm("M12 5v14M5 12h14", lijn = wit, dikte = 3f))
    val huis = listOf(Vorm("M3 11l9-7 9 7M5 10v10h14V10", lijn = wit, dikte = 2.6f))

    // Tabjes
    val tabVandaag = listOf(
        Vorm("M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zM11 1h2v3h-2zM11 20h2v3h-2zM1 11h3v2H1zM20 11h3v2h-3z", vul = Color(0xFFE3A93E)),
    )
    val tabWerk = listOf(
        Vorm("M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zm-1 4h2v4.4l3 1.8-1 1.7-4-2.4z", vul = Color(0xFF5E9A70)),
    )
    val tabOnthoud = listOf(Vorm("M6 3h12v18l-6-4.2L6 21z", vul = Color(0xFFDC7258)))

    // In- en uitklokken
    val uitklokken = listOf(
        Vorm("M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4", lijn = wit, dikte = 2.6f),
        Vorm("M16 17l5-5-5-5", lijn = wit, dikte = 2.6f),
        Vorm("M21 12H9", lijn = wit, dikte = 2.6f),
    )
    val inklokken = listOf(
        Vorm("M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4", lijn = wit, dikte = 2.6f),
        Vorm("M10 17l5-5-5-5", lijn = wit, dikte = 2.6f),
        Vorm("M15 12H3", lijn = wit, dikte = 2.6f),
    )

    // Tegels
    val klok = listOf(
        Vorm(cirkel(12f, 12f, 9f), vul = creme),
        Vorm("M12 7v5l3.2 2", lijn = Color(0xFF4F8A60), dikte = 2.4f),
    )
    val kalender = listOf(
        Vorm(rechthoek(3f, 5f, 18f, 16f, 3.5f), vul = creme),
        Vorm(rechthoek(3f, 5f, 18f, 5f, 2.5f), vul = wit),
        Vorm(cirkel(8f, 14.5f, 1.6f), vul = Color(0xFFD39A2E)),
        Vorm(cirkel(12f, 14.5f, 1.6f), vul = Color(0xFFD39A2E)),
        Vorm(cirkel(16f, 14.5f, 1.6f), vul = Color(0xFFD39A2E)),
    ).verschuif(y = -1f)
    val bladwijzer = listOf(Vorm("M6 3h12v18l-6-4.2L6 21z", vul = creme, lijn = creme, dikte = 1.5f))
    val mail = listOf(
        Vorm(rechthoek(3f, 5.5f, 18f, 13f, 3f), vul = creme),
        Vorm("M4 7.5l8 5.5 8-5.5", lijn = Color(0xFF6E95C3), dikte = 2f),
    )

    // Onthoud-lijstje
    val document = listOf(
        Vorm("M6 3h8l5 5v13H6z", vul = creme),
        Vorm("M14 3v5h5", vul = Color(0xFFF3D9CC)),
        Vorm("M9 13h7M9 17h5", lijn = Color(0xFFDC7258), dikte = 2f),
    ).verschuif(x = -0.5f)
    val telefoon = listOf(
        Vorm(
            "M22 16.9v3a2 2 0 0 1-2.2 2A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7l.7 2.8-1.8 3.4a16 16 0 0 0 6 6l3.4-1.8 2.8.7a2 2 0 0 1 1.7 2z",
            vul = creme,
        ),
    )
    val iemand = listOf(
        Vorm(cirkel(12f, 8f, 4.2f), vul = creme),
        Vorm("M4 21a8 8 0 0 1 16 0z", vul = creme),
    ).verschuif(y = -0.4f)

    // Snel naar
    val sleutel = listOf(
        Vorm("M14.7 6.3a4 4 0 0 0 5 5l-8.9 8.9a2.1 2.1 0 0 1-3-3z", lijn = creme, dikte = 2.8f),
        Vorm("M14.7 6.3l3-3a4 4 0 0 1 2 5", lijn = creme, dikte = 2.8f),
    ).verschuif(x = -1.55f)
    val praatwolk = listOf(
        Vorm("M21 12a8 8 0 0 1-11.6 7.1L4 20l1-4.6A8 8 0 1 1 21 12z", vul = creme),
        Vorm(cirkel(8.5f, 12f, 1.3f), vul = Color(0xFF9F84C9)),
        Vorm(cirkel(12f, 12f, 1.3f), vul = Color(0xFF9F84C9)),
        Vorm(cirkel(15.5f, 12f, 1.3f), vul = Color(0xFF9F84C9)),
    ).verschuif(x = -0.5f, y = -0.75f)
    val mensen = listOf(
        Vorm(cirkel(9f, 8f, 3.6f), vul = creme),
        Vorm("M2.5 20a6.5 6.5 0 0 1 13 0z", vul = creme),
        Vorm(cirkel(17f, 9f, 2.6f), vul = Color(0xFFE3EFE3)),
        Vorm("M14.5 20a5 5 0 0 1 7.5-4.3V20z", vul = Color(0xFFE3EFE3)),
    ).verschuif(x = -0.25f, y = -0.2f)
    val week = listOf(
        Vorm(rechthoek(3f, 5f, 18f, 16f, 3.5f), vul = creme),
        Vorm(rechthoek(3f, 5f, 18f, 5f, 2.5f), vul = wit),
        Vorm(cirkel(8f, 14.5f, 1.6f), vul = Color(0xFF6E95C3)),
        Vorm(cirkel(12f, 14.5f, 1.6f), vul = Color(0xFF6E95C3)),
    ).verschuif(y = -1f)
    val hart = listOf(
        Vorm("M20.8 5.6a5.5 5.5 0 0 0-7.8 0L12 6.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 22l8.8-8.6a5.5 5.5 0 0 0 0-7.8z", vul = creme),
    ).verschuif(y = -1f)

    // Onthoud (ontwerp/Toevoegen.dc.html en Mensen.dc.html)
    val microfoon = listOf(
        Vorm(rechthoek(9f, 3f, 6f, 11f, 3f), lijn = wit, dikte = 2.4f),
        Vorm("M5 11a7 7 0 0 0 14 0M12 18v3", lijn = wit, dikte = 2.4f),
    )
    val microfoonPaars = listOf(
        Vorm(rechthoek(9f, 3f, 6f, 11f, 3f), lijn = Color(0xFF6A4BC4), dikte = 2.4f),
        Vorm("M5 11a7 7 0 0 0 14 0M12 18v3", lijn = Color(0xFF6A4BC4), dikte = 2.4f),
    )
    val zoek = listOf(Vorm(cirkel(11f, 11f, 7f) + "M20 20l-4-4", lijn = Color(0xFF9A7A6A), dikte = 2.6f))
    val plakken = listOf(
        Vorm(rechthoek(8f, 2.5f, 8f, 4f, 1.5f), lijn = Color(0xFF6A4BC4), dikte = 2.2f),
        Vorm("M16 4.5h2a2 2 0 0 1 2 2V19a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6.5a2 2 0 0 1 2-2h2", lijn = Color(0xFF6A4BC4), dikte = 2.2f),
    )
    val vinkje = listOf(Vorm("M5 12.5l4.5 4.5L19 7.5", lijn = wit, dikte = 3f))
    val kopWie = listOf(Vorm(cirkel(12f, 8f, 4f) + "M4 21a8 8 0 0 1 16 0", lijn = Color(0xFF6A4BC4), dikte = 2.4f))
    val kopWat = listOf(Vorm("M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9zM14 3v6h6", lijn = Color(0xFFC2457A), dikte = 2.4f))
    val kopWanneer = listOf(
        Vorm(rechthoek(3f, 5f, 18f, 16f, 3f), lijn = Color(0xFF2F6DB5), dikte = 2.4f),
        Vorm("M3 10h18M8 3v4M16 3v4", lijn = Color(0xFF2F6DB5), dikte = 2.4f),
    )
    val kopWaar = listOf(Vorm("M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21z" + cirkel(12f, 9.5f, 2.5f), lijn = Color(0xFF3F7A58), dikte = 2.4f))
    val seintje = listOf(Vorm("M6 9a6 6 0 0 1 12 0c0 6 2.5 8 2.5 8h-17S6 15 6 9zM10 20.5a2 2 0 0 0 4 0", lijn = Color(0xFFB0820A), dikte = 2.4f))
    val mailWit = listOf(
        Vorm("M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM3.5 7l8.5 6 8.5-6", lijn = wit, dikte = 2.4f),
    )
    val plusPaars = listOf(Vorm("M12 5v14M5 12h14", lijn = Color(0xFF6A4BC4), dikte = 2.8f))

    // Menu-knop
    val pijlRechts = listOf(Vorm("M9 6l6 6-6 6", lijn = wit, dikte = 2.6f))
    val pijlLinks = listOf(Vorm("M15 6l-6 6 6 6", lijn = wit, dikte = 2.6f))
    val terug = listOf(Vorm("M15 6l-6 6 6 6", lijn = Kleur.InktZacht, dikte = 2.6f))
}

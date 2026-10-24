package nl.odelia.dashboard.ui.onthoud

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import nl.odelia.dashboard.onthoud.Groep
import nl.odelia.dashboard.onthoud.Soort
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.theme.Pastel
import nl.odelia.dashboard.ui.theme.Pastels

/** Icoontje van een soort (uit ontwerp/Toevoegen.dc.html), als lijntjes in [kleur]. */
fun soortIcoon(soort: Soort, kleur: Color = Color(0xFF3A3046), dikte: Float = 2.2f): List<Vorm> = listOf(
    Vorm(
        when (soort) {
            Soort.Inleveren -> "M12 3v12M7 10l5 5 5-5M5 21h14"
            Soort.Maken -> "M12 20h9M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z"
            Soort.Bellen -> "M22 16.9v3a2 2 0 0 1-2.2 2A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7l.7 2.8-1.8 3.4a16 16 0 0 0 6 6l3.4-1.8 2.8.7a2 2 0 0 1 1.7 2z"
            Soort.Mailen -> "M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM3.5 7l8.5 6 8.5-6"
            Soort.Meenemen -> "M6 7h12l-1 13H7zM9 7a3 3 0 0 1 6 0"
            Soort.Vragen -> "M9 9a3 3 0 1 1 4 2.8c-.6.3-1 .9-1 1.6V14M12 18h.01"
        },
        lijn = kleur,
        dikte = dikte,
    ),
)

/** Vlakkleur van een soort-knop (uit het ontwerp). */
fun soortKleur(soort: Soort): Color = when (soort) {
    Soort.Inleveren -> Color(0xFFFFD6E3)
    Soort.Maken -> Color(0xFFE6DBFF)
    Soort.Bellen -> Color(0xFFFFF1B8)
    Soort.Mailen -> Color(0xFFD4E8FF)
    Soort.Meenemen -> Color(0xFFD6EBDD)
    Soort.Vragen -> Color(0xFFFFE3CC)
}

/** Pastel voor het blokje voor een afspraak in een lijst. */
fun soortPastel(soort: Soort?): Pastel = when (soort) {
    Soort.Inleveren, null -> Pastels.Koraal
    Soort.Maken -> Pastels.Lila
    Soort.Bellen -> Pastels.Geel
    Soort.Mailen -> Pastels.Blauw
    Soort.Meenemen -> Pastels.Salie
    Soort.Vragen -> Pastels.Abrikoos
}

/** Wit icoontje voor in het gekleurde blokje. Zonder soort: een bladwijzer. */
fun lijstIcoon(soort: Soort?): List<Vorm> = soort?.let { soortIcoon(it, Color(0xFFFBF3EA), 2.6f) } ?: Icoontjes.bladwijzer

/** Verloop voor het rondje van een persoon (kleuren uit ontwerp/Mensen.dc.html). */
fun persoonKleur(kleur: Int): Brush {
    val paren = listOf(
        Color(0xFFF6B3C8) to Color(0xFFDE7FA1),
        Color(0xFFD2BDF0) to Color(0xFFA88AD8),
        Color(0xFFAFCBEA) to Color(0xFF7FA5D2),
        Color(0xFFA8D6B4) to Color(0xFF6FAE84),
        Color(0xFFF8C29C) to Color(0xFFE9956A),
        Color(0xFFF7DA8A) to Color(0xFFE5B54E),
    )
    val (licht, donker) = paren[Math.floorMod(kleur, paren.size)]
    return hoekVerloop(licht, donker)
}

/** "🎒 School" */
fun groepTekst(groep: Groep) = "${groep.emoji} ${groep.label}"

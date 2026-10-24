package androidx.compose.ui.text.font

import java.io.File

/** Zelfde vorm als Android's Font(resId, weight), maar leest het lettertype uit app/src/main/res. */
fun Font(resId: Int, weight: FontWeight = FontWeight.Normal, style: FontStyle = FontStyle.Normal): Font {
    val naam = when (resId) {
        10 -> "nunito_semibold"
        11 -> "nunito_bold"
        12 -> "nunito_extrabold"
        13 -> "nunito_black"
        else -> error("onbekend lettertype $resId")
    }
    val bestand = File(System.getProperty("res"), "font/$naam.ttf")
    return androidx.compose.ui.text.platform.Font(bestand, weight, style)
}

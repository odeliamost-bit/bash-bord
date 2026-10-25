package nl.odelia.dashboard.logica

import java.net.URLEncoder

/**
 * Links om iets op te zoeken. De app zelf gaat hiermee niet online:
 * hij geeft de link aan Chrome of Google Maps, en die gaan online.
 */
object Links {
    /** Zoeken met Google. */
    fun zoek(term: String): String = "https://www.google.com/search?q=" + codeer(term)

    /** Een plek of adres in Google Maps (opent de Maps-app als die er is). */
    fun route(plek: String): String = "https://www.google.com/maps/search/?api=1&query=" + codeer(plek)

    private fun codeer(tekst: String) = URLEncoder.encode(tekst.trim().take(300), "UTF-8")
}

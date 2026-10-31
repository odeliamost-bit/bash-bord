package nl.odelia.dashboard.online

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stuurt uren naar het script in Odelia's Google-spreadsheet (gekozen door Odelia, okt 2026).
 *
 * - Alleen versleuteld (https) en alleen naar Google Apps Script (het bouw-recept controleert deze lijst).
 * - Het adres en de code komen uit GitHub-geheimen (SHEET_URL, SHEET_TOKEN), niet uit de code.
 * - Er gaan alleen datums, begin, eind, pauze en vrij/ziek mee. Verder niks.
 */
object Sheet {
    private val TOEGESTAAN = setOf("script.google.com", "script.googleusercontent.com")
    private const val MAX_BYTES = 100_000

    /** Stuurt [bericht] (JSON) en geeft het antwoord van het script terug. */
    suspend fun stuur(adres: String, bericht: String): String = withContext(Dispatchers.IO) {
        val post = open(adres)
        try {
            post.requestMethod = "POST"
            post.doOutput = true
            post.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            post.outputStream.use { it.write(bericht.toByteArray(Charsets.UTF_8)) }
            when (val code = post.responseCode) {
                200 -> post.inputStream.use(::leesBeperkt)
                // Apps Script doet het werk en stuurt je dan door naar het antwoord (op googleusercontent.com).
                301, 302, 303, 307 -> {
                    val verder = post.getHeaderField("Location") ?: throw IOException("Doorsturen zonder adres")
                    val get = open(verder)
                    try {
                        if (get.responseCode != 200) throw IOException("Antwoord ${get.responseCode}")
                        get.inputStream.use(::leesBeperkt)
                    } finally {
                        get.disconnect()
                    }
                }
                else -> throw IOException("Antwoord $code")
            }
        } finally {
            post.disconnect()
        }
    }

    private fun open(link: String): HttpsURLConnection {
        val url = URL(link)
        if (url.protocol != "https" || url.host !in TOEGESTAAN) throw IOException("Niet toegestaan: ${url.host}")
        return (url.openConnection() as HttpsURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 30_000
            instanceFollowRedirects = false
            setRequestProperty("User-Agent", "MijnDashboard (Android)")
        }
    }

    private fun leesBeperkt(stroom: InputStream): String {
        val uit = ByteArrayOutputStream()
        val stukje = ByteArray(8192)
        while (true) {
            val n = stroom.read(stukje)
            if (n < 0) break
            if (uit.size() + n > MAX_BYTES) throw IOException("Antwoord te groot")
            uit.write(stukje, 0, n)
        }
        return uit.toString("UTF-8")
    }
}

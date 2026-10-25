package nl.odelia.dashboard.online

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * HET ENIGE bestand in de app dat online gaat (het bouw-recept controleert dat).
 *
 * - Alleen als Odelia eerst "ja" heeft gezegd (de schermen vragen het elke keer).
 * - Alleen versleuteld (https) en alleen naar de weerdienst Open-Meteo.
 * - Niet langer dan een paar seconden wachten, en nooit meer dan 2 MB ophalen.
 * - Er gaat alleen een plek (naam of coördinaten) mee, verder niks.
 */
object Online {
    private val TOEGESTAAN = setOf("api.open-meteo.com", "geocoding-api.open-meteo.com")
    private const val MAX_BYTES = 2_000_000

    suspend fun haal(link: String): String = withContext(Dispatchers.IO) {
        val url = URL(link)
        if (url.protocol != "https" || url.host !in TOEGESTAAN) throw IOException("Niet toegestaan: ${url.host}")
        val verbinding = url.openConnection() as HttpsURLConnection
        try {
            verbinding.connectTimeout = 10_000
            verbinding.readTimeout = 20_000
            verbinding.instanceFollowRedirects = false
            verbinding.setRequestProperty("User-Agent", "MijnDashboard (Android)")
            if (verbinding.responseCode != 200) throw IOException("Antwoord ${verbinding.responseCode}")
            verbinding.inputStream.use(::leesBeperkt)
        } finally {
            verbinding.disconnect()
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

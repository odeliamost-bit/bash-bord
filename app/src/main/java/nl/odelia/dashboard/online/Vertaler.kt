package nl.odelia.dashboard.online

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.odelia.dashboard.BuildConfig
import nl.odelia.dashboard.logica.Vertalen

/**
 * Vertalen via Claude, via de EIGEN backend (backend/vertaler, Cloudflare Worker).
 *
 * - De Claude-sleutel staat alleen in de backend. De app kent alleen het adres en een app-code
 *   (GitHub-geheimen VERTAAL_URL en VERTAAL_CODE, zie README).
 * - Alleen https, alleen naar dat ene adres, alleen als Odelia zelf op "Vertalen" tikt.
 * - Er gaat alleen de ingesproken tekst mee (max 2000 tekens), verder niks.
 */
object Vertaler {
    private const val MAX_BYTES = 100_000

    val ingesteld: Boolean
        get() = BuildConfig.VERTAAL_URL.startsWith("https:") && BuildConfig.VERTAAL_CODE.isNotEmpty()

    suspend fun vertaal(tekst: String, taal: Vertalen.Taal = Vertalen.Taal.Auto): String = withContext(Dispatchers.IO) {
        if (!ingesteld) throw IOException("Niet ingesteld")
        val body = Vertalen.verzoek(tekst, taal) ?: throw IOException("Geen tekst")
        val url = URL(BuildConfig.VERTAAL_URL)
        if (url.protocol != "https") throw IOException("Alleen https")
        val verbinding = url.openConnection() as HttpsURLConnection
        try {
            verbinding.requestMethod = "POST"
            verbinding.doOutput = true
            verbinding.connectTimeout = 10_000
            verbinding.readTimeout = 45_000
            verbinding.instanceFollowRedirects = false
            verbinding.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            verbinding.setRequestProperty("X-App-Code", BuildConfig.VERTAAL_CODE)
            verbinding.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            if (verbinding.responseCode != 200) throw IOException("Antwoord ${verbinding.responseCode}")
            val antwoord = verbinding.inputStream.use { stroom ->
                val uit = ByteArrayOutputStream()
                val stukje = ByteArray(8192)
                while (true) {
                    val n = stroom.read(stukje)
                    if (n < 0) break
                    if (uit.size() + n > MAX_BYTES) throw IOException("Antwoord te groot")
                    uit.write(stukje, 0, n)
                }
                uit.toString("UTF-8")
            }
            Vertalen.antwoord(antwoord) ?: throw IOException("Leeg antwoord")
        } finally {
            verbinding.disconnect()
        }
    }
}

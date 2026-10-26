package nl.odelia.dashboard.online

import java.io.IOException
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.odelia.dashboard.logica.SyncRegels
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

/**
 * Praat met Odelia's eigen Supabase-project, voor de sync tussen apparaten.
 *
 * - Alleen versleuteld (https) en alleen naar het eigen project ([basis], uit een GitHub-geheim).
 * - Inloggen met een code per e-mail. Geen wachtwoorden.
 * - Alleen de onderdelen uit [SyncRegels.ONLINE]. Uren, cyclus enz. komen hier nooit langs.
 * - Supabase laat alleen Odelia's eigen rijen zien (RLS, zie `supabase/kompas.sql`).
 */
class Supabase(private val basis: String, private val sleutel: String) {
    init {
        require(basis.startsWith("https://")) { "Alleen https" }
    }

    private val host = basis.toHttpUrl().host
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    data class Sessie(val email: String, val id: String, val toegang: String, val ververs: String, val verloopt: Long)
    data class Rij(val naam: String, val inhoud: String, val versie: Instant)
    class Fout(val code: Int, bericht: String) : IOException(bericht)

    // ---------- Inloggen (code per e-mail) ----------

    suspend fun vraagCode(email: String) {
        vraag("/auth/v1/otp", JSONObject().put("email", email).put("create_user", true))
    }

    suspend fun logIn(email: String, code: String): Sessie =
        sessie(vraag("/auth/v1/verify", JSONObject().put("type", "email").put("email", email).put("token", code)))

    suspend fun ververs(sessie: Sessie): Sessie =
        sessie(vraag("/auth/v1/token?grant_type=refresh_token", JSONObject().put("refresh_token", sessie.ververs)))

    suspend fun logUit(sessie: Sessie) {
        vraag("/auth/v1/logout", JSONObject(), sessie.toegang)
    }

    private fun sessie(tekst: String): Sessie {
        val o = JSONObject(tekst)
        val gebruiker = o.getJSONObject("user")
        return Sessie(
            email = gebruiker.optString("email"),
            id = gebruiker.getString("id"),
            toegang = o.getString("access_token"),
            ververs = o.getString("refresh_token"),
            verloopt = System.currentTimeMillis() + o.optLong("expires_in", 3600) * 1000,
        )
    }

    // ---------- Gegevens ----------

    suspend fun haal(sessie: Sessie, naam: String): Rij? {
        mag(naam)
        val tekst = vraag("/rest/v1/$TABEL?select=onderdeel,inhoud,updated_at&onderdeel=eq.$naam", null, sessie.toegang)
        val rijen = JSONArray(tekst)
        return if (rijen.length() == 0) null else rij(rijen.getJSONObject(0))
    }

    suspend fun zet(sessie: Sessie, naam: String, inhoud: String): Rij {
        mag(naam)
        val rij = JSONObject().put("user_id", sessie.id).put("onderdeel", naam).put("inhoud", inhoud)
        val tekst = vraag(
            "/rest/v1/$TABEL?on_conflict=user_id,onderdeel",
            JSONArray().put(rij),
            sessie.toegang,
            "Prefer" to "resolution=merge-duplicates,return=representation",
        )
        return rij(JSONArray(tekst).getJSONObject(0))
    }

    private fun mag(naam: String) {
        if (naam !in SyncRegels.ONLINE) throw IOException("$naam mag niet online")
    }

    private fun rij(o: JSONObject) = Rij(o.getString("onderdeel"), o.getString("inhoud"), tijd(o.getString("updated_at")))

    private suspend fun vraag(pad: String, body: Any?, token: String? = null, extra: Pair<String, String>? = null): String =
        withContext(Dispatchers.IO) {
            val verzoek = Request.Builder().url(basis + pad).header("apikey", sleutel)
            if (token != null) verzoek.header("Authorization", "Bearer $token")
            if (extra != null) verzoek.header(extra.first, extra.second)
            if (body != null) verzoek.post(body.toString().toRequestBody(JSON))
            client.newCall(veilig(verzoek.build())).execute().use(::lees)
        }

    private fun veilig(verzoek: Request): Request {
        if (!verzoek.isHttps || verzoek.url.host != host) throw IOException("Niet toegestaan: ${verzoek.url.host}")
        return verzoek
    }

    private fun lees(antwoord: Response): String {
        if ((antwoord.body?.contentLength() ?: 0) > MAX_BYTES) throw IOException("Antwoord te groot")
        val tekst = antwoord.body?.string().orEmpty()
        if (!antwoord.isSuccessful) throw Fout(antwoord.code, tekst.take(200))
        return tekst
    }

    // ---------- Realtime: meteen horen als een ander apparaat iets verandert ----------

    interface Luisteraar {
        fun open()
        /** Er is iets veranderd (naam van het onderdeel, of `null` als dat onbekend is). */
        fun wijziging(naam: String?)
        fun dicht()
    }

    /** Verbinding openhouden. Geeft een functie terug om hem te sluiten. */
    fun luister(sessie: Sessie, luisteraar: Luisteraar): () -> Unit {
        val werk = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        var hartslag: Job? = null
        var teller = 1
        val onderwerp = "realtime:kompas"
        val verzoek = Request.Builder()
            .url("$basis/realtime/v1/websocket?apikey=$sleutel&vsn=1.0.0")
            .build()

        fun stuur(ws: WebSocket, topic: String, event: String, payload: JSONObject) {
            ws.send(JSONObject().put("topic", topic).put("event", event).put("payload", payload).put("ref", (teller++).toString()).toString())
        }

        val ws = client.newWebSocket(veilig(verzoek), object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val wijzigingen = JSONObject().put("event", "*").put("schema", "public").put("table", TABEL)
                val config = JSONObject().put("postgres_changes", JSONArray().put(wijzigingen))
                stuur(webSocket, onderwerp, "phx_join", JSONObject().put("config", config).put("access_token", sessie.toegang))
                hartslag = werk.launch {
                    while (isActive) {
                        delay(25_000)
                        stuur(webSocket, "phoenix", "heartbeat", JSONObject())
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val o = try {
                    JSONObject(text)
                } catch (_: Exception) {
                    return
                }
                val payload = o.optJSONObject("payload") ?: JSONObject()
                when (o.optString("event")) {
                    "phx_reply" -> if (o.optString("topic") == onderwerp) {
                        if (payload.optString("status") == "ok") luisteraar.open() else webSocket.close(1000, null)
                    }
                    "postgres_changes" -> {
                        val naam = payload.optJSONObject("data")?.optJSONObject("record")?.optString("onderdeel")
                        luisteraar.wijziging(naam?.takeIf { it.isNotEmpty() })
                    }
                    "system" -> if (payload.optString("status") == "error") webSocket.close(1000, null)
                    "phx_error", "phx_close" -> webSocket.close(1000, null)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = klaar()

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) = klaar()

            private fun klaar() {
                hartslag?.cancel()
                luisteraar.dicht()
            }
        })
        return {
            hartslag?.cancel()
            ws.close(1000, null)
        }
    }

    companion object {
        const val TABEL = "kompas_data"
        private const val MAX_BYTES = 5_000_000L
        private val JSON = "application/json".toMediaType()

        /** Tijd van Supabase, bijv. "2026-10-26T12:34:56.123456+00:00". */
        fun tijd(tekst: String): Instant = OffsetDateTime.parse(tekst.replace(' ', 'T')).toInstant()
    }
}

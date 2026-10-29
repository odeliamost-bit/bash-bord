package nl.odelia.dashboard.online

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.odelia.dashboard.BuildConfig
import nl.odelia.dashboard.Acties
import nl.odelia.dashboard.data.Bestanden
import nl.odelia.dashboard.data.GeldOpslag
import nl.odelia.dashboard.data.LerenOpslag
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.data.ReceptenOpslag
import nl.odelia.dashboard.logica.InstellingenJson
import nl.odelia.dashboard.logica.WensenJson
import nl.odelia.dashboard.logica.SyncRegels
import nl.odelia.dashboard.logica.SyncRegels.Stand
import nl.odelia.dashboard.logica.SyncRegels.Stap
import nl.odelia.dashboard.ui.SyncStaat
import org.json.JSONObject

/**
 * Sync tussen telefoon, tablet en laptop via Odelia's eigen Supabase.
 *
 * - Alles blijft ook op het apparaat; de app werkt dus zonder internet.
 * - Alleen na inloggen, en alleen de onderdelen uit [SyncRegels.ONLINE]
 *   (nu: Geld, Onthoud/Mensen, Wensen, Leren, Recepten, Instellingen; foto's nog niet).
 * - Eigen wijziging: meteen versturen. Wijziging van een ander apparaat: meteen ophalen (realtime).
 * - De inlog en de sync-stand staan in `noBackupFilesDir`, niet in de Google-back-up.
 */
object Sync {
    /** Eén onderdeel. [na] = na het overnemen van een ander apparaat (bijv. meldingen opnieuw zetten). */
    private class Deel(val tekst: suspend () -> String, val vervang: suspend (String) -> Unit, val na: suspend () -> Unit = {})

    private val werk = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val slot = Mutex()
    private val _staat = MutableStateFlow(SyncStaat())
    val staat: StateFlow<SyncStaat> = _staat.asStateFlow()

    private var supabase: Supabase? = null
    private lateinit var map: File
    @Volatile private var sessie: Supabase.Sessie? = null
    private val delen = mutableMapOf<String, Deel>()
    private val standen = mutableMapOf<String, Stand>()
    private val wachtrij = mutableMapOf<String, Job>()

    @Volatile private var aan = false
    @Volatile private var generatie = 0
    private val verbinding = Any()
    private var sluit: (() -> Unit)? = null
    private var pogingen = 0

    @Synchronized
    fun start(context: Context) {
        if (::map.isInitialized) return
        val app = context.applicationContext
        map = app.noBackupFilesDir
        supabase = try {
            Supabase(BuildConfig.SUPABASE_URL.trimEnd('/'), BuildConfig.SUPABASE_ANON_KEY).takeIf { BuildConfig.SUPABASE_ANON_KEY.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        sessie = leesSessie()
        standen.putAll(leesStanden())

        // Volgorde = volgorde van bijwerken. Instellingen als laatste (nieuw in Supabase, zie README).
        val geld = GeldOpslag.van(app)
        delen["geld"] = Deel(geld::alsTekst, geld::vervang)
        geld.naWijziging = { lokaalVeranderd("geld") }

        // Onthoud. Mensen komt hier ook uit (geen eigen bestand). Foto's gaan (nog) niet mee.
        val opslag = Opslag.van(app)
        delen["onthoud"] = Deel(opslag::onthoudAlsTekst, opslag::vervangOnthoud, na = { Acties.naWijziging(app) })
        opslag.naOnthoud = { lokaalVeranderd("onthoud") }

        val wensen = Bestanden.wensen(app)
        delen["wensen"] = Deel(wensen::alsTekst, { t -> JSONObject(t); wensen.vervang { WensenJson.lees(t) } })
        wensen.naWijziging = { lokaalVeranderd("wensen") }

        val leren = LerenOpslag.van(app)
        delen["leren"] = Deel(leren::alsTekst, leren::vervang)
        leren.naWijziging = { lokaalVeranderd("leren") }

        val recepten = ReceptenOpslag.van(app)
        delen["recepten"] = Deel(recepten::alsTekst, recepten::vervang)
        recepten.naWijziging = { lokaalVeranderd("recepten") }

        // Instellingen: grote tekst en tegels blijven per apparaat.
        val instellingen = Bestanden.instellingen(app)
        delen["instellingen"] = Deel(
            tekst = { InstellingenJson.schrijfGedeeld(instellingen.nu()) },
            vervang = { t -> instellingen.vervang { hier -> InstellingenJson.neemGedeeld(hier, t) } },
            na = { Acties.naWijziging(app) },
        )
        instellingen.naWijziging = { lokaalVeranderd("instellingen") }

        _staat.value = SyncStaat(kan = supabase != null, email = sessie?.email)
    }

    /** App komt in beeld: alles bijwerken en de realtime-verbinding openen. */
    fun aan() {
        aan = true
        if (sessie != null) werk.launch {
            allesBijwerken()
            verbind()
        }
    }

    /** App uit beeld: verbinding dicht (scheelt batterij). Bij openen wordt alles weer bijgewerkt. */
    fun uit() {
        aan = false
        verbreek()
    }

    // ---------- Inloggen ----------

    fun vraagCode(email: String) = bezig {
        val schoon = email.trim().lowercase()
        supabase!!.vraagCode(schoon)
        _staat.update { it.copy(codeVoor = schoon, bericht = "📬 Code gestuurd naar $schoon") }
    }

    fun logIn(code: String) = bezig {
        val email = _staat.value.codeVoor ?: return@bezig
        val nieuw = supabase!!.logIn(email, code.filter(Char::isDigit))
        sessie = nieuw
        bewaarSessie(nieuw)
        _staat.update { it.copy(email = nieuw.email, codeVoor = null, bericht = "✅ Ingelogd") }
        if (aan) {
            allesBijwerken()
            verbind()
        }
    }

    fun logUit() = bezig {
        val oud = sessie ?: return@bezig
        verbreek()
        sessie = null
        File(map, SESSIE).delete()
        slot.withLock {
            standen.clear()
            bewaarStanden()
        }
        _staat.value = SyncStaat(kan = true, bericht = "👋 Uitgelogd. Alles staat nog op dit apparaat.")
        try {
            supabase!!.logUit(oud)
        } catch (_: Exception) {
        }
    }

    fun nu() = bezig {
        allesBijwerken()
        if (aan && !_staat.value.live) verbind()
    }

    private fun bezig(wat: suspend () -> Unit) {
        if (supabase == null) return
        werk.launch {
            _staat.update { it.copy(bezig = true, bericht = null) }
            try {
                wat()
            } catch (e: Exception) {
                _staat.update { it.copy(bericht = uitleg(e)) }
            } finally {
                _staat.update { it.copy(bezig = false) }
            }
        }
    }

    // ---------- Bijwerken ----------

    private fun lokaalVeranderd(naam: String) {
        werk.launch {
            slot.withLock {
                standen[naam] = (standen[naam] ?: Stand()).copy(vies = true, gewijzigd = Instant.now())
                bewaarStanden()
            }
            if (sessie == null) return@launch
            // Even wachten: bij snel tikken maar één keer versturen.
            synchronized(wachtrij) {
                wachtrij[naam]?.cancel()
                wachtrij[naam] = werk.launch {
                    delay(1_500)
                    probeer { bijwerken(naam) }
                }
            }
        }
    }

    private suspend fun allesBijwerken() = probeer {
        for (naam in delen.keys) bijwerken(naam)
    }

    private suspend fun probeer(wat: suspend () -> Unit) {
        try {
            wat()
            _staat.update { it.copy(laatste = LocalDateTime.now(), bericht = null) }
        } catch (e: Exception) {
            _staat.update { it.copy(bericht = uitleg(e)) }
        }
    }

    private suspend fun bijwerken(naam: String) = slot.withLock {
        val sb = supabase ?: return@withLock
        val deel = delen[naam] ?: return@withLock
        val s = geldigeSessie() ?: return@withLock
        val online = sb.haal(s, naam)
        val stand = standen[naam] ?: Stand()
        when (SyncRegels.kies(stand, online?.versie)) {
            Stap.Niks -> return@withLock
            Stap.Neem -> {
                online!!
                // Kan het gelezen worden? Anders niks overschrijven.
                JSONObject(online.inhoud)
                // De eerste keer: wat hier stond als reserve bewaren.
                if (stand.versie == null) {
                    val reserve = File(map, "reserve-$naam.json")
                    if (!reserve.exists()) reserve.writeText(deel.tekst())
                }
                deel.vervang(online.inhoud)
                standen[naam] = Stand(versie = online.versie)
                try {
                    deel.na()
                } catch (_: Exception) {
                }
            }
            Stap.Verstuur -> {
                val rij = sb.zet(s, naam, deel.tekst())
                standen[naam] = Stand(versie = rij.versie)
            }
        }
        bewaarStanden()
    }

    /** De inlog, ververst als hij bijna verloopt. `null` = niet (meer) ingelogd. */
    private suspend fun geldigeSessie(): Supabase.Sessie? {
        val s = sessie ?: return null
        if (s.verloopt - System.currentTimeMillis() > 120_000) return s
        return try {
            supabase!!.ververs(s).also {
                sessie = it
                bewaarSessie(it)
            }
        } catch (e: Supabase.Fout) {
            if (e.code in 400..499) {
                sessie = null
                File(map, SESSIE).delete()
                _staat.value = SyncStaat(kan = true, bericht = "🔑 Log opnieuw in.")
                null
            } else throw e
        }
    }

    // ---------- Realtime ----------

    private suspend fun verbind() {
        val s = try {
            geldigeSessie()
        } catch (_: Exception) {
            null
        }
        synchronized(verbinding) {
            verbreek()
            if (!aan) return
            val nr = generatie
            if (s == null) {
                opnieuw(nr)
            } else {
                sluit = luister(s, nr)
            }
        }
    }

    private fun luister(s: Supabase.Sessie, nr: Int) =
        supabase!!.luister(s, object : Supabase.Luisteraar {
            override fun open() {
                if (nr != generatie) return
                // Na een onderbreking kan er iets gemist zijn.
                if (pogingen > 0) werk.launch { allesBijwerken() }
                pogingen = 0
                _staat.update { it.copy(live = true) }
            }

            override fun wijziging(naam: String?) {
                if (nr != generatie) return
                werk.launch { if (naam != null && naam in delen) probeer { bijwerken(naam) } else allesBijwerken() }
            }

            override fun dicht() {
                if (nr != generatie) return
                _staat.update { it.copy(live = false) }
                opnieuw(nr)
            }
        })

    /** Verbinding kwijt (geen internet, inlog verlopen …): over een tijdje opnieuw. */
    private fun opnieuw(nr: Int) {
        if (!aan || sessie == null) return
        val wacht = minOf(60_000L, 5_000L shl minOf(pogingen, 4))
        pogingen++
        werk.launch {
            delay(wacht)
            if (nr == generatie && aan) verbind()
        }
    }

    private fun verbreek() = synchronized(verbinding) {
        generatie++
        sluit?.invoke()
        sluit = null
        _staat.update { it.copy(live = false) }
    }

    // ---------- Bewaren op het apparaat ----------

    private fun bewaarSessie(s: Supabase.Sessie) = schrijf(
        SESSIE,
        JSONObject().put("email", s.email).put("id", s.id).put("toegang", s.toegang).put("ververs", s.ververs).put("verloopt", s.verloopt),
    )

    private fun leesSessie(): Supabase.Sessie? = lees(SESSIE)?.let {
        Supabase.Sessie(it.getString("email"), it.getString("id"), it.getString("toegang"), it.getString("ververs"), it.getLong("verloopt"))
    }

    private fun bewaarStanden() = schrijf(STANDEN, JSONObject().apply {
        for ((naam, s) in standen) put(naam, JSONObject().put("versie", s.versie?.toString()).put("vies", s.vies).put("gewijzigd", s.gewijzigd?.toString()))
    })

    private fun leesStanden(): Map<String, Stand> = lees(STANDEN)?.let { o ->
        o.keys().asSequence().associateWith { naam ->
            val s = o.getJSONObject(naam)
            Stand(
                versie = s.optString("versie").takeIf { it.isNotEmpty() }?.let(Instant::parse),
                vies = s.optBoolean("vies"),
                gewijzigd = s.optString("gewijzigd").takeIf { it.isNotEmpty() }?.let(Instant::parse),
            )
        }
    } ?: emptyMap()

    private fun lees(naam: String): JSONObject? = try {
        JSONObject(String(AtomicFile(File(map, naam)).readFully(), Charsets.UTF_8))
    } catch (_: Exception) {
        null
    }

    private fun schrijf(naam: String, o: JSONObject) {
        val bestand = AtomicFile(File(map, naam))
        val uit = try {
            bestand.startWrite()
        } catch (_: Exception) {
            return
        }
        try {
            uit.write(o.toString().toByteArray(Charsets.UTF_8))
            bestand.finishWrite(uit)
        } catch (_: Exception) {
            bestand.failWrite(uit)
        }
    }

    private fun uitleg(e: Exception): String = when {
        e is Supabase.Fout && e.code == 429 -> "⏳ Even wachten, probeer het over een minuut opnieuw."
        e is Supabase.Fout && e.code in listOf(400, 401, 403) && _staat.value.codeVoor != null -> "❌ Die code klopt niet of is verlopen."
        e is Supabase.Fout -> "⚠️ Supabase gaf een fout (${e.code})."
        else -> "📵 Geen verbinding. Alles staat veilig op dit apparaat; ik probeer het later opnieuw."
    }

    private const val SESSIE = "sync-inlog.json"
    private const val STANDEN = "sync-stand.json"
}

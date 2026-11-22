package nl.odelia.dashboard.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nl.odelia.dashboard.logica.Plek
import org.json.JSONObject

/**
 * Bewaart de zeilplek op de telefoon (niet in de code: die is openbaar).
 * Gaat versleuteld mee in de sync (nov 2026), zie `online/Sync.kt`.
 */
object PlekOpslag {
    private const val BESTAND = "instellingen"
    @Volatile private var stroom: MutableStateFlow<Plek?>? = null

    /** Voor de sync: na elke eigen wijziging. */
    @Volatile var naWijziging: (() -> Unit)? = null

    /** De plek zoals hij nu is; verandert ook als een ander apparaat een plek kiest. */
    fun data(context: Context): StateFlow<Plek?> = stroom ?: synchronized(this) {
        stroom ?: MutableStateFlow(lees(context)).also { stroom = it }
    }

    fun lees(context: Context): Plek? {
        val p = context.getSharedPreferences(BESTAND, Context.MODE_PRIVATE)
        val naam = p.getString("plek_naam", null) ?: return null
        return Plek(
            naam = naam,
            lat = p.getString("plek_lat", null)?.toDoubleOrNull() ?: return null,
            lon = p.getString("plek_lon", null)?.toDoubleOrNull() ?: return null,
            uitleg = p.getString("plek_uitleg", "").orEmpty(),
        )
    }

    fun bewaar(context: Context, plek: Plek?) {
        zet(context, plek)
        naWijziging?.invoke()
    }

    private fun zet(context: Context, plek: Plek?) {
        context.getSharedPreferences(BESTAND, Context.MODE_PRIVATE).edit().apply {
            if (plek == null) {
                remove("plek_naam"); remove("plek_lat"); remove("plek_lon"); remove("plek_uitleg")
            } else {
                putString("plek_naam", plek.naam)
                putString("plek_lat", plek.lat.toString())
                putString("plek_lon", plek.lon.toString())
                putString("plek_uitleg", plek.uitleg)
            }
        }.apply()
        (data(context) as MutableStateFlow).value = plek
    }

    // ---------- Sync ----------

    fun alsTekst(context: Context): String = JSONObject().apply {
        lees(context)?.let { put("plek", JSONObject().put("naam", it.naam).put("lat", it.lat).put("lon", it.lon).put("uitleg", it.uitleg)) }
    }.toString()

    /** Plek van een ander apparaat (zonder [naWijziging]). */
    fun vervang(context: Context, tekst: String) {
        val o = JSONObject(tekst).optJSONObject("plek")
        val plek = o?.let { Plek(it.getString("naam"), it.getDouble("lat"), it.getDouble("lon"), it.optString("uitleg")) }
        zet(context, plek)
    }
}

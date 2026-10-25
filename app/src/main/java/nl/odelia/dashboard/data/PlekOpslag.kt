package nl.odelia.dashboard.data

import android.content.Context
import nl.odelia.dashboard.logica.Plek

/** Bewaart de zeilplek op de telefoon (niet in de code: die is openbaar). */
object PlekOpslag {
    private const val BESTAND = "instellingen"

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
        context.getSharedPreferences(BESTAND, Context.MODE_PRIVATE).edit().apply {
            if (plek == null) {
                clear()
            } else {
                putString("plek_naam", plek.naam)
                putString("plek_lat", plek.lat.toString())
                putString("plek_lon", plek.lon.toString())
                putString("plek_uitleg", plek.uitleg)
            }
        }.apply()
    }
}

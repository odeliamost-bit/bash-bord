package nl.odelia.dashboard.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Eén keer de plek van de telefoon vragen, voor de poort-check (bouwnacht 2). Geen constante locatie.
 * Liefst via netwerk (wifi/zendmast: zuinig), anders GPS. Geen Google-diensten nodig. Gaat nergens heen.
 */
object Locatie {
    fun mag(context: Context) = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Mag het ook als de app dicht is ("Altijd toestaan")? */
    fun magAltijd(context: Context) = mag(context) &&
        (Build.VERSION.SDK_INT < 29 || context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)

    @SuppressLint("MissingPermission")
    suspend fun eenKeer(context: Context): Location? {
        if (!mag(context)) return null
        val lm = context.getSystemService(LocationManager::class.java) ?: return null
        val bron = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER).firstOrNull { lm.isProviderEnabled(it) } ?: return null
        if (Build.VERSION.SDK_INT < 30) return lm.getLastKnownLocation(bron)
        return withTimeoutOrNull(30_000) {
            suspendCancellableCoroutine { c ->
                val stop = CancellationSignal()
                c.invokeOnCancellation { stop.cancel() }
                lm.getCurrentLocation(bron, stop, Executors.newSingleThreadExecutor()) { plek -> if (c.isActive) c.resume(plek) }
            }
        }
    }
}

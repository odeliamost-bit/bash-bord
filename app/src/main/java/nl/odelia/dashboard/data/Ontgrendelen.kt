package nl.odelia.dashboard.data

import android.app.KeyguardManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import nl.odelia.dashboard.logica.Wachtwoorden

/**
 * Ontgrendelen met wat de telefoon zelf heeft: vingerafdruk, patroon of pincode.
 * Alleen Android zelf (geen extra bibliotheek). De app ziet je vinger of pincode nooit,
 * hij hoort alleen "gelukt" of "niet gelukt".
 */
object Ontgrendelen {
    /** Heeft de telefoon een schermslot (pincode/patroon/wachtwoord)? Zonder dat kan het niet veilig. */
    fun kan(context: Context): Boolean =
        context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

    /**
     * Vraagt om te ontgrendelen. Op Android 10+ met de vingerafdruk-vraag van de telefoon
     * (met pincode/patroon als andere keuze). Op oudere telefoons: het gewone schermslot,
     * via [startOud] (een Intent die de Activity met een resultaat moet starten).
     */
    fun vraag(
        context: Context,
        gelukt: () -> Unit,
        mislukt: (String?) -> Unit,
        startOud: (Intent) -> Unit,
    ) {
        if (!kan(context)) {
            mislukt("Zet eerst een schermslot aan op je telefoon (pincode of patroon).")
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val km = context.getSystemService(KeyguardManager::class.java)
            @Suppress("DEPRECATION")
            val intent = km?.createConfirmDeviceCredentialIntent("Wachtwoorden", "Ontgrendel met je pincode of patroon")
            if (intent == null) mislukt("Ontgrendelen lukt niet op deze telefoon.") else startOud(intent)
            return
        }
        val bouwer = BiometricPrompt.Builder(context)
            .setTitle("Wachtwoorden openen")
            .setSubtitle("Vingerafdruk, patroon of pincode")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bouwer.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
        } else {
            @Suppress("DEPRECATION")
            bouwer.setDeviceCredentialAllowed(true)
        }
        val hoofd = Handler(Looper.getMainLooper())
        bouwer.build().authenticate(
            CancellationSignal(),
            { hoofd.post(it) },
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) = gelukt()
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    val zelfGestopt = errorCode == BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.BIOMETRIC_ERROR_CANCELED
                    mislukt(if (zelfGestopt) null else errString?.toString())
                }
                // onAuthenticationFailed: vinger niet herkend; de telefoon laat het zelf opnieuw proberen.
            },
        )
    }

    /**
     * Zet tekst op het klembord, gemarkeerd als gevoelig (dan laat Android 13+ hem niet zien
     * in het kopieer-berichtje). Na een minuut haalt de app hem weer weg, als hij er nog staat.
     */
    fun kopieerGeheim(context: Context, tekst: String): Boolean {
        val kb = context.getSystemService(ClipboardManager::class.java) ?: return false
        val clip = ClipData.newPlainText("Wachtwoord", tekst)
        clip.description.extras = PersistableBundle().apply {
            // Zelfde als ClipDescription.EXTRA_IS_SENSITIVE (Android 13+); oudere telefoons negeren het.
            putBoolean("android.content.extra.IS_SENSITIVE", true)
        }
        kb.setPrimaryClip(clip)
        Handler(Looper.getMainLooper()).postDelayed({
            val nu = try {
                kb.primaryClip?.getItemAt(0)?.text?.toString()
            } catch (_: Exception) {
                null
            }
            if (nu == tekst) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) kb.clearPrimaryClip() else kb.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }, Wachtwoorden.KLEMBORD_WEG.toMillis())
        return true
    }
}

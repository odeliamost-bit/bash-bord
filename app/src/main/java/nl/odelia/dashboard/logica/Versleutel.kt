package nl.odelia.dashboard.logica

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Versleutelen vóór de sync (bouwnacht 1, nov 2026).
 *
 * - Odelia kiest één **geheime zin** en typt die op elk apparaat één keer.
 * - Uit die zin + haar eigen Supabase-id maakt de app een sleutel (PBKDF2). De zin zelf wordt nergens bewaard.
 * - Alles gaat versleuteld (AES-256-GCM) naar Supabase. Supabase (en wie daar inbreekt) kan het niet lezen.
 * - Online tekst begint met [VOORVOEGSEL]. Oude, onversleutelde tekst wordt nog gelezen en daarna versleuteld teruggezet.
 */
object Versleutel {
    const val VOORVOEGSEL = "enc1:"
    private const val RONDES = 210_000
    private const val IV = 12
    private val toeval = SecureRandom()

    /** Minstens 8 tekens, anders is hij te makkelijk te raden. */
    fun zinOk(zin: String) = zin.trim().length >= 8

    /** De sleutel (32 bytes). Zelfde zin + zelfde account = zelfde sleutel op elk apparaat. */
    fun sleutel(zin: String, gebruiker: String): ByteArray {
        val spec = PBEKeySpec(zin.trim().toCharArray(), "bash-bord|$gebruiker".toByteArray(Charsets.UTF_8), RONDES, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun isDicht(inhoud: String) = inhoud.startsWith(VOORVOEGSEL)

    fun dicht(tekst: String, sleutel: ByteArray): String {
        val iv = ByteArray(IV).also(toeval::nextBytes)
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, SecretKeySpec(sleutel, "AES"), GCMParameterSpec(128, iv))
        val uit = c.doFinal(tekst.toByteArray(Charsets.UTF_8))
        return VOORVOEGSEL + Base64.getEncoder().encodeToString(iv + uit)
    }

    /** Terug naar gewone tekst. Verkeerde zin = [VerkeerdeZin]. Onversleutelde (oude) tekst komt ongewijzigd terug. */
    fun open(inhoud: String, sleutel: ByteArray): String {
        if (!isDicht(inhoud)) return inhoud
        return try {
            val alles = Base64.getDecoder().decode(inhoud.removePrefix(VOORVOEGSEL))
            val c = Cipher.getInstance("AES/GCM/NoPadding")
            c.init(Cipher.DECRYPT_MODE, SecretKeySpec(sleutel, "AES"), GCMParameterSpec(128, alles, 0, IV))
            String(c.doFinal(alles, IV, alles.size - IV), Charsets.UTF_8)
        } catch (e: Exception) {
            throw VerkeerdeZin()
        }
    }

    class VerkeerdeZin : Exception("Geheime zin klopt niet")
}

package nl.odelia.dashboard.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Bewaart de sync-sleutel op dit apparaat, zelf weer versleuteld met de Android-sleutelkluis (Keystore).
 * De geheime zin zelf wordt nergens bewaard. Het bestand staat in `noBackupFilesDir` (niet in back-ups).
 */
class SyncSleutel(map: File) {
    private val bestand = AtomicFile(File(map, "sync-sleutel.bin"))

    fun lees(): ByteArray? = try {
        val alles = bestand.readFully()
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, kluis(), GCMParameterSpec(128, alles, 0, 12))
        c.doFinal(alles, 12, alles.size - 12)
    } catch (_: Exception) {
        null
    }

    fun bewaar(sleutel: ByteArray) {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, kluis())
        val uit = c.iv + c.doFinal(sleutel)
        val s = bestand.startWrite()
        try {
            s.write(uit)
            bestand.finishWrite(s)
        } catch (e: Exception) {
            bestand.failWrite(s)
            throw e
        }
    }

    fun wis() = bestand.delete()

    private fun kluis(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return gen.generateKey()
    }

    private companion object {
        const val ALIAS = "bashbord-sync"
    }
}

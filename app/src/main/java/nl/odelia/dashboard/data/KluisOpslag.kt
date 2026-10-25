package nl.odelia.dashboard.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.odelia.dashboard.logica.WachtwoordenData
import nl.odelia.dashboard.logica.WachtwoordenJson

/**
 * Bewaart de wachtwoorden versleuteld in `wachtwoorden.json.enc` op de telefoon.
 * De sleutel (AES-GCM, 256 bit) zit in de Android Keystore: hij kan de telefoon niet uit,
 * ook niet via een back-up. Gaat nooit online.
 */
class KluisOpslag private constructor(context: Context) {
    private val bestand = AtomicFile(File(context.filesDir, "wachtwoorden.json.enc"))
    private val slot = Mutex()

    /** Ontsleutelt alles. Geen bestand = nog leeg. Gooit een fout als ontsleutelen niet lukt. */
    suspend fun lees(): WachtwoordenData = withContext(Dispatchers.IO) {
        slot.withLock {
            if (!bestand.baseFile.exists()) return@withLock WachtwoordenData()
            val alles = bestand.readFully()
            val ivLengte = alles[0].toInt()
            val iv = alles.copyOfRange(1, 1 + ivLengte)
            val cipher = Cipher.getInstance(VORM)
            cipher.init(Cipher.DECRYPT_MODE, sleutel(), GCMParameterSpec(128, iv))
            val tekst = String(cipher.doFinal(alles, 1 + ivLengte, alles.size - 1 - ivLengte), Charsets.UTF_8)
            WachtwoordenJson.lees(tekst)
        }
    }

    /** Versleutelt en bewaart. Elke keer een nieuwe IV (dat regelt de Keystore). */
    suspend fun bewaar(d: WachtwoordenData) = withContext(Dispatchers.IO) {
        slot.withLock {
            val cipher = Cipher.getInstance(VORM)
            cipher.init(Cipher.ENCRYPT_MODE, sleutel())
            val iv = cipher.iv
            val geheim = cipher.doFinal(WachtwoordenJson.schrijf(d).toByteArray(Charsets.UTF_8))
            val uit = bestand.startWrite()
            try {
                uit.write(byteArrayOf(iv.size.toByte()))
                uit.write(iv)
                uit.write(geheim)
                bestand.finishWrite(uit)
            } catch (e: Exception) {
                bestand.failWrite(uit)
                throw e
            }
        }
    }

    private fun sleutel(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val maker = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        maker.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return maker.generateKey()
    }

    companion object {
        private const val ALIAS = "wachtwoorden"
        private const val VORM = "AES/GCM/NoPadding"

        @Volatile private var instantie: KluisOpslag? = null

        fun van(context: Context): KluisOpslag = instantie ?: synchronized(this) {
            instantie ?: KluisOpslag(context.applicationContext).also { instantie = it }
        }
    }
}

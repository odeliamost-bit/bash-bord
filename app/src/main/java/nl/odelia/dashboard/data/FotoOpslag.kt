package nl.odelia.dashboard.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.AtomicFile
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

/**
 * Foto's bij Onthoud-items. De foto staat als JPEG in filesDir/fotos/,
 * en in fotos.json staat welk item (id) bij welk bestand hoort.
 * Los van de database, zodat die niet hoeft te veranderen. Gaat nooit online.
 */
class FotoOpslag private constructor(private val context: Context) {
    private val map = File(context.filesDir, "fotos").apply { mkdirs() }
    private val bestand = AtomicFile(File(context.filesDir, "fotos.json"))
    private val werk = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val slot = Mutex()
    private val _data = MutableStateFlow<Map<String, String>>(emptyMap())

    /** Onthoud-id → pad van de foto. */
    val data: StateFlow<Map<String, String>> = _data.asStateFlow()

    private val geladen = werk.async {
        val tekst = try {
            String(bestand.readFully(), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
        if (tekst != null) _data.value = lees(tekst)
    }

    /** Zet de foto bij een item (of haal hem weg met `null`). Een oude foto wordt opgeruimd. */
    fun zet(id: String, pad: String?) {
        werk.launch {
            geladen.await()
            slot.withLock {
                val oud = _data.value[id]
                if (oud == pad) return@withLock
                val nieuw = if (pad == null) _data.value - id else _data.value + (id to pad)
                _data.value = nieuw
                schrijf(nieuw)
                if (oud != null) verwijderBestand(oud)
            }
        }
    }

    /** Bewaar een camera-foto als JPEG. Geeft het pad, of null als het mislukt. */
    fun bewaarBitmap(beeld: Bitmap): String? = try {
        val uit = nieuwBestand()
        uit.outputStream().use { beeld.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        uit.absolutePath
    } catch (_: Exception) {
        null
    }

    /** Kopieer een gekozen foto naar de app. Te groot (meer dan 20 MB) = null. */
    fun bewaarUri(uri: Uri): String? = try {
        val uit = nieuwBestand()
        context.contentResolver.openInputStream(uri)?.use { inn ->
            uit.outputStream().use { o ->
                val buf = ByteArray(64 * 1024)
                var totaal = 0L
                while (true) {
                    val n = inn.read(buf)
                    if (n < 0) break
                    totaal += n
                    if (totaal > MAX_GROOTTE) throw IllegalStateException("te groot")
                    o.write(buf, 0, n)
                }
            }
        } ?: throw IllegalStateException("niet te openen")
        uit.absolutePath
    } catch (_: Exception) {
        null
    }

    /** Een foto die (nog) niet bij een bewaard item hoort, weggooien. */
    fun gooiWeg(pad: String) {
        if (pad !in _data.value.values) verwijderBestand(pad)
    }

    /** Klein plaatje om te laten zien (niet de hele foto in het geheugen). */
    fun laad(pad: String, maxZijde: Int = 400): Bitmap? = try {
        val maat = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(pad, maat)
        var stap = 1
        while (maat.outWidth / (stap * 2) >= maxZijde && maat.outHeight / (stap * 2) >= maxZijde) stap *= 2
        BitmapFactory.decodeFile(pad, BitmapFactory.Options().apply { inSampleSize = stap })
    } catch (_: Exception) {
        null
    }

    private fun nieuwBestand() = File(map, UUID.randomUUID().toString() + ".jpg")

    /** Alleen bestanden in onze eigen fotomap weggooien. */
    private fun verwijderBestand(pad: String) {
        val f = File(pad)
        if (f.parentFile?.canonicalPath == map.canonicalPath) f.delete()
    }

    private fun schrijf(d: Map<String, String>) {
        val uit = try {
            bestand.startWrite()
        } catch (_: Exception) {
            return
        }
        try {
            uit.write(JSONObject(d).toString().toByteArray(Charsets.UTF_8))
            bestand.finishWrite(uit)
        } catch (_: Exception) {
            bestand.failWrite(uit)
        }
    }

    private fun lees(tekst: String): Map<String, String> = try {
        val o = JSONObject(tekst)
        o.keys().asSequence().mapNotNull { k -> o.optString(k).takeIf { it.isNotBlank() }?.let { k to it } }.toMap()
    } catch (_: Exception) {
        emptyMap()
    }

    companion object {
        private const val MAX_GROOTTE = 20L * 1024 * 1024

        @Volatile private var instantie: FotoOpslag? = null

        fun van(context: Context): FotoOpslag = instantie ?: synchronized(this) {
            instantie ?: FotoOpslag(context.applicationContext).also { instantie = it }
        }
    }
}

package nl.odelia.dashboard.data

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.media.ExifInterface
import java.io.File
import java.util.UUID

/**
 * Foto's bij Onthoud. Ze staan alleen op de telefoon, in de eigen map van de app ("fotos").
 * Andere apps kunnen er niet bij. De camera-app krijgt alleen even het ene bestand om in te schrijven.
 */
object Fotos {
    private const val MAP = "fotos"

    /** Alleen namen die wij zelf maken (UUID + .jpg). Zo kan een naam nooit naar een andere map wijzen. */
    private val GOEDE_NAAM = Regex("[0-9a-f-]{36}\\.jpg")

    fun map(context: Context): File = File(context.filesDir, MAP).apply { mkdirs() }

    /** Een nieuw, leeg bestand waar de camera de foto in zet. */
    fun nieuw(context: Context): File = File(map(context), "${UUID.randomUUID()}.jpg")

    fun bestand(context: Context, naam: String): File? =
        if (GOEDE_NAAM.matches(naam)) File(map(context), naam) else null

    fun gooiWeg(context: Context, naam: String) {
        bestand(context, naam)?.delete()
    }

    /** De foto, verkleind tot ongeveer [maxZijde] pixels en rechtop gedraaid. `null` als hij er niet is. */
    fun plaatje(context: Context, naam: String, maxZijde: Int = 1080): ImageBitmap? {
        val bestand = bestand(context, naam)?.takeIf { it.exists() } ?: return null
        val maat = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(bestand.path, maat)
        var stap = 1
        while (maat.outWidth / (stap * 2) >= maxZijde || maat.outHeight / (stap * 2) >= maxZijde) stap *= 2
        val bitmap = BitmapFactory.decodeFile(bestand.path, BitmapFactory.Options().apply { inSampleSize = stap })
            ?: return null
        return rechtop(bitmap, bestand).asImageBitmap()
    }

    private fun rechtop(bitmap: Bitmap, bestand: File): Bitmap {
        val graden = when (
            ExifInterface(bestand.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val draai = Matrix().apply { postRotate(graden) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, draai, true)
    }
}

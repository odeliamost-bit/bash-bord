package androidx.compose.ui.res

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.File

/** Zelfde vorm als Android's painterResource(id), maar leest het plaatje uit app/src/main/res. */
@Composable
fun painterResource(id: Int): Painter = remember(id) {
    val pad = when (id) {
        1 -> "drawable-nodpi/meisje.png"
        else -> error("onbekend plaatje $id")
    }
    val bytes = File(System.getProperty("res"), pad).readBytes()
    BitmapPainter(org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap())
}

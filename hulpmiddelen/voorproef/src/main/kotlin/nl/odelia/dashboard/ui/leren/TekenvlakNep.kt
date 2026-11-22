package nl.odelia.dashboard.ui.leren

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.onderdelen.Tekst

/** Nep-versie voor de voorproef: de echte pagina draait alleen op Android (WebView). */
@Composable
fun Tekenvlak(modifier: Modifier = Modifier) {
    Box(modifier.background(Color(0xFFEEF1F4)), contentAlignment = Alignment.Center) {
        Tekst("Schakelbord-tekenvlak", 18.sp)
    }
}

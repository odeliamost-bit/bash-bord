package nl.odelia.dashboard.ui.leren

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Tekenen in de Simulator: de Schakelbord-simulator (eerdere versie op claude.ai),
 * nu ingebouwd in de app (`assets/schakelbord.html`). Rekent echt (knooppuntmethode).
 * Werkt helemaal zonder internet: netwerk staat uit voor deze pagina.
 * Tekeningen en ideeën blijven in de opslag van deze pagina, alleen op dit apparaat.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Tekenvlak(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.blockNetworkLoads = true
                settings.allowContentAccess = false
                webChromeClient = WebChromeClient()
                loadUrl("file:///android_asset/schakelbord.html")
            }
        },
    )
}

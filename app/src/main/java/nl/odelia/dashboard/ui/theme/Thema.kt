package nl.odelia.dashboard.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.R

/** Nunito, dik (zoals in het ontwerp). */
val Nunito = FontFamily(
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black),
)

@Composable
fun DashboardThema(inhoud: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Kleur.Koraal,
            background = Kleur.Creme,
            surface = Kleur.Kaart,
            onBackground = Kleur.Inkt,
            onSurface = Kleur.Inkt,
        ),
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Kleur.Inkt,
            ),
            content = inhoud,
        )
    }
}

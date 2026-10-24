package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role

/**
 * Tik-effect in klei-stijl: de knop wordt heel even iets kleiner.
 * Geen standaard Android-vlek (ripple).
 * Zet hem vooraan, dan krimpen schaduw en voetje mee.
 */
fun Modifier.tik(onClickLabel: String? = null, onClick: () -> Unit): Modifier = composed {
    val bron = remember { MutableInteractionSource() }
    indrukken(bron).clickable(bron, indication = null, onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
}

/** Zoals [tik], voor tabjes en menu-regels die aan of uit staan. */
fun Modifier.kies(aan: Boolean, onClick: () -> Unit): Modifier = composed {
    val bron = remember { MutableInteractionSource() }
    indrukken(bron).selectable(aan, bron, indication = null, role = Role.Tab, onClick = onClick)
}

private fun Modifier.indrukken(bron: InteractionSource): Modifier = composed {
    val schaal = remember { Animatable(1f) }
    LaunchedEffect(bron) {
        // Na elkaar afspelen: ook bij een heel korte tik zie je de knop even krimpen.
        bron.interactions.collect { actie ->
            when (actie) {
                is PressInteraction.Press -> schaal.animateTo(0.95f, tween(90))
                is PressInteraction.Release, is PressInteraction.Cancel -> schaal.animateTo(1f, tween(160))
            }
        }
    }
    graphicsLayer {
        scaleX = schaal.value
        scaleY = schaal.value
    }
}

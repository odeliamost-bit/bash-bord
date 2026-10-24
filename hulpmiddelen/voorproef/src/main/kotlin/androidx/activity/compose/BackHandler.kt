package androidx.activity.compose

import androidx.compose.runtime.Composable

/** Op de computer is er geen terug-knop. */
@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {}

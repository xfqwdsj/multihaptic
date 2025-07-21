package top.ltfan.multihaptic.compose

import androidx.compose.runtime.Composable
import top.ltfan.multihaptic.vibrator.Vibrator

/**
 * Remember a [Vibrator] instance for use in Compose.
 */
@Composable
expect fun rememberVibrator(): Vibrator

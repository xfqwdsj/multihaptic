package top.ltfan.multihaptic.compose

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import top.ltfan.multihaptic.getVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

@androidx.compose.runtime.Composable
actual fun rememberVibrator(): Vibrator {
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope) { getVibrator(coroutineScope, Unit) }
}

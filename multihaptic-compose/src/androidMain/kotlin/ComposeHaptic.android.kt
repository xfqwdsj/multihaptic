package top.ltfan.multihaptic.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import top.ltfan.multihaptic.getVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

@Composable
actual fun rememberVibrator(): Vibrator {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    return remember(coroutineScope, context) { getVibrator(coroutineScope, context) }
}

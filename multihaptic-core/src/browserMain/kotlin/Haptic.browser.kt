package top.ltfan.multihaptic

import kotlinx.coroutines.CoroutineScope
import top.ltfan.multihaptic.vibrator.BrowserVibrator
import top.ltfan.multihaptic.vibrator.Vibrator
import kotlin.time.Duration

actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator = BrowserVibrator(coroutineScope)

internal expect fun vibrate(duration: Duration)

internal expect fun isVibrateSupported(): Boolean

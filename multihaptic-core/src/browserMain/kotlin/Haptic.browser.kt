package top.ltfan.multihaptic

import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import top.ltfan.multihaptic.vibrator.BrowserVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

public actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator =
    BrowserVibrator(coroutineScope)

internal expect fun vibrate(duration: Duration)

internal expect fun isVibrationSupported(): Boolean

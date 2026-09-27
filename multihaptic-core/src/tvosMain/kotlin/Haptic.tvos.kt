package top.ltfan.multihaptic

import kotlinx.coroutines.CoroutineScope
import top.ltfan.multihaptic.platform.apple.corehaptics.getCoreHapticsVibrator
import top.ltfan.multihaptic.vibrator.StubVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

public actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator {
    return getCoreHapticsVibrator(coroutineScope) ?: StubVibrator()
}

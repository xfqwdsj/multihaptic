package top.ltfan.multihaptic

import kotlinx.coroutines.CoroutineScope
import platform.UIKit.UIView
import top.ltfan.multihaptic.platform.apple.corehaptics.getCoreHapticsVibrator
import top.ltfan.multihaptic.vibrator.UIFeedbackVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

public actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator {
    return getCoreHapticsVibrator(coroutineScope) ?: UIFeedbackVibrator(coroutineScope, config as? UIView)
}

package top.ltfan.multihaptic

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import platform.UIKit.UIDevice
import platform.UIKit.UIView
import top.ltfan.multihaptic.platform.corehaptics.getCoreHapticsVibrator
import top.ltfan.multihaptic.vibrator.StubVibrator
import top.ltfan.multihaptic.vibrator.UIFeedbackVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator {
    val version = UIDevice.currentDevice.systemVersion.split(".").map { it.toInt() }
    if (version.first() >= 13) getCoreHapticsVibrator(coroutineScope)?.let { return it }
    if (version.first() >= 10) return UIFeedbackVibrator(coroutineScope, config as? UIView)
    return StubVibrator()
}

package top.ltfan.multihaptic

import kotlinx.coroutines.CoroutineScope
import top.ltfan.multihaptic.platform.apple.corehaptics.getCoreHapticsVibrator
import top.ltfan.multihaptic.vibrator.HapticFeedbackVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

/**
 * Selects Core Haptics on compatible macOS hardware, with AppKit haptic
 * feedback for the remaining supported systems.
 *
 * @param coroutineScope The coroutine scope to use for vibration effects.
 * @param config Unused on macOS.
 * @return An instance of [Vibrator].
 */
public actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator {
    return getCoreHapticsVibrator(coroutineScope) ?: HapticFeedbackVibrator(coroutineScope)
}

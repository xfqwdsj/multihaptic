package top.ltfan.multihaptic

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import platform.Foundation.NSProcessInfo
import platform.Foundation.isMacCatalystApp
import top.ltfan.multihaptic.platform.corehaptics.getCoreHapticsVibrator
import top.ltfan.multihaptic.vibrator.HapticFeedbackVibrator
import top.ltfan.multihaptic.vibrator.StubVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

/**
 * Returns a [Vibrator] instance based on the macOS version.
 * - If the macOS version is 13.0 or later, it attempts to use Core
 *   Haptics.
 * - If the macOS version is 10.11 or later, it uses Haptic Feedback.
 * - Otherwise, it returns a stub vibrator.
 *
 * @param coroutineScope The coroutine scope to use for vibration effects.
 * @param config The configuration object, expected to be unused in this
 *   context.
 * @return An instance of [Vibrator].
 */
@ExperimentalForeignApi
@BetaInteropApi
actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator {
    val processInfo = NSProcessInfo.processInfo
    val version = processInfo.operatingSystemVersion
    val majorVersion = version.useContents { majorVersion }.toInt()
    val minorVersion = version.useContents { minorVersion }.toInt()

    if (majorVersion >= 13 && processInfo.isMacCatalystApp()) getCoreHapticsVibrator(coroutineScope)?.let { return it }
    if (majorVersion >= 10 && minorVersion >= 11) return HapticFeedbackVibrator(coroutineScope)
    return StubVibrator()
}

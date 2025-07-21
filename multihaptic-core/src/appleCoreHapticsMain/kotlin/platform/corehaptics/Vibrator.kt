package top.ltfan.multihaptic.platform.corehaptics

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import platform.CoreHaptics.CHHapticEngine
import platform.apple.dsl.AppleError
import platform.apple.dsl.runThrowing
import top.ltfan.multihaptic.vibrator.CoreHapticsVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

/**
 * Returns a [CoreHapticsVibrator] instance or null if the device does not
 * support haptics.
 *
 * Warning: This function does not check system version and may cause
 * crashes on older systems.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun getCoreHapticsVibrator(coroutineScope: CoroutineScope): Vibrator? {
    if (!CHHapticEngine.Companion.capabilitiesForHardware().supportsHaptics) return null

    val engine = try {
        runThrowing { CHHapticEngine(it) }
    } catch (e: AppleError) {
        e.printNSErrorInfo()
        return null
    }

    fun startEngine() {
        try {
            runThrowing { engine.startAndReturnError(it) }
        } catch (e: AppleError) {
            e.printNSErrorInfo()
        }
    }

    engine.resetHandler = ::startEngine
    startEngine()

    return CoreHapticsVibrator(coroutineScope, engine)
}

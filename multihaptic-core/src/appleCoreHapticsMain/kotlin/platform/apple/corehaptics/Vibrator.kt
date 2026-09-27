package top.ltfan.multihaptic.platform.apple.corehaptics

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import platform.CoreHaptics.CHHapticEngine
import top.ltfan.multihaptic.platform.apple.AppleError
import top.ltfan.multihaptic.platform.apple.runThrowing
import top.ltfan.multihaptic.vibrator.CoreHapticsVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

/**
 * Creates a [CoreHapticsVibrator] when the hardware supports haptics and
 * the engine initializes successfully; otherwise returns null.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
public fun getCoreHapticsVibrator(coroutineScope: CoroutineScope): Vibrator? {
    if (!CHHapticEngine.capabilitiesForHardware().supportsHaptics) return null

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

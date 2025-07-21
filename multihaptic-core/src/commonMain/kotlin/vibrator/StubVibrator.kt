package top.ltfan.multihaptic.vibrator

import top.ltfan.multihaptic.HapticEffect

/**
 * A stub implementation of [Vibrator] that does nothing.
 */
open class StubVibrator internal constructor() : Vibrator {
    override fun vibrate(effect: HapticEffect) {}
    override fun cancel() {}
}

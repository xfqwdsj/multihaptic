package top.ltfan.multihaptic.vibrator

import kotlinx.coroutines.CoroutineScope
import top.ltfan.multihaptic.HapticEffect
import top.ltfan.multihaptic.isVibrateSupported
import top.ltfan.multihaptic.performByDuration
import top.ltfan.multihaptic.unpack
import top.ltfan.multihaptic.vibrate
import kotlin.time.Duration

class BrowserVibrator internal constructor(coroutineScope: CoroutineScope) : AbstractVibrator(coroutineScope) {
    override suspend fun perform(effect: HapticEffect) {
        effect.unpack { basic ->
            basic.performByDuration {
                vibrate(it)
            }
        }
    }

    override fun cancel() {
        vibrate(Duration.Companion.ZERO)
    }

    override fun isVibrationSupported(): Boolean {
        return isVibrateSupported()
    }
}

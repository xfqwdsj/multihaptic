package top.ltfan.multihaptic.vibrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import top.ltfan.multihaptic.HapticEffect

abstract class AbstractVibrator internal constructor(coroutineScope: CoroutineScope) : StubVibrator() {
    private val effectChannel = Channel<HapticEffect>(Channel.CONFLATED)

    init {
        coroutineScope.launch {
            for (effect in effectChannel) {
                perform(effect)
            }
        }
    }

    protected abstract suspend fun perform(effect: HapticEffect)

    override fun vibrate(effect: HapticEffect) {
        effectChannel.trySend(effect)
    }
}

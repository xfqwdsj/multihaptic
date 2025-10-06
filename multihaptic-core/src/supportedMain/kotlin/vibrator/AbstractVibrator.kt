package top.ltfan.multihaptic.vibrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import top.ltfan.multihaptic.HapticEffect

abstract class AbstractVibrator internal constructor(coroutineScope: CoroutineScope) : StubVibrator() {
    protected val nextEffect = MutableSharedFlow<HapticEffect>(replay = 0, extraBufferCapacity = 1)

    init {
        coroutineScope.launch {
            nextEffect.collect { effect ->
                perform(effect)
            }
        }
    }

    protected abstract suspend fun perform(effect: HapticEffect)

    override fun vibrate(effect: HapticEffect) {
        nextEffect.tryEmit(effect)
    }
}

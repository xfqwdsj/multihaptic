package top.ltfan.multihaptic.vibrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import top.ltfan.multihaptic.HapticEffect

abstract class AbstractVibrator internal constructor(coroutineScope: CoroutineScope) : StubVibrator() {
    protected val nextEffect = MutableStateFlow<HapticEffect?>(null)

    init {
        coroutineScope.launch {
            nextEffect.collect {
                it?.let { effect ->
                    perform(effect)
                }
            }
        }
    }

    protected abstract suspend fun perform(effect: HapticEffect)

    override fun vibrate(effect: HapticEffect) {
        nextEffect.value = effect
    }
}

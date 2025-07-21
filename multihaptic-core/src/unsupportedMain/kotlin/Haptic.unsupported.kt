package top.ltfan.multihaptic

import kotlinx.coroutines.CoroutineScope
import top.ltfan.multihaptic.vibrator.StubVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator = StubVibrator()

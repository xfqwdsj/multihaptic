package top.ltfan.multihaptic

import kotlinx.coroutines.CoroutineScope
import top.ltfan.multihaptic.vibrator.Vibrator
import top.ltfan.multihaptic.vibrator.WatchOSVibrator

public actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator = WatchOSVibrator(coroutineScope)

package top.ltfan.multihaptic

import android.content.Context
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import top.ltfan.multihaptic.vibrator.AndroidVibrator
import top.ltfan.multihaptic.vibrator.StubVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

/**
 * Creates an Android vibrator from the [Context] in [config], or returns a
 * [StubVibrator] when a context or system vibrator is unavailable.
 *
 * @param coroutineScope The coroutine scope to use for vibration effects.
 * @param config The Android [Context].
 * @return An instance of [Vibrator].
 */
public actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator =
    (config as? Context)?.getSystemService<android.os.Vibrator>()?.let {
        AndroidVibrator(it, coroutineScope)
    } ?: StubVibrator()

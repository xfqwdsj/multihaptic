package top.ltfan.multihaptic

import android.content.Context
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import top.ltfan.multihaptic.vibrator.AndroidVibrator
import top.ltfan.multihaptic.vibrator.StubVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

/**
 * Returns a [Vibrator] instance based on the provided configuration. If
 * the configuration is not a [Context], a [StubVibrator] is returned.
 *
 * You may need to store a [Context] instance from your application or
 * activity and create your own wrapper around this function to use it
 * effectively.
 *
 * @param coroutineScope The coroutine scope to use for vibration effects.
 * @param config The configuration object, expected to be a [Context].
 * @return An instance of [Vibrator].
 */
actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator =
    (config as? Context)?.getSystemService<android.os.Vibrator>()?.let {
        AndroidVibrator(it, coroutineScope)
    } ?: StubVibrator()

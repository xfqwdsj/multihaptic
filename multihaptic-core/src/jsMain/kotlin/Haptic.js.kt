package top.ltfan.multihaptic

import kotlinx.browser.window
import kotlin.time.Duration

internal actual fun vibrate(duration: Duration) {
    if (isVibrationSupported()) {
        window.navigator.vibrate(duration.inWholeMilliseconds.toInt())
    }
}

internal actual fun isVibrationSupported(): Boolean {
    return js("typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function'") as Boolean
}

package top.ltfan.multihaptic

import kotlin.time.Duration
import kotlinx.browser.window

internal actual fun vibrate(duration: Duration) {
    if (isVibrationSupported()) {
        window.navigator.vibrate(duration.inWholeMilliseconds.toInt())
    }
}

internal actual fun isVibrationSupported(): Boolean {
    return js("typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function'")
        as Boolean
}

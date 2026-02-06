package top.ltfan.multihaptic

import kotlinx.browser.window
import kotlin.time.Duration

internal actual fun vibrate(duration: Duration) {
    if (isVibrateSupported()) {
        window.navigator.vibrate(duration.inWholeMilliseconds.toInt())
    }
}

internal actual fun isVibrateSupported(): Boolean {
    return js("typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function'") as Boolean
}

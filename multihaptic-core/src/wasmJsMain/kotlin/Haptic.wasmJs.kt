package top.ltfan.multihaptic

import kotlinx.browser.window
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.time.Duration

internal actual fun vibrate(duration: Duration) {
    if (isVibrateSupported()) {
        window.navigator.vibrate(duration.inWholeMilliseconds.toInt())
    }
}

internal actual fun isVibrateSupported(): Boolean = checkVibrateSupported()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function'")
private external fun checkVibrateSupported(): Boolean

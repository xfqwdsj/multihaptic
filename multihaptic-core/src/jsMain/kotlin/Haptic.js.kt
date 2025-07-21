package top.ltfan.multihaptic

import kotlinx.browser.window
import kotlin.time.Duration

internal actual fun vibrate(duration: Duration) {
    window.navigator.vibrate(duration.inWholeMilliseconds.toInt())
}

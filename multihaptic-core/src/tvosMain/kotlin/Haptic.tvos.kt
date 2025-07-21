package top.ltfan.multihaptic

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import platform.Foundation.NSProcessInfo
import top.ltfan.multihaptic.platform.corehaptics.getCoreHapticsVibrator
import top.ltfan.multihaptic.vibrator.StubVibrator
import top.ltfan.multihaptic.vibrator.Vibrator

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun getVibrator(coroutineScope: CoroutineScope, config: Any?): Vibrator {
    val processInfo = NSProcessInfo.processInfo
    val version = processInfo.operatingSystemVersion
    val majorVersion = version.useContents { majorVersion }.toInt()

    if (majorVersion >= 13) getCoreHapticsVibrator(coroutineScope)?.let { return it }
    return StubVibrator()
}

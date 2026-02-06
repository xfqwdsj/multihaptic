package top.ltfan.multihaptic.vibrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import platform.WatchKit.WKHapticType
import platform.WatchKit.WKInterfaceDevice
import top.ltfan.multihaptic.BasicPrimitive
import top.ltfan.multihaptic.HapticEffect
import top.ltfan.multihaptic.PrimitiveType
import top.ltfan.multihaptic.unpack

class WatchOSVibrator internal constructor(coroutineScope: CoroutineScope) : AbstractVibrator(coroutineScope) {
    private val device = WKInterfaceDevice.currentDevice()

    override val isVibrationSupported: Boolean = true

    override suspend fun perform(effect: HapticEffect) {
        effect.unpack { it.perform() }
    }

    private suspend fun BasicPrimitive.perform() {
        when (this) {
            is BasicPrimitive.Predefined -> type.perform()
            is BasicPrimitive.Custom -> fallback?.perform() ?: delay(duration)
        }
    }

    private fun PrimitiveType.perform() {
        device.playHaptic(hapticType)
    }

    private val PrimitiveType.hapticType
        get() = when (this) {
            PrimitiveType.Click -> WKHapticType.WKHapticTypeClick
            PrimitiveType.Thud -> WKHapticType.WKHapticTypeNotification
            PrimitiveType.Spin -> WKHapticType.WKHapticTypeNotification
            PrimitiveType.QuickRise -> WKHapticType.WKHapticTypeDirectionUp
            PrimitiveType.SlowRise -> WKHapticType.WKHapticTypeDirectionUp
            PrimitiveType.QuickFall -> WKHapticType.WKHapticTypeDirectionDown
            PrimitiveType.Tick -> WKHapticType.WKHapticTypeClick
            PrimitiveType.LowTick -> WKHapticType.WKHapticTypeClick
        }
}

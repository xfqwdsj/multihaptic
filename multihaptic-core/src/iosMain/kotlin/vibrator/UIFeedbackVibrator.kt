package top.ltfan.multihaptic.vibrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UIView
import top.ltfan.multihaptic.BasicPrimitive
import top.ltfan.multihaptic.HapticEffect
import top.ltfan.multihaptic.PrimitiveType
import top.ltfan.multihaptic.unpack

class UIFeedbackVibrator internal constructor(coroutineScope: CoroutineScope, private val uiView: UIView?) :
    AbstractVibrator(coroutineScope) {

    override fun isVibrationSupported(): Boolean = true

    override suspend fun perform(effect: HapticEffect) {
        effect.unpack { it.feedback() }
    }

    /**
     * Reserved for future use. The current Kotlin binding does not include
     * a constructor with a [UIView] parameter, and the currently used
     * constructor has been marked as deprecated.
     *
     * After the Kotlin binding is updated, the implementation will need to be
     * replaced.
     */
    private fun getImpactFeedbackGenerator(style: UIImpactFeedbackStyle) = UIImpactFeedbackGenerator(style)

    private suspend fun BasicPrimitive.feedback() {
        when (this) {
            is BasicPrimitive.Predefined -> type.feedback()
            is BasicPrimitive.Custom -> fallback?.feedback() ?: delay(duration)
        }
    }

    private fun PrimitiveType.feedback() {
        when (this) {
            PrimitiveType.Click -> getImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
            PrimitiveType.Thud -> getImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy).impactOccurred()
            PrimitiveType.Spin -> getImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy).impactOccurred()
            PrimitiveType.QuickRise -> getImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleRigid).impactOccurred()
            PrimitiveType.SlowRise -> getImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy).impactOccurred()
            PrimitiveType.QuickFall -> getImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleSoft).impactOccurred()
            PrimitiveType.Tick -> getImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
            PrimitiveType.LowTick -> getImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
        }
    }
}

package top.ltfan.multihaptic.vibrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import platform.AppKit.NSHapticFeedbackManager
import platform.AppKit.NSHapticFeedbackPatternAlignment
import platform.AppKit.NSHapticFeedbackPatternGeneric
import platform.AppKit.NSHapticFeedbackPatternLevelChange
import platform.AppKit.NSHapticFeedbackPerformanceTimeNow
import top.ltfan.multihaptic.BasicPrimitive
import top.ltfan.multihaptic.HapticEffect
import top.ltfan.multihaptic.PrimitiveType
import top.ltfan.multihaptic.unpack

class HapticFeedbackVibrator internal constructor(coroutineScope: CoroutineScope) : AbstractVibrator(coroutineScope) {
    private val feedbackPerformer = NSHapticFeedbackManager.defaultPerformer

    override val isVibrationSupported = true

    override suspend fun perform(effect: HapticEffect) {
        effect.unpack { it.feedback() }
    }

    private suspend fun BasicPrimitive.feedback() {
        when (this) {
            is BasicPrimitive.Predefined -> type.feedback()
            is BasicPrimitive.Custom -> fallback?.feedback() ?: delay(duration)
        }
    }

    private fun PrimitiveType.feedback() {
        feedbackPerformer.performFeedbackPattern(pattern, NSHapticFeedbackPerformanceTimeNow)
    }

    private val PrimitiveType.pattern
        get() = when (this) {
            PrimitiveType.Click -> NSHapticFeedbackPatternGeneric
            PrimitiveType.Thud -> NSHapticFeedbackPatternLevelChange
            PrimitiveType.Spin -> NSHapticFeedbackPatternLevelChange
            PrimitiveType.QuickRise -> NSHapticFeedbackPatternLevelChange
            PrimitiveType.SlowRise -> NSHapticFeedbackPatternLevelChange
            PrimitiveType.QuickFall -> NSHapticFeedbackPatternLevelChange
            PrimitiveType.Tick -> NSHapticFeedbackPatternAlignment
            PrimitiveType.LowTick -> NSHapticFeedbackPatternAlignment
        }
}

package top.ltfan.multihaptic.platform.corehaptics

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreHaptics.dsl.HapticEvent
import platform.CoreHaptics.dsl.HapticParameterCurve
import platform.CoreHaptics.dsl.HapticPatternBuilder

@ExperimentalForeignApi
@BetaInteropApi
inline fun CHHapticPattern(scale: Float = 1f, block: HapticPatternBuilder.() -> Unit) =
    ScalableHapticPatternBuilder(scale).apply(block).build()

class ScalableHapticPatternBuilder(private val factor: Float) : HapticPatternBuilder() {
    override fun preBuild() {
        events = events.map { event ->
            when (event) {
                is HapticEvent.Transient -> event.copy(event.hapticIntensity?.let { it * factor })
                is HapticEvent.Continuous -> event.copy(event.hapticIntensity?.let { it * factor })
            }
        }
        curves = curves.map { curve ->
            when (curve) {
                is HapticParameterCurve.Intensity -> curve.copy(controlPoints = curve.controlPoints.map { it.copy(value = it.value * factor) })
                else -> curve
            }
        }
    }
}

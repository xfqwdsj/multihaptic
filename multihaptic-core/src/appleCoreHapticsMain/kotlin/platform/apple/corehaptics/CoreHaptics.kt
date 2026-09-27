package top.ltfan.multihaptic.platform.apple.corehaptics

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreHaptics.CHHapticPattern
import top.ltfan.multihaptic.platform.apple.corehaptics.dsl.ContinuousEvent
import top.ltfan.multihaptic.platform.apple.corehaptics.dsl.CurveKind
import top.ltfan.multihaptic.platform.apple.corehaptics.dsl.HapticPattern
import top.ltfan.multihaptic.platform.apple.corehaptics.dsl.HapticPatternDsl
import top.ltfan.multihaptic.platform.apple.corehaptics.dsl.TransientEvent
import top.ltfan.multihaptic.platform.apple.corehaptics.dsl.buildHapticPattern
import top.ltfan.multihaptic.platform.apple.corehaptics.dsl.toNative

public fun HapticPattern.scaled(factor: Float): HapticPattern = copy(
    events = events.map { event ->
        when (event) {
            is TransientEvent -> event.copy(hapticIntensity = event.hapticIntensity?.times(factor))
            is ContinuousEvent -> event.copy(hapticIntensity = event.hapticIntensity?.times(factor))
        }
    },
    curves = curves.map { curve ->
        if (curve.kind == CurveKind.Intensity) {
            curve.copy(controlPoints = curve.controlPoints.map { it.copy(value = it.value * factor) })
        } else curve
    },
)

@ExperimentalForeignApi
@BetaInteropApi
public inline fun CHHapticPattern(scale: Float = 1f, block: HapticPatternDsl.() -> Unit): CHHapticPattern =
    buildHapticPattern(block = block).scaled(scale).toNative()

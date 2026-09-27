package top.ltfan.multihaptic.platform.apple.corehaptics.dsl

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreHaptics.CHHapticDynamicParameterIDHapticAttackTimeControl
import platform.CoreHaptics.CHHapticDynamicParameterIDHapticDecayTimeControl
import platform.CoreHaptics.CHHapticDynamicParameterIDHapticIntensityControl
import platform.CoreHaptics.CHHapticDynamicParameterIDHapticReleaseTimeControl
import platform.CoreHaptics.CHHapticDynamicParameterIDHapticSharpnessControl
import platform.CoreHaptics.CHHapticEvent
import platform.CoreHaptics.CHHapticEventParameter
import platform.CoreHaptics.CHHapticEventParameterIDAttackTime
import platform.CoreHaptics.CHHapticEventParameterIDDecayTime
import platform.CoreHaptics.CHHapticEventParameterIDHapticIntensity
import platform.CoreHaptics.CHHapticEventParameterIDHapticSharpness
import platform.CoreHaptics.CHHapticEventParameterIDReleaseTime
import platform.CoreHaptics.CHHapticEventParameterIDSustained
import platform.CoreHaptics.CHHapticEventTypeHapticContinuous
import platform.CoreHaptics.CHHapticEventTypeHapticTransient
import platform.CoreHaptics.CHHapticParameterCurve
import platform.CoreHaptics.CHHapticParameterCurveControlPoint
import platform.CoreHaptics.CHHapticPattern
import top.ltfan.multihaptic.platform.apple.runThrowing
import kotlin.time.Duration

public fun HapticEvent.toNative(): CHHapticEvent {
    val parameters = buildList {
        hapticIntensity?.let { add(CHHapticEventParameter(CHHapticEventParameterIDHapticIntensity, it)) }
        hapticSharpness?.let { add(CHHapticEventParameter(CHHapticEventParameterIDHapticSharpness, it)) }
        if (this@toNative is ContinuousEvent) {
            attackTime?.let { add(CHHapticEventParameter(CHHapticEventParameterIDAttackTime, it.seconds.toFloat())) }
            decayTime?.let { add(CHHapticEventParameter(CHHapticEventParameterIDDecayTime, it.seconds.toFloat())) }
            add(CHHapticEventParameter(CHHapticEventParameterIDReleaseTime, releaseTime.seconds.toFloat()))
            add(CHHapticEventParameter(CHHapticEventParameterIDSustained, if (sustained) 1f else 0f))
        }
    }
    return when (this) {
        is TransientEvent -> CHHapticEvent(
            eventType = CHHapticEventTypeHapticTransient,
            parameters = parameters,
            relativeTime = relativeTime.seconds,
        )

        is ContinuousEvent -> CHHapticEvent(
            eventType = CHHapticEventTypeHapticContinuous,
            parameters = parameters,
            relativeTime = relativeTime.seconds,
            duration = duration.seconds,
        )
    }
}

public fun HapticParameterCurve.toNative(): CHHapticParameterCurve {
    val parameterId = when (kind) {
        CurveKind.Intensity -> CHHapticDynamicParameterIDHapticIntensityControl
        CurveKind.Sharpness -> CHHapticDynamicParameterIDHapticSharpnessControl
        CurveKind.AttackTime -> CHHapticDynamicParameterIDHapticAttackTimeControl
        CurveKind.DecayTime -> CHHapticDynamicParameterIDHapticDecayTimeControl
        CurveKind.ReleaseTime -> CHHapticDynamicParameterIDHapticReleaseTimeControl
    }
    return CHHapticParameterCurve(
        parameterID = parameterId,
        controlPoints = controlPoints.map { point ->
            CHHapticParameterCurveControlPoint(point.relativeTime.seconds, point.value)
        },
        relativeTime = relativeTime.seconds,
    )
}

@ExperimentalForeignApi
@BetaInteropApi
public fun HapticPattern.toNative(): CHHapticPattern = runThrowing { error ->
    CHHapticPattern(
        events = events.map(HapticEvent::toNative),
        parameterCurves = curves.map(HapticParameterCurve::toNative),
        error = error,
    )
}

@ExperimentalForeignApi
@BetaInteropApi
public fun CHHapticPattern(block: HapticPatternDsl.() -> Unit): CHHapticPattern =
    buildHapticPattern(block = block).toNative()

private val Duration.seconds: Double get() = inWholeMilliseconds.toDouble() / 1000

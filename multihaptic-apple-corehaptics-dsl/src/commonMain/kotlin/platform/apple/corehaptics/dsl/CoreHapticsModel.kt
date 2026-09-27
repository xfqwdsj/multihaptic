package top.ltfan.multihaptic.platform.apple.corehaptics.dsl

import top.ltfan.dslutilities.DslBuildHook
import top.ltfan.dslutilities.DslBuilder
import top.ltfan.dslutilities.DslInitialProvider
import top.ltfan.dslutilities.DslList
import top.ltfan.dslutilities.DslListHook
import top.ltfan.dslutilities.DslListScope
import top.ltfan.dslutilities.DslValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

public val DefaultDuration: Duration = 50.milliseconds

public sealed interface HapticEvent {
    public val hapticIntensity: Float?
    public val hapticSharpness: Float?
    public val relativeTime: Duration
}

@DslBuilder(resultName = "TransientEvent", supertype = HapticEvent::class)
public interface TransientEventDsl {
    @DslValue
    public var hapticIntensity: Float?

    @DslValue
    public var hapticSharpness: Float?

    @DslValue(provider = ZeroDuration::class)
    public var relativeTime: Duration
}

@DslBuilder(resultName = "ContinuousEvent", supertype = HapticEvent::class)
public interface ContinuousEventDsl : TransientEventDsl {
    @DslValue(provider = DefaultEventDuration::class)
    public var duration: Duration

    @DslValue
    public var attackTime: Duration?

    @DslValue
    public var decayTime: Duration?

    @DslValue(provider = ZeroDuration::class)
    public var releaseTime: Duration

    @DslValue(initial = "false")
    public var sustained: Boolean
}

public object ZeroDuration : DslInitialProvider<Duration> {
    override fun provide(): Duration = Duration.ZERO
}

public object DefaultEventDuration : DslInitialProvider<Duration> {
    override fun provide(): Duration = DefaultDuration
}

public enum class CurveKind { Intensity, Sharpness, AttackTime, DecayTime, ReleaseTime }

@DslBuilder(resultName = "CurveControlPoint", requireConfiguration = true)
public interface CurveControlPointDsl {
    @DslValue(required = true)
    public var relativeTime: Duration

    @DslValue(required = true)
    public var value: Float
}

@DslBuilder(resultName = "HapticParameterCurve", buildHook = ValidateCurve::class, requireConfiguration = true)
public interface HapticParameterCurveDsl {
    public val kind: CurveKind

    @DslValue(provider = ZeroDuration::class)
    public var relativeTime: Duration

    @DslList(scopeName = "points", hook = CurvePointHook::class, children = [CurveControlPointDsl::class])
    public var controlPoints: MutableList<CurveControlPoint>
}

public object CurvePointHook : DslListHook<CurveControlPoint> {
    override fun beforeSet(element: CurveControlPoint) {
        require(element.relativeTime >= Duration.ZERO) { "Control point time must be non-negative." }
    }
}

public object ValidateCurve : DslBuildHook<HapticParameterCurveDsl> {
    override fun beforeBuild(scope: HapticParameterCurveDsl) {
        require(scope.relativeTime >= Duration.ZERO) { "Curve time must be non-negative." }
        require(scope.controlPoints.isNotEmpty()) { "A parameter curve needs a control point." }
        val validRange = if (scope.kind == CurveKind.Intensity) 0f..1f else -1f..1f
        scope.controlPoints.forEach { point ->
            require(point.value in validRange) {
                "${scope.kind} control point must be in $validRange."
            }
        }
    }
}

@DslBuilder(resultName = "HapticPattern", buildHook = ValidatePattern::class)
public interface HapticPatternDsl {
    @DslList(
        scopeName = "events",
        hook = EventHook::class,
        children = [TransientEventDsl::class, ContinuousEventDsl::class],
    )
    public var events: MutableList<HapticEvent>

    @DslList(scopeName = "curves", children = [HapticParameterCurveDsl::class])
    public var curves: MutableList<HapticParameterCurve>
}

public object EventHook : DslListHook<HapticEvent> {
    override fun beforeSet(element: HapticEvent) {
        require(element.relativeTime >= Duration.ZERO) { "Event time must be non-negative." }
        require(element.hapticIntensity?.let { it in 0f..1f } ?: true) {
            "Event intensity must be between 0 and 1."
        }
        require(element.hapticSharpness?.let { it in 0f..1f } ?: true) {
            "Event sharpness must be between 0 and 1."
        }
        if (element is ContinuousEvent) {
            require(element.duration > Duration.ZERO && element.duration <= 30.seconds) {
                "Continuous event duration must be between 0 and 30 seconds."
            }
        }
    }
}

public object ValidatePattern : DslBuildHook<HapticPatternDsl> {
    override fun beforeBuild(scope: HapticPatternDsl) {
        check(scope.events.isNotEmpty()) { "At least one event must be defined." }
    }
}

public fun HapticPatternDslBuilderEventsScope.add(event: HapticEvent) {
    elements += event
}

public val HapticPatternDslBuilderEventsScope.transient: Unit
    get() {
        transient()
    }

public fun HapticPatternDslBuilderEventsScope.transient(
    relativeTime: Duration = Duration.ZERO,
    block: TransientEventDsl.() -> Unit = {},
) {
    val builder = TransientEventDslBuilder()
    builder.relativeTime = relativeTime
    builder.block()
    elements += builder.build()
}

public val HapticPatternDslBuilderEventsScope.continuous: Unit
    get() {
        continuous()
    }

public fun HapticPatternDslBuilderEventsScope.continuous(
    duration: Duration = DefaultDuration,
    relativeTime: Duration = Duration.ZERO,
    block: ContinuousEventDsl.() -> Unit = {},
) {
    val builder = ContinuousEventDslBuilder()
    builder.duration = duration
    builder.relativeTime = relativeTime
    builder.block()
    elements += builder.build()
}

public var TransientEventDsl.intensity: Float?
    get() = hapticIntensity
    set(value) {
        hapticIntensity = value
    }

public var TransientEventDsl.sharpness: Float?
    get() = hapticSharpness
    set(value) {
        hapticSharpness = value
    }

public fun HapticPatternDslBuilderCurvesScope.add(curve: HapticParameterCurve) {
    elements += curve
}

private fun HapticPatternDslBuilderCurvesScope.curve(
    kind: CurveKind,
    relativeTime: Duration,
    block: DslListScope<CurveControlPoint>.() -> Unit,
) {
    val builder = HapticParameterCurveDslBuilder(kind)
    builder.relativeTime = relativeTime
    builder.points { block(this) }
    elements += builder.build()
}

public fun HapticPatternDslBuilderCurvesScope.intensity(
    relativeTime: Duration = Duration.ZERO,
    block: DslListScope<CurveControlPoint>.() -> Unit,
): Unit = curve(CurveKind.Intensity, relativeTime, block)

public fun HapticPatternDslBuilderCurvesScope.sharpness(
    relativeTime: Duration = Duration.ZERO,
    block: DslListScope<CurveControlPoint>.() -> Unit,
): Unit = curve(CurveKind.Sharpness, relativeTime, block)

public fun HapticPatternDslBuilderCurvesScope.attackTime(
    relativeTime: Duration = Duration.ZERO,
    block: DslListScope<CurveControlPoint>.() -> Unit,
): Unit = curve(CurveKind.AttackTime, relativeTime, block)

public fun HapticPatternDslBuilderCurvesScope.decayTime(
    relativeTime: Duration = Duration.ZERO,
    block: DslListScope<CurveControlPoint>.() -> Unit,
): Unit = curve(CurveKind.DecayTime, relativeTime, block)

public fun HapticPatternDslBuilderCurvesScope.releaseTime(
    relativeTime: Duration = Duration.ZERO,
    block: DslListScope<CurveControlPoint>.() -> Unit,
): Unit = curve(CurveKind.ReleaseTime, relativeTime, block)

public fun DslListScope<CurveControlPoint>.add(point: CurveControlPoint) {
    elements += point
}

public fun DslListScope<CurveControlPoint>.add(relativeTime: Duration, value: Float) {
    elements += CurveControlPoint(relativeTime, value)
}

context(scope: DslListScope<CurveControlPoint>)
public infix fun Float.at(relativeTime: Duration) {
    scope.elements += CurveControlPoint(relativeTime, this)
}

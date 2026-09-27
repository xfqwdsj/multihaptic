package top.ltfan.multihaptic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import top.ltfan.dslutilities.DslBuildHook
import top.ltfan.dslutilities.DslBuilder
import top.ltfan.dslutilities.DslInitialProvider
import top.ltfan.dslutilities.DslList
import top.ltfan.dslutilities.DslListHook
import top.ltfan.dslutilities.DslListScope
import top.ltfan.dslutilities.DslValue
import top.ltfan.dslutilities.DslValueHook
import top.ltfan.multihaptic.vibrator.Vibrator
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public val DefaultDuration: Duration = 50.milliseconds

public sealed class BasicPrimitive {
    public abstract val scale: Float

    public data class Predefined(val type: PrimitiveType, override val scale: Float = 1f) : BasicPrimitive()

    public data class Custom(
        val duration: Duration = DefaultDuration,
        val curves: HapticCurves,
        override val scale: Float = 1f,
        val fallback: PrimitiveType? = null,
    ) : BasicPrimitive()
}

@DslBuilder(resultName = "Primitive")
public interface PrimitiveDsl {
    public val basic: BasicPrimitive

    @DslValue(provider = InitialDelay::class)
    public var delay: Duration

    @DslValue(provider = InitialDelayType::class)
    public var delayType: DelayType
}

@DslBuilder(resultName = "HapticEffect", generateFunction = false)
public interface HapticEffectDsl {
    @DslList(hook = PrimitiveListHook::class)
    public var primitives: MutableList<Primitive>
}

@DslBuilder(resultName = "HapticCurves", buildHook = DefaultCurves::class)
public interface HapticCurvesDsl {
    @DslList(scopeName = "intensity", hook = KeyframeListHook::class)
    public var intensity: MutableList<Keyframe>

    @DslList(scopeName = "sharpness", hook = KeyframeListHook::class)
    public var sharpness: MutableList<Keyframe>
}

@DslBuilder(resultName = "Keyframe", requireConfiguration = true)
public interface KeyframeDsl {
    @DslValue(required = true, hook = KeyframeTimeHook::class)
    public var time: Duration

    @DslValue(required = true, hook = KeyframeValueHook::class)
    public var value: Float
}

@DslBuilder(resultName = "PredefinedOptions", generateFunction = false)
public interface PredefinedOptionsDsl {
    @DslValue(provider = InitialScale::class)
    public var scale: Float

    @DslValue(provider = InitialDelay::class)
    public var delay: Duration

    @DslValue(provider = InitialDelayType::class)
    public var delayType: DelayType
}

@DslBuilder(resultName = "CustomOptions", generateFunction = false)
public interface CustomOptionsDsl : PredefinedOptionsDsl {
    @DslValue(required = true)
    public var curves: HapticCurves

    @DslValue
    public var fallback: PrimitiveType?
}

public object InitialScale : DslInitialProvider<Float> {
    override fun provide(): Float = 1f
}

public object InitialDelay : DslInitialProvider<Duration> {
    override fun provide(): Duration = Duration.ZERO
}

public object InitialDelayType : DslInitialProvider<DelayType> {
    override fun provide(): DelayType = DelayType.Pause
}

public object PrimitiveListHook : DslListHook<Primitive> {
    override fun beforeSet(element: Primitive) {
        require(element.delay >= Duration.ZERO) { "Primitive delay must be non-negative." }
    }
}

public object KeyframeListHook : DslListHook<Keyframe> {
    override fun beforeSet(element: Keyframe): Unit = validateKeyframe(element)
}

public object KeyframeTimeHook : DslValueHook<Duration> {
    override fun beforeSet(value: Duration) {
        require(value >= Duration.ZERO) { "Keyframe time must be non-negative." }
    }
}

public object KeyframeValueHook : DslValueHook<Float> {
    override fun beforeSet(value: Float) {
        require(value in 0f..1f) { "Keyframe value must be between 0 and 1." }
    }
}

public object DefaultCurves : DslBuildHook<HapticCurvesDsl> {
    override fun beforeBuild(scope: HapticCurvesDsl) {
        if (scope.intensity.isEmpty()) scope.intensity += Keyframe(Duration.ZERO, 1f)
        if (scope.sharpness.isEmpty()) scope.sharpness += Keyframe(Duration.ZERO, 1f)
    }
}

public inline fun HapticEffect(block: HapticEffectDsl.() -> Unit): HapticEffect =
    HapticEffectDslBuilder().apply(block).build()

public operator fun HapticEffect.plus(primitive: Primitive): HapticEffect =
    HapticEffect(primitives + primitive)

public operator fun HapticEffect.plus(other: HapticEffect): HapticEffect =
    HapticEffect(primitives + other.primitives)

public operator fun HapticEffect.plus(type: PrimitiveType): HapticEffect = this + Primitive(type)

public operator fun HapticEffect.minus(primitive: Primitive): HapticEffect =
    HapticEffect(primitives - primitive)

public operator fun HapticEffect.minus(other: HapticEffect): HapticEffect =
    HapticEffect(primitives - other.primitives.toSet())

public operator fun HapticEffect.minus(type: PrimitiveType): HapticEffect =
    HapticEffect(primitives.filter { it.basic !is BasicPrimitive.Predefined || it.basic.type != type })

public fun Primitive(
    type: PrimitiveType,
    scale: Float = 1f,
    delay: Duration = Duration.ZERO,
    delayType: DelayType = DelayType.Pause,
): Primitive = buildPrimitive(BasicPrimitive.Predefined(type, scale)) {
    this.delay = delay
    this.delayType = delayType
}

public fun HapticEffectDsl.predefined(
    type: PrimitiveType,
    scale: Float = 1f,
    delay: Duration = Duration.ZERO,
    delayType: DelayType = DelayType.Pause,
) {
    primitives += Primitive(type, scale, delay, delayType)
}

public inline fun HapticEffectDsl.predefined(type: PrimitiveType, block: PredefinedOptionsDsl.() -> Unit) {
    val options = PredefinedOptionsDslBuilder().apply(block).build()
    predefined(type, options.scale, options.delay, options.delayType)
}

public val HapticEffectDsl.click: Unit inline get() = click()
public inline fun HapticEffectDsl.click(block: PredefinedOptionsDsl.() -> Unit = {}): Unit =
    predefined(PrimitiveType.Click, block)

public val HapticEffectDsl.thud: Unit inline get() = thud()
public inline fun HapticEffectDsl.thud(block: PredefinedOptionsDsl.() -> Unit = {}): Unit =
    predefined(PrimitiveType.Thud, block)

public val HapticEffectDsl.spin: Unit inline get() = spin()
public inline fun HapticEffectDsl.spin(block: PredefinedOptionsDsl.() -> Unit = {}): Unit =
    predefined(PrimitiveType.Spin, block)

public val HapticEffectDsl.quickRise: Unit inline get() = quickRise()
public inline fun HapticEffectDsl.quickRise(block: PredefinedOptionsDsl.() -> Unit = {}): Unit =
    predefined(PrimitiveType.QuickRise, block)

public val HapticEffectDsl.slowRise: Unit inline get() = slowRise()
public inline fun HapticEffectDsl.slowRise(block: PredefinedOptionsDsl.() -> Unit = {}): Unit =
    predefined(PrimitiveType.SlowRise, block)

public val HapticEffectDsl.quickFall: Unit inline get() = quickFall()
public inline fun HapticEffectDsl.quickFall(block: PredefinedOptionsDsl.() -> Unit = {}): Unit =
    predefined(PrimitiveType.QuickFall, block)

public val HapticEffectDsl.tick: Unit inline get() = tick()
public inline fun HapticEffectDsl.tick(block: PredefinedOptionsDsl.() -> Unit = {}): Unit =
    predefined(PrimitiveType.Tick, block)

public val HapticEffectDsl.lowTick: Unit inline get() = lowTick()
public inline fun HapticEffectDsl.lowTick(block: PredefinedOptionsDsl.() -> Unit = {}): Unit =
    predefined(PrimitiveType.LowTick, block)

public inline fun HapticEffectDsl.custom(duration: Duration = DefaultDuration, block: CustomOptionsDsl.() -> Unit) {
    val options = CustomOptionsDslBuilder().apply(block).build()
    primitives += buildPrimitive(BasicPrimitive.Custom(duration, options.curves, options.scale, options.fallback)) {
        delay = options.delay
        delayType = options.delayType
    }
}

public inline fun CustomOptionsDsl.curves(block: HapticCurvesDsl.() -> Unit) {
    curves = buildHapticCurves(block = block)
}

public val CustomOptionsDsl.clickFallback: Unit
    inline get() {
        fallback = PrimitiveType.Click
    }
public val CustomOptionsDsl.thudFallback: Unit
    inline get() {
        fallback = PrimitiveType.Thud
    }
public val CustomOptionsDsl.spinFallback: Unit
    inline get() {
        fallback = PrimitiveType.Spin
    }
public val CustomOptionsDsl.quickRiseFallback: Unit
    inline get() {
        fallback = PrimitiveType.QuickRise
    }
public val CustomOptionsDsl.slowRiseFallback: Unit
    inline get() {
        fallback = PrimitiveType.SlowRise
    }
public val CustomOptionsDsl.quickFallFallback: Unit
    inline get() {
        fallback = PrimitiveType.QuickFall
    }
public val CustomOptionsDsl.tickFallback: Unit
    inline get() {
        fallback = PrimitiveType.Tick
    }
public val CustomOptionsDsl.lowTickFallback: Unit
    inline get() {
        fallback = PrimitiveType.LowTick
    }

public fun HapticCurvesDsl.intensity(frame: Keyframe) {
    intensity += frame
}

public fun HapticCurvesDsl.intensity(time: Duration, value: Float): Unit = intensity(Keyframe(time, value))
public fun HapticCurvesDsl.intensity(value: Float = 1f): Unit = intensity(Duration.ZERO, value)
public fun HapticCurvesDsl.intensity(frames: Collection<Keyframe>) {
    intensity.addAll(frames)
}

public fun HapticCurvesDsl.intensity(vararg frames: Keyframe) {
    intensity.addAll(frames)
}

public fun HapticCurvesDsl.sharpness(frame: Keyframe) {
    sharpness += frame
}

public fun HapticCurvesDsl.sharpness(time: Duration, value: Float): Unit = sharpness(Keyframe(time, value))
public fun HapticCurvesDsl.sharpness(value: Float = 1f): Unit = sharpness(Duration.ZERO, value)
public fun HapticCurvesDsl.sharpness(frames: Collection<Keyframe>) {
    sharpness.addAll(frames)
}

public fun HapticCurvesDsl.sharpness(vararg frames: Keyframe) {
    sharpness.addAll(frames)
}

public fun DslListScope<Keyframe>.add(time: Duration, value: Float) {
    elements += Keyframe(time, value)
}

public fun DslListScope<Keyframe>.add(frame: Keyframe) {
    elements += frame
}

public fun DslListScope<Keyframe>.addAll(frames: Collection<Keyframe>) {
    elements.addAll(frames)
}

context(scope: DslListScope<Keyframe>)
public infix fun Float.at(time: Duration) {
    scope.elements += Keyframe(time, this)
}

internal fun validateKeyframe(frame: Keyframe) {
    require(frame.time >= Duration.ZERO) { "Keyframe time must be non-negative." }
    require(frame.value in 0f..1f) { "Keyframe value must be between 0 and 1." }
}

public expect fun getVibrator(
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    config: Any?,
): Vibrator

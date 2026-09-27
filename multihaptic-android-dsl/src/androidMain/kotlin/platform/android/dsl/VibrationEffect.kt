package top.ltfan.multihaptic.platform.android.dsl

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import top.ltfan.dslutilities.DslBuildHook
import top.ltfan.dslutilities.DslBuilder
import top.ltfan.dslutilities.DslInitialProvider
import top.ltfan.dslutilities.DslList
import top.ltfan.dslutilities.DslListHook
import top.ltfan.dslutilities.DslValue
import kotlin.math.roundToInt
import kotlin.time.Duration

public data class WaveformControlPoint(val data: WaveformPoint, val duration: Duration)
public data class WaveformPoint(val amplitude: Float, val frequencyHz: Float)
public data class EnvelopeControlPoint(val data: EnvelopePoint, val duration: Duration)
public data class EnvelopePoint(val intensity: Float, val sharpness: Float)
public data class AmplitudeControlPoint(val amplitude: Int, val duration: Duration)
public data class OnOffInterval(val start: Duration, val end: Duration)

@RequiresApi(Build.VERSION_CODES.R)
public enum class AndroidPrimitiveType(public val id: Int) {
    Click(VibrationEffect.Composition.PRIMITIVE_CLICK),

    @RequiresApi(Build.VERSION_CODES.S)
    Thud(VibrationEffect.Composition.PRIMITIVE_THUD),

    @RequiresApi(Build.VERSION_CODES.S)
    Spin(VibrationEffect.Composition.PRIMITIVE_SPIN), QuickRise(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE), SlowRise(
        VibrationEffect.Composition.PRIMITIVE_SLOW_RISE,
    ),
    QuickFall(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL), Tick(VibrationEffect.Composition.PRIMITIVE_TICK),

    @RequiresApi(Build.VERSION_CODES.S)
    LowTick(VibrationEffect.Composition.PRIMITIVE_LOW_TICK),
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public enum class AndroidDelayType(public val id: Int) {
    Pause(VibrationEffect.Composition.DELAY_TYPE_PAUSE), RelativeStartOffset(VibrationEffect.Composition.DELAY_TYPE_RELATIVE_START_OFFSET),
}

@DslBuilder(resultName = "WaveformConfig", generateFunction = false, buildHook = ValidateWaveform::class)
public interface WaveformDsl {
    public val initialFrequencyHz: Float?

    @DslList(hook = WaveformPointHook::class)
    public var controlPoints: MutableList<WaveformControlPoint>
}

@DslBuilder(resultName = "EnvelopeConfig", generateFunction = false, buildHook = ValidateEnvelope::class)
public interface EnvelopeDsl {
    public val initialSharpness: Float?

    @DslList(hook = EnvelopePointHook::class)
    public var controlPoints: MutableList<EnvelopeControlPoint>
}

@DslBuilder(resultName = "ComposedPrimitive", generateFunction = false)
public interface ComposedPrimitiveDsl {
    public val type: AndroidPrimitiveType

    @DslValue(initial = "1")
    public var scale: Float

    @DslValue(provider = ZeroDuration::class)
    public var delay: Duration

    @DslValue(provider = PauseDelay::class)
    public var delayType: AndroidDelayType
}

@DslBuilder(resultName = "ComposedConfig", generateFunction = false, buildHook = ValidateComposed::class)
public interface ComposedDsl {
    @DslList(hook = ComposedPrimitiveHook::class)
    public var primitives: MutableList<ComposedPrimitive>
}

@DslBuilder(resultName = "AmplitudeConfig", generateFunction = false, buildHook = ValidateAmplitude::class)
public interface AmplitudeDsl {
    public val repeat: Int

    @DslList(hook = AmplitudePointHook::class)
    public var controlPoints: MutableList<AmplitudeControlPoint>
}

@DslBuilder(resultName = "OnOffConfig", generateFunction = false, buildHook = ValidateOnOff::class)
public interface OnOffDsl {
    public val repeat: Int

    @DslList(hook = OnOffIntervalHook::class)
    public var intervals: MutableList<OnOffInterval>
}

public object ZeroDuration : DslInitialProvider<Duration> {
    override fun provide(): Duration = Duration.ZERO
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public object PauseDelay : DslInitialProvider<AndroidDelayType> {
    override fun provide(): AndroidDelayType = AndroidDelayType.Pause
}

public object WaveformPointHook : DslListHook<WaveformControlPoint> {
    override fun beforeSet(element: WaveformControlPoint) {
        require(element.data.amplitude in 0f..1f) { "Amplitude must be between 0 and 1." }
        require(element.data.frequencyHz >= 0f) { "Frequency must be non-negative." }
        require(element.duration > Duration.ZERO) { "Duration must be positive." }
    }
}

public object EnvelopePointHook : DslListHook<EnvelopeControlPoint> {
    override fun beforeSet(element: EnvelopeControlPoint) {
        require(element.data.intensity in 0f..1f) { "Intensity must be between 0 and 1." }
        require(element.data.sharpness in 0f..1f) { "Sharpness must be between 0 and 1." }
        require(element.duration > Duration.ZERO) { "Duration must be positive." }
    }
}

public object ComposedPrimitiveHook : DslListHook<ComposedPrimitive> {
    override fun beforeSet(element: ComposedPrimitive) {
        require(element.scale in 0f..1f) { "Scale must be between 0 and 1." }
        require(element.delay >= Duration.ZERO) { "Delay must be non-negative." }
    }
}

public object AmplitudePointHook : DslListHook<AmplitudeControlPoint> {
    override fun beforeSet(element: AmplitudeControlPoint) {
        require(element.amplitude in 0..255) { "Amplitude must be between 0 and 255." }
        require(element.duration > Duration.ZERO) { "Duration must be positive." }
    }
}

public object OnOffIntervalHook : DslListHook<OnOffInterval> {
    override fun beforeSet(element: OnOffInterval) {
        require(element.start >= Duration.ZERO && element.end > element.start) {
            "An on/off interval must have a non-negative start before its end."
        }
    }
}

public object ValidateWaveform : DslBuildHook<WaveformDsl> {
    override fun beforeBuild(scope: WaveformDsl) {
        check(scope.controlPoints.isNotEmpty()) { "Waveform effects must have at least one control point." }
        require(scope.initialFrequencyHz == null || scope.initialFrequencyHz!! >= 0f)
    }
}

public object ValidateEnvelope : DslBuildHook<EnvelopeDsl> {
    override fun beforeBuild(scope: EnvelopeDsl) {
        check(scope.controlPoints.isNotEmpty()) { "Basic envelope effects must have at least one control point." }
        check(scope.controlPoints.last().data.intensity == 0f) {
            "Basic envelope effects must end at a zero intensity control point."
        }
        require(scope.initialSharpness?.let { it in 0f..1f } ?: true)
    }
}

public object ValidateComposed : DslBuildHook<ComposedDsl> {
    override fun beforeBuild(scope: ComposedDsl) {
        check(scope.primitives.isNotEmpty()) { "Composed effects must have at least one primitive." }
    }
}

public object ValidateAmplitude : DslBuildHook<AmplitudeDsl> {
    override fun beforeBuild(scope: AmplitudeDsl) {
        check(scope.controlPoints.any { it.duration > Duration.ZERO }) {
            "Amplitude effects must have at least one non-zero duration."
        }
    }
}

public object ValidateOnOff : DslBuildHook<OnOffDsl> {
    override fun beforeBuild(scope: OnOffDsl) {
        check(scope.intervals.isNotEmpty()) { "On-off effects must have at least one effect." }
        scope.intervals = normalizeIntervals(scope.intervals)
    }
}

private fun normalizeIntervals(intervals: List<OnOffInterval>): MutableList<OnOffInterval> {
    val merged = mutableListOf<OnOffInterval>()
    for (interval in intervals.sortedBy { it.start }) {
        val last = merged.lastOrNull()
        if (last == null || interval.start > last.end) merged += interval
        else merged[merged.lastIndex] = last.copy(end = maxOf(last.end, interval.end))
    }
    return merged
}

public inline fun waveformConfig(initialFrequencyHz: Float? = null, block: WaveformDsl.() -> Unit): WaveformConfig =
    WaveformDslBuilder(initialFrequencyHz).apply(block).build()

public inline fun envelopeConfig(initialSharpness: Float? = null, block: EnvelopeDsl.() -> Unit): EnvelopeConfig =
    EnvelopeDslBuilder(initialSharpness).apply(block).build()

@RequiresApi(Build.VERSION_CODES.R)
public inline fun composedConfig(block: ComposedDsl.() -> Unit): ComposedConfig =
    ComposedDslBuilder().apply(block).build()

public inline fun amplitudeConfig(repeat: Int = -1, block: AmplitudeDsl.() -> Unit): AmplitudeConfig =
    AmplitudeDslBuilder(repeat).apply(block).build()

public inline fun onOffConfig(repeat: Int = -1, block: OnOffDsl.() -> Unit): OnOffConfig =
    OnOffDslBuilder(repeat).apply(block).build()

private inline fun <D, C> MutableList<C>.insertAt(
    data: D,
    time: Duration,
    duration: (C) -> Duration,
    copy: (C, Duration) -> C,
    create: (D, Duration) -> C,
) {
    require(time >= Duration.ZERO) { "Control point time must be non-negative." }
    var elapsed = Duration.ZERO
    for (index in indices) {
        val next = elapsed + duration(this[index])
        require(time != next) { "There is already a control point at $time." }
        if (time < next) {
            val old = this[index]
            add(index, create(data, time - elapsed))
            this[index + 1] = copy(old, next - time)
            return
        }
        elapsed = next
    }
    add(create(data, time - elapsed))
}

public val WaveformDsl.list: MutableList<WaveformControlPoint> inline get() = controlPoints

@Suppress("UnusedReceiverParameter")
public fun WaveformDsl.point(amplitude: Float, frequencyHz: Float): WaveformPoint =
    WaveformPoint(amplitude, frequencyHz)

public fun WaveformDsl.add(point: WaveformControlPoint) {
    controlPoints += point
}

public fun WaveformDsl.add(amplitude: Float, frequencyHz: Float, duration: Duration): Unit =
    add(WaveformControlPoint(WaveformPoint(amplitude, frequencyHz), duration))

context(scope: WaveformDsl)
public infix fun WaveformPoint.after(duration: Duration) {
    scope.add(WaveformControlPoint(this, duration))
}

context(scope: WaveformDsl)
public infix fun WaveformPoint.at(time: Duration) {
    scope.controlPoints.insertAt(
        this,
        time,
        { it.duration },
        { point, length -> point.copy(duration = length) },
        ::WaveformControlPoint,
    )
}

public val EnvelopeDsl.list: MutableList<EnvelopeControlPoint> inline get() = controlPoints

@Suppress("UnusedReceiverParameter")
public fun EnvelopeDsl.point(intensity: Float, sharpness: Float): EnvelopePoint = EnvelopePoint(intensity, sharpness)
public fun EnvelopeDsl.add(point: EnvelopeControlPoint) {
    controlPoints += point
}

public fun EnvelopeDsl.add(intensity: Float, sharpness: Float, duration: Duration): Unit =
    add(EnvelopeControlPoint(EnvelopePoint(intensity, sharpness), duration))

context(scope: EnvelopeDsl)
public infix fun EnvelopePoint.after(duration: Duration) {
    scope.add(EnvelopeControlPoint(this, duration))
}

context(scope: EnvelopeDsl)
public infix fun EnvelopePoint.at(time: Duration) {
    scope.controlPoints.insertAt(
        this,
        time,
        { it.duration },
        { point, length -> point.copy(duration = length) },
        ::EnvelopeControlPoint,
    )
}

public val AmplitudeDsl.list: MutableList<AmplitudeControlPoint> inline get() = controlPoints
public fun AmplitudeDsl.add(amplitude: Int, duration: Duration) {
    controlPoints += AmplitudeControlPoint(amplitude, duration)
}

context(scope: AmplitudeDsl)
public infix fun Int.after(duration: Duration) {
    scope.add(this, duration)
}

context(scope: AmplitudeDsl)
public infix fun Int.at(time: Duration) {
    scope.controlPoints.insertAt(
        this,
        time,
        { it.duration },
        { point, length -> point.copy(duration = length) },
        ::AmplitudeControlPoint,
    )
}

context(scope: AmplitudeDsl)
public infix fun Float.after(duration: Duration) {
    (this * 255).roundToInt().coerceIn(0..255) after duration
}

context(scope: AmplitudeDsl)
public infix fun Float.at(time: Duration) {
    (this * 255).roundToInt().coerceIn(0..255) at time
}

public val OnOffDsl.list: MutableList<OnOffInterval> inline get() = intervals
public fun OnOffDsl.range(start: Duration, end: Duration) {
    require(start >= Duration.ZERO) { "Effect start time must be non-negative." }
    require(end >= Duration.ZERO) { "Effect end time must be non-negative." }
    require(start <= end) { "Effect start must not exceed its end." }
    if (start == end) return
    val merged = normalizeIntervals(intervals + OnOffInterval(start, end))
    intervals.clear()
    intervals.addAll(merged)
}

public fun OnOffDsl.range(range: ClosedRange<Duration>): Unit = range(range.start, range.endInclusive)
public fun OnOffDsl.pattern(off: Duration, on: Duration) {
    require(off >= Duration.ZERO) { "Off duration must be non-negative." }
    require(on >= Duration.ZERO) { "On duration must be non-negative." }
    val start = (intervals.maxOfOrNull { it.end } ?: Duration.ZERO) + off
    range(start, start + on)
}

@RequiresApi(Build.VERSION_CODES.R)
public inline fun ComposedDsl.primitive(type: AndroidPrimitiveType, block: ComposedPrimitiveDsl.() -> Unit = {}) {
    primitives += ComposedPrimitiveDslBuilder(type).apply(block).build()
}

@get:RequiresApi(Build.VERSION_CODES.R)
public val ComposedDsl.click: Unit inline get() = click()

@RequiresApi(Build.VERSION_CODES.R)
public inline fun ComposedDsl.click(block: ComposedPrimitiveDsl.() -> Unit = {}): Unit =
    primitive(AndroidPrimitiveType.Click, block)

@get:RequiresApi(Build.VERSION_CODES.S)
public val ComposedDsl.thud: Unit inline get() = thud()

@RequiresApi(Build.VERSION_CODES.S)
public inline fun ComposedDsl.thud(block: ComposedPrimitiveDsl.() -> Unit = {}): Unit =
    primitive(AndroidPrimitiveType.Thud, block)

@get:RequiresApi(Build.VERSION_CODES.S)
public val ComposedDsl.spin: Unit inline get() = spin()

@RequiresApi(Build.VERSION_CODES.S)
public inline fun ComposedDsl.spin(block: ComposedPrimitiveDsl.() -> Unit = {}): Unit =
    primitive(AndroidPrimitiveType.Spin, block)

@get:RequiresApi(Build.VERSION_CODES.R)
public val ComposedDsl.quickRise: Unit inline get() = quickRise()

@RequiresApi(Build.VERSION_CODES.R)
public inline fun ComposedDsl.quickRise(block: ComposedPrimitiveDsl.() -> Unit = {}): Unit =
    primitive(AndroidPrimitiveType.QuickRise, block)

@get:RequiresApi(Build.VERSION_CODES.R)
public val ComposedDsl.slowRise: Unit inline get() = slowRise()

@RequiresApi(Build.VERSION_CODES.R)
public inline fun ComposedDsl.slowRise(block: ComposedPrimitiveDsl.() -> Unit = {}): Unit =
    primitive(AndroidPrimitiveType.SlowRise, block)

@get:RequiresApi(Build.VERSION_CODES.R)
public val ComposedDsl.quickFall: Unit inline get() = quickFall()

@RequiresApi(Build.VERSION_CODES.R)
public inline fun ComposedDsl.quickFall(block: ComposedPrimitiveDsl.() -> Unit = {}): Unit =
    primitive(AndroidPrimitiveType.QuickFall, block)

@get:RequiresApi(Build.VERSION_CODES.R)
public val ComposedDsl.tick: Unit inline get() = tick()

@RequiresApi(Build.VERSION_CODES.R)
public inline fun ComposedDsl.tick(block: ComposedPrimitiveDsl.() -> Unit = {}): Unit =
    primitive(AndroidPrimitiveType.Tick, block)

@get:RequiresApi(Build.VERSION_CODES.S)
public val ComposedDsl.lowTick: Unit inline get() = lowTick()

@RequiresApi(Build.VERSION_CODES.S)
public inline fun ComposedDsl.lowTick(block: ComposedPrimitiveDsl.() -> Unit = {}): Unit =
    primitive(AndroidPrimitiveType.LowTick, block)

@get:RequiresApi(Build.VERSION_CODES.BAKLAVA)
public val ComposedPrimitiveDsl.pauseDelay: Unit
    inline get() {
        delayType = AndroidDelayType.Pause
    }

@get:RequiresApi(Build.VERSION_CODES.BAKLAVA)
public val ComposedPrimitiveDsl.relativeStartOffsetDelay: Unit
    inline get() {
        delayType = AndroidDelayType.RelativeStartOffset
    }

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public fun WaveformConfig.toNative(): VibrationEffect = VibrationEffect.WaveformEnvelopeBuilder().apply {
    initialFrequencyHz?.let(::setInitialFrequencyHz)
    controlPoints.forEach { addControlPoint(it.data.amplitude, it.data.frequencyHz, it.duration.inWholeMilliseconds) }
}.build()

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public fun EnvelopeConfig.toNative(): VibrationEffect = VibrationEffect.BasicEnvelopeBuilder().apply {
    initialSharpness?.let(::setInitialSharpness)
    controlPoints.forEach { addControlPoint(it.data.intensity, it.data.sharpness, it.duration.inWholeMilliseconds) }
}.build()

@RequiresApi(Build.VERSION_CODES.R)
public fun ComposedConfig.toNative(): VibrationEffect = VibrationEffect.startComposition().apply {
    primitives.forEach { primitive ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            addPrimitive(
                primitive.type.id,
                primitive.scale,
                primitive.delay.inWholeMilliseconds.toInt(),
                primitive.delayType.id,
            )
        } else {
            addPrimitive(primitive.type.id, primitive.scale, primitive.delay.inWholeMilliseconds.toInt())
        }
    }
}.compose()

@RequiresApi(Build.VERSION_CODES.O)
public fun AmplitudeConfig.toNative(): VibrationEffect = VibrationEffect.createWaveform(
    controlPoints.map { it.duration.inWholeMilliseconds }.toLongArray(),
    controlPoints.map { it.amplitude }.toIntArray(),
    repeat,
)

@RequiresApi(Build.VERSION_CODES.O)
public fun OnOffConfig.toNative(): VibrationEffect {
    val durations = mutableListOf<Long>()
    var cursor = Duration.ZERO
    for ((start, end) in intervals) {
        durations += (start - cursor).inWholeMilliseconds
        durations += (end - start).inWholeMilliseconds
        cursor = end
    }
    return VibrationEffect.createWaveform(durations.toLongArray(), repeat)
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public inline fun waveformVibrationEffect(
    initialFrequencyHz: Float? = null,
    block: WaveformDsl.() -> Unit,
): VibrationEffect = waveformConfig(initialFrequencyHz, block).toNative()

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public inline fun envelopeVibrationEffect(
    initialSharpness: Float? = null,
    block: EnvelopeDsl.() -> Unit,
): VibrationEffect = envelopeConfig(initialSharpness, block).toNative()

@RequiresApi(Build.VERSION_CODES.R)
public inline fun composedVibrationEffect(block: ComposedDsl.() -> Unit): VibrationEffect =
    composedConfig(block).toNative()

@RequiresApi(Build.VERSION_CODES.O)
public inline fun amplitudeVibrationEffect(repeat: Int = -1, block: AmplitudeDsl.() -> Unit): VibrationEffect =
    amplitudeConfig(repeat, block).toNative()

@RequiresApi(Build.VERSION_CODES.O)
public inline fun onOffVibrationEffect(repeat: Int = -1, block: OnOffDsl.() -> Unit): VibrationEffect =
    onOffConfig(repeat, block).toNative()

public inline fun <R> Context.withVibratorOrNull(block: Vibrator?.() -> R): R = getSystemService<Vibrator>().block()

public inline fun <R> Context.tryWithVibrator(failed: () -> Unit = {}, block: Vibrator.() -> R): R? =
    getSystemService<Vibrator>()?.block() ?: run { failed(); null }

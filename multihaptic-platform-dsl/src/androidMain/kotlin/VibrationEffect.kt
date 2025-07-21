package platform.android.dsl

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import top.ltfan.dslutilities.LockableValueDsl
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A builder for creating waveform vibration effects.
 *
 * @property initialFrequencyHz The initial frequency of the waveform
 *   effect, in Hz. If null, the default frequency will be used.
 */
@WaveformVibrationEffectBuilder.Dsl
open class WaveformVibrationEffectBuilder(
    @field:FloatRange(from = 0.0) private val initialFrequencyHz: Float? = null,
) : LockableValueDsl() {
    private var pointer = Duration.ZERO

    /**
     * A list of control points for the waveform effect.
     *
     * Each control point consists of a [PointData] and a [Duration]. The
     * operations on this list are controlled by the DSL.
     */
    val list by list<ControlPoint>(
        beforeSet = { _, it -> beforeSet(it) },
        beforeAccess = {
            if (!isLocked) return@list
            check(isNotEmpty()) { "Waveform effects must have at least one control point." }
        },
    )

    /**
     * Adds a control point to the waveform effect.
     *
     * @param point The control point to add, which contains the amplitude,
     *   frequency, and duration.
     */
    fun add(point: ControlPoint) {
        list.add(point)
        pointer += point.duration
    }

    /**
     * Adds a control point to the waveform effect with a specified amplitude,
     * frequency, and duration.
     *
     * @param amplitude The amplitude of the control point. Must be between 0.0
     *   and 1.0.
     * @param frequencyHz The frequency of the control point, in Hz. Must be
     *   non-negative.
     * @param duration The transition time to this control point.
     */
    fun add(
        @FloatRange(from = 0.0, to = 1.0) amplitude: Float,
        @FloatRange(from = 0.0) frequencyHz: Float,
        duration: Duration
    ) {
        add(ControlPoint(point(amplitude, frequencyHz), duration))
    }

    /**
     * Creates a [PointData] instance representing a point in the waveform
     * effect.
     *
     * @param amplitude The amplitude of the point. Must be between 0.0 and
     *   1.0.
     * @param frequencyHz The frequency of the point, in Hz. Must be
     *   non-negative.
     * @return A [PointData] instance with the specified amplitude and
     *   frequency.
     */
    fun point(@FloatRange(from = 0.0, to = 1.0) amplitude: Float, @FloatRange(from = 0.0) frequencyHz: Float) =
        PointData(amplitude, frequencyHz)

    /**
     * Adds a control point to the waveform effect.
     *
     * @param duration The transition time to this control point.
     * @receiver The point data, which contains the amplitude and frequency.
     *   Can be created using [point].
     */
    infix fun PointData.after(duration: Duration) {
        add(ControlPoint(this, duration))
    }

    /**
     * Adds a control point to the waveform effect at a specific time.
     *
     * **Warning:** For best performance, add points in chronological order
     * and without overlap. Overlapping or out-of-order points can cause
     * significant performance issues.
     *
     * @param time The absolute time at which the effect should reach this
     *   control point.
     * @receiver The point data, which contains the amplitude and frequency.
     *   Can be created using [point].
     */
    infix fun PointData.at(time: Duration) = addControlPoint(
        data = this,
        time = time,
        pointer = pointer,
        list = list,
        setPointer = { pointer = it },
        getDuration = { it.duration },
        copyWithDuration = { point, duration -> point.copy(duration = duration) },
        createControlPoint = { data, duration -> ControlPoint(data, duration) },
    )

    private fun beforeSet(point: ControlPoint) {
        require(point.data.amplitude in 0f..1f) { "Amplitude must be between 0 and 1" }
        require(point.data.frequencyHz >= 0f) { "Frequency must be non-negative" }
        require(point.duration > Duration.ZERO) { "Duration must be positive." }
    }

    /**
     * A pre-build hook that can be overridden to perform actions before the
     * effect is built.
     *
     * This method is called before the vibration effect is built, allowing for
     * any necessary setup or validation.
     */
    protected open fun preBuild() {}

    /**
     * Builds the [VibrationEffect] using the current list of control points.
     *
     * @return The constructed [VibrationEffect].
     */
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun build(): VibrationEffect {
        preBuild()
        lock()
        return VibrationEffect.WaveformEnvelopeBuilder().apply {
            initialFrequencyHz?.let { setInitialFrequencyHz(it) }
            list.forEach { (data, duration) ->
                val (amplitude, frequencyHz) = data
                addControlPoint(amplitude, frequencyHz, duration.inWholeMilliseconds)
            }
        }.build()
    }

    /**
     * Represents a point in the waveform effect, with an amplitude and
     * frequency.
     *
     * @property amplitude The amplitude of the point. Must be between 0.0 and
     *   1.0.
     * @property frequencyHz The frequency of the point, in Hz. Must be
     *   non-negative.
     */
    data class PointData(
        @field:FloatRange(from = 0.0, to = 1.0) val amplitude: Float,
        @field:FloatRange(from = 0.0) val frequencyHz: Float,
    )

    /**
     * Represents a control point in the waveform effect with a specific
     * duration.
     *
     * @property data The point data containing amplitude and frequency.
     * @property duration The transition time.
     */
    data class ControlPoint(
        val data: PointData,
        val duration: Duration,
    )

    @DslMarker
    annotation class Dsl
}

/**
 * Creates a [VibrationEffect] using waveform effects.
 *
 * @param initialFrequencyHz The initial frequency of the waveform effect,
 *   or null to use the default value.
 * @param block The DSL block to configure the waveform vibration effect.
 * @sample samples.dsl.waveform
 */
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
inline fun waveformVibrationEffect(
    @FloatRange(from = 0.0) initialFrequencyHz: Float? = null, block: WaveformVibrationEffectBuilder.() -> Unit
): VibrationEffect = WaveformVibrationEffectBuilder(initialFrequencyHz).apply(block).build()

/**
 * A builder for creating basic envelope vibration effects.
 *
 * @property initialSharpness The initial sharpness of the envelope effect.
 *   Must be between 0.0 and 1.0. If null, the default sharpness will be
 *   used.
 */
@EnvelopeVibrationEffectBuilder.Dsl
open class EnvelopeVibrationEffectBuilder(
    @field:FloatRange(from = 0.0, to = 1.0) private val initialSharpness: Float? = null,
) : LockableValueDsl() {
    private var pointer = Duration.ZERO

    /**
     * A list of control points for the envelope effect.
     *
     * Each control point consists of a [PointData] and a [Duration]. The
     * operations on this list are controlled by the DSL.
     */
    val list by list<ControlPoint>(
        beforeSet = { _, it -> beforeSet(it) },
        beforeAccess = {
            if (!isLocked) return@list
            check(!isEmpty()) { "Basic envelope effects must have at least one control point." }
            check(last().data.intensity == 0f) { "Basic envelope effects must end at a zero intensity control point." }
        },
    )

    /**
     * Adds a control point to the envelope effect.
     *
     * @param point The control point to add, which contains the intensity,
     *   sharpness, and duration.
     */
    fun add(point: ControlPoint) {
        list.add(point)
        pointer += point.duration
    }

    /**
     * Adds a control point to the envelope effect with a specified intensity,
     * sharpness, and duration.
     *
     * @param intensity The intensity of the control point. Must be between 0.0
     *   and 1.0.
     * @param sharpness The sharpness of the control point. Must be between 0.0
     *   and 1.0.
     * @param duration The transition time to this control point.
     */
    fun add(
        @FloatRange(from = 0.0, to = 1.0) intensity: Float,
        @FloatRange(from = 0.0, to = 1.0) sharpness: Float,
        duration: Duration
    ) {
        add(ControlPoint(point(intensity, sharpness), duration))
    }

    /**
     * Creates a [PointData] instance representing a point in the envelope
     * effect.
     *
     * @param intensity The intensity of the point. Must be between 0.0 and
     *   1.0.
     * @param sharpness The sharpness of the point. Must be between 0.0 and
     *   1.0.
     * @return A [PointData] instance with the specified intensity and
     *   sharpness.
     */
    fun point(@FloatRange(from = 0.0, to = 1.0) intensity: Float, @FloatRange(from = 0.0, to = 1.0) sharpness: Float) =
        PointData(intensity, sharpness)

    /**
     * Adds a control point to the envelope effect.
     *
     * @param duration The transition time to this control point.
     * @receiver The point data, which contains the intensity and sharpness.
     *   Can be created using [point].
     */
    infix fun PointData.after(duration: Duration) {
        add(ControlPoint(this, duration))
    }

    /**
     * Adds a control point to the envelope effect at a specific time.
     *
     * **Warning:** For best performance, add points in chronological order
     * and without overlap. Overlapping or out-of-order points can cause
     * significant performance issues.
     *
     * @param time The absolute time at which the effect should reach this
     *   control point.
     * @receiver The point data, which contains the intensity and sharpness.
     *   Can be created using [point].
     */
    infix fun PointData.at(time: Duration) = addControlPoint(
        data = this,
        time = time,
        pointer = pointer,
        list = list,
        setPointer = { pointer = it },
        getDuration = { it.duration },
        copyWithDuration = { point, duration -> point.copy(duration = duration) },
        createControlPoint = { data, duration -> ControlPoint(data, duration) },
    )

    private fun beforeSet(point: ControlPoint) {
        require(point.data.intensity in 0f..1f) { "Intensity must be between 0 and 1" }
        require(point.data.sharpness in 0f..1f) { "Sharpness must be between 0 and 1" }
        require(point.duration > Duration.ZERO) { "Duration must be positive." }
    }

    /**
     * A pre-build hook that can be overridden to perform actions before the
     * effect is built.
     *
     * This method is called before the vibration effect is built, allowing for
     * any necessary setup or validation.
     */
    protected open fun preBuild() {}

    /**
     * Builds the [VibrationEffect] using the current list of control points.
     *
     * @return The constructed [VibrationEffect].
     */
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun build(): VibrationEffect {
        preBuild()
        lock()
        return VibrationEffect.BasicEnvelopeBuilder().apply {
            list.forEach { (data, duration) ->
                val (intensity, sharpness) = data
                initialSharpness?.let { setInitialSharpness(it) }
                addControlPoint(intensity, sharpness, duration.inWholeMilliseconds)
            }
        }.build()
    }

    /**
     * Represents a point in the envelope effect, with an intensity and
     * sharpness.
     *
     * @property intensity The intensity of the point. Must be between 0.0 and
     *   1.0.
     * @property sharpness The sharpness of the point. Must be between 0.0 and
     *   1.0.
     */
    data class PointData(
        @field:FloatRange(from = 0.0, to = 1.0) val intensity: Float,
        @field:FloatRange(from = 0.0, to = 1.0) val sharpness: Float,
    )

    /**
     * Represents a control point in the envelope effect, with a specific
     * duration.
     *
     * @property data The point data, which contains the intensity and
     *   sharpness.
     * @property duration The transition time to this control point.
     */
    data class ControlPoint(
        val data: PointData,
        val duration: Duration,
    )

    @DslMarker
    annotation class Dsl
}

/**
 * Creates a [VibrationEffect] using a basic envelope.
 *
 * @param initialSharpness The initial sharpness of the envelope effect.
 *   Must be between 0.0 and 1.0. If null, the default sharpness will be
 *   used.
 * @param block The DSL block to configure the envelope vibration effect.
 * @sample samples.dsl.envelope
 */
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
inline fun envelopeVibrationEffect(
    @FloatRange(from = 0.0, to = 1.0) initialSharpness: Float? = null, block: EnvelopeVibrationEffectBuilder.() -> Unit
): VibrationEffect = EnvelopeVibrationEffectBuilder(initialSharpness).apply(block).build()

/**
 * A builder for creating composed vibration effects from a list of
 * primitives.
 *
 * This builder allows you to create complex vibration effects by combining
 * multiple primitive effects with specified scales and delays.
 */
@RequiresApi(Build.VERSION_CODES.R)
@ComposedVibrationEffectBuilder.Dsl
open class ComposedVibrationEffectBuilder : LockableValueDsl() {
    /**
     * A list of primitives that make up the composed vibration effect.
     *
     * Each primitive can be added using the [primitive] method, which allows
     * for customization of the type, scale, and delay.
     */
    val list by list<Primitive>(
        beforeSet = { _, primitive ->
            require(primitive.scale in 0f..1f) { "Scale must be between 0 and 1" }
            require(primitive.delay >= Duration.ZERO) { "Delay must be non-negative" }
        },
        beforeAccess = {
            if (!isLocked) return@list
            check(isNotEmpty()) { "Composed effects must have at least one primitive." }
        },
    )

    /**
     * Adds a primitive to the list.
     *
     * @param type The type of the primitive effect to add.
     * @param block A lambda to configure the primitive.
     */
    inline fun primitive(type: PrimitiveType, block: Primitive.Builder.() -> Unit = {}) {
        list.add(Primitive.Builder(type).apply(block).build())
    }

    /**
     * Adds a [PrimitiveType.Click] primitive to the list.
     *
     * This is a shorthand for adding a click primitive with default
     * configuration.
     */
    val click inline get() = click()

    /**
     * Adds a [PrimitiveType.Click] primitive to the list.
     *
     * @param block A lambda to configure the primitive.
     */
    inline fun click(block: Primitive.Builder.() -> Unit = {}) {
        primitive(PrimitiveType.Click, block)
    }

    /**
     * Adds a [PrimitiveType.Thud] primitive to the list.
     *
     * This is a shorthand for adding a thud primitive with default
     * configuration.
     */
    val thud @RequiresApi(Build.VERSION_CODES.S) inline get() = thud()

    /**
     * Adds a [PrimitiveType.Thud] primitive to the list.
     *
     * @param block A lambda to configure the primitive.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    inline fun thud(block: Primitive.Builder.() -> Unit = {}) {
        primitive(PrimitiveType.Thud, block)
    }

    /**
     * Adds a [PrimitiveType.Spin] primitive to the list.
     *
     * This is a shorthand for adding a spin primitive with default
     * configuration.
     */
    val spin @RequiresApi(Build.VERSION_CODES.S) inline get() = spin()

    /**
     * Adds a [PrimitiveType.Spin] primitive to the list.
     *
     * @param block A lambda to configure the primitive.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    inline fun spin(block: Primitive.Builder.() -> Unit = {}) {
        primitive(PrimitiveType.Spin, block)
    }

    /**
     * Adds a [PrimitiveType.QuickRise] primitive to the list.
     *
     * This is a shorthand for adding a quick rise primitive with default
     * configuration.
     */
    val quickRise inline get() = quickRise()

    /**
     * Adds a [PrimitiveType.QuickRise] primitive to the list.
     *
     * @param block A lambda to configure the primitive.
     */
    inline fun quickRise(block: Primitive.Builder.() -> Unit = {}) {
        primitive(PrimitiveType.QuickRise, block)
    }

    /**
     * Adds a [PrimitiveType.SlowRise] primitive to the list.
     *
     * This is a shorthand for adding a slow rise primitive with default
     * configuration.
     */
    val slowRise inline get() = slowRise()

    /**
     * Adds a [PrimitiveType.SlowRise] primitive to the list.
     *
     * @param block A lambda to configure the primitive.
     */
    inline fun slowRise(block: Primitive.Builder.() -> Unit = {}) {
        primitive(PrimitiveType.SlowRise, block)
    }

    /**
     * Adds a [PrimitiveType.QuickFall] primitive to the list.
     *
     * This is a shorthand for adding a quick fall primitive with default
     * configuration.
     */
    val quickFall inline get() = quickFall()

    /**
     * Adds a [PrimitiveType.QuickFall] primitive to the list.
     *
     * @param block A lambda to configure the primitive.
     */
    inline fun quickFall(block: Primitive.Builder.() -> Unit = {}) {
        primitive(PrimitiveType.QuickFall, block)
    }

    /**
     * Adds a [PrimitiveType.Tick] primitive to the list.
     *
     * This is a shorthand for adding a tick primitive with default
     * configuration.
     */
    val tick inline get() = tick()

    /**
     * Adds a [PrimitiveType.Tick] primitive to the list.
     *
     * @param block A lambda to configure the primitive.
     */
    inline fun tick(block: Primitive.Builder.() -> Unit = {}) {
        primitive(PrimitiveType.Tick, block)
    }

    /**
     * Adds a [PrimitiveType.LowTick] primitive to the list.
     *
     * This is a shorthand for adding a low tick primitive with default
     * configuration.
     */
    val lowTick @RequiresApi(Build.VERSION_CODES.S) inline get() = lowTick()

    /**
     * Adds a [PrimitiveType.LowTick] primitive to the list.
     *
     * @param block A lambda to configure the primitive.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    inline fun lowTick(block: Primitive.Builder.() -> Unit = {}) {
        primitive(PrimitiveType.LowTick, block)
    }

    /**
     * A pre-build hook that can be overridden to perform actions before the
     * effect is built.
     *
     * This method is called before the vibration effect is built, allowing for
     * any necessary setup or validation.
     */
    open fun preBuild() {}

    /**
     * Builds the [VibrationEffect] using the current list of primitives.
     *
     * @return The constructed [VibrationEffect].
     */
    fun build(): VibrationEffect {
        preBuild()
        lock()
        return VibrationEffect.startComposition().apply {
            list.forEach { primitive ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                    if (primitive !is Primitive.Baklava) return@forEach
                    addPrimitive(
                        primitive.type.id,
                        primitive.scale,
                        primitive.delay.inWholeMilliseconds.toInt(),
                        primitive.delayType.id,
                    )
                } else {
                    if (primitive !is Primitive.R) return@forEach
                    addPrimitive(
                        primitive.type.id,
                        primitive.scale,
                        primitive.delay.inWholeMilliseconds.toInt(),
                    )
                }
            }
        }.compose()
    }

    sealed class Primitive {
        /** The primitive type to add. */
        abstract val type: PrimitiveType

        /** The scale to apply to the intensity of the primitive. */
        abstract val scale: Float

        /** The duration to wait before playing this primitive. */
        abstract val delay: Duration

        data class R(
            override val type: PrimitiveType,
            @field:FloatRange(from = 0.0, to = 1.0) override val scale: Float = 1f,
            override val delay: Duration = Duration.ZERO,
        ) : Primitive()

        @RequiresApi(Build.VERSION_CODES.BAKLAVA)
        data class Baklava(
            override val type: PrimitiveType,
            @field:FloatRange(from = 0.0, to = 1.0) override val scale: Float = 1f,
            override val delay: Duration = Duration.ZERO,

            /**
             * The type of delay to be applied, e.g. a pause between last primitive and
             * this one or a start offset. Defaults to [DelayType.Pause].
             */
            val delayType: DelayType = DelayType.Pause,
        ) : Primitive()

        @Dsl
        class Builder(private val type: PrimitiveType) : LockableValueDsl() {
            /** The scale to apply to the intensity of the primitive. */
            var scale by prepared(1f)

            /** The duration to wait before playing this primitive. */
            var delay by prepared(Duration.ZERO)

            /**
             * The type of delay to be applied, e.g. a pause between last primitive and
             * this one or a start offset. Defaults to [DelayType.Pause].
             */
            @delegate:RequiresApi(Build.VERSION_CODES.BAKLAVA)
            var delayType by prepared(DelayType.Pause)

            /** Sets the delay type to [DelayType.Pause]. */
            val pauseDelay: Unit
                @RequiresApi(Build.VERSION_CODES.BAKLAVA) inline get() {
                    delayType = DelayType.Pause
                }

            /** Sets the delay type to [DelayType.RelativeStartOffset]. */
            val relativeStartOffsetDelay: Unit
                @RequiresApi(Build.VERSION_CODES.BAKLAVA) inline get() {
                    delayType = DelayType.RelativeStartOffset
                }

            fun build() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                Baklava(type, scale, delay, delayType)
            } else {
                R(type, scale, delay)
            }
        }
    }

    /** Represents different types of primitive haptic effects. */
    enum class PrimitiveType(val id: Int) {
        /**
         * This effect should produce a sharp, crisp click sensation.
         *
         * @see VibrationEffect.Composition.PRIMITIVE_CLICK
         */
        Click(VibrationEffect.Composition.PRIMITIVE_CLICK),

        /**
         * A haptic effect that simulates downwards movement with gravity. Often
         * followed by extra energy of hitting and reverberation to augment
         * physicality.
         *
         * @see VibrationEffect.Composition.PRIMITIVE_THUD
         */
        @RequiresApi(Build.VERSION_CODES.S)
        Thud(VibrationEffect.Composition.PRIMITIVE_THUD),

        /**
         * A haptic effect that simulates spinning momentum.
         *
         * @see VibrationEffect.Composition.PRIMITIVE_SPIN
         */
        @RequiresApi(Build.VERSION_CODES.S)
        Spin(VibrationEffect.Composition.PRIMITIVE_SPIN),

        /**
         * A haptic effect that simulates quick upward movement against gravity.
         *
         * @see VibrationEffect.Composition.PRIMITIVE_QUICK_RISE
         */
        QuickRise(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE),

        /**
         * A haptic effect that simulates slow upward movement against gravity.
         *
         * @see VibrationEffect.Composition.PRIMITIVE_SLOW_RISE
         */
        SlowRise(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE),

        /**
         * A haptic effect that simulates quick downwards movement with gravity.
         *
         * @see VibrationEffect.Composition.PRIMITIVE_QUICK_FALL
         */
        QuickFall(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL),

        /**
         * This very short effect should produce a light crisp sensation intended
         * to be used repetitively for dynamic feedback.
         *
         * @see VibrationEffect.Composition.PRIMITIVE_TICK
         */
        Tick(VibrationEffect.Composition.PRIMITIVE_TICK),

        /**
         * This very short low frequency effect should produce a light crisp
         * sensation intended to be used repetitively for dynamic feedback.
         *
         * @see VibrationEffect.Composition.PRIMITIVE_LOW_TICK
         */
        @RequiresApi(Build.VERSION_CODES.S)
        LowTick(VibrationEffect.Composition.PRIMITIVE_LOW_TICK);
    }

    /** Represents the type of delay for a haptic primitive in a composition. */
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    enum class DelayType(val id: Int) {
        /**
         * The delay represents a pause in the composition between the end of the
         * previous primitive and the beginning of the next one.
         *
         * The primitive will start after the requested pause after the last
         * primitive ended. The actual time the primitive will be played depends on
         * the previous primitive's actual duration on the device hardware. This
         * enables the combination of primitives to create more complex effects
         * based on how close to each other they'll play.
         *
         * @see VibrationEffect.Composition.DELAY_TYPE_PAUSE
         */
        Pause(VibrationEffect.Composition.DELAY_TYPE_PAUSE),

        /**
         * The delay represents an offset before starting this primitive, relative
         * to the start time of the previous primitive in the composition.
         *
         * The primitive will start at the requested fixed time after the last
         * primitive started, independently of that primitive's actual duration on
         * the device hardware. This enables precise timings of primitives within
         * a composition, ensuring they'll be played at the desired intervals.
         * A primitive will be dropped from the composition if it overlaps with
         * previous ones.
         *
         * @see VibrationEffect.Composition.DELAY_TYPE_RELATIVE_START_OFFSET
         */
        RelativeStartOffset(VibrationEffect.Composition.DELAY_TYPE_RELATIVE_START_OFFSET);
    }

    @DslMarker
    annotation class Dsl
}

/**
 * Creates a [VibrationEffect] using a composed vibration effect.
 *
 * @param block The DSL block to configure the composed vibration effect.
 * @sample samples.dsl.composed
 */
@RequiresApi(Build.VERSION_CODES.R)
inline fun composedVibrationEffect(
    block: ComposedVibrationEffectBuilder.() -> Unit,
): VibrationEffect = ComposedVibrationEffectBuilder().apply(block).build()

/**
 * A builder for creating amplitude vibration effects from a pattern of
 * durations and amplitudes.
 *
 * @property repeat The index at which to repeat the vibration pattern, or
 *   -1 for no repetition.
 */
@AmplitudeVibrationEffectBuilder.Dsl
open class AmplitudeVibrationEffectBuilder(private val repeat: Int = -1) : LockableValueDsl() {
    private var pointer = Duration.ZERO

    /**
     * A list of control points, where each point represents a duration and
     * amplitude for the vibration effect.
     *
     * Each point consists of a [Duration] for the duration and an [Int] for
     * the amplitude. The amplitude must be between 0 and 255.
     */
    val list by list<ControlPoint>(
        beforeSet = { _, it -> beforeSet(it) },
        beforeAccess = {
            if (!isLocked) return@list
            check(any { it.duration > Duration.ZERO }) { "Amplitude effects must have at least one non-zero duration." }
        },
    )

    /**
     * Adds a duration and amplitude pair to the vibration effect.
     *
     * @param amplitude The amplitude value. Must be between 0 and 255.
     * @param duration The duration for which the amplitude is applied.
     */
    fun add(amplitude: Int, duration: Duration) {
        list.add(ControlPoint(amplitude, duration))
        pointer += duration
    }

    /**
     * Adds a control point to the amplitude effect.
     *
     * @param duration The transition time to this control point.
     * @receiver The amplitude value. Must be between 0 and 255.
     */
    infix fun Int.after(duration: Duration) {
        add(this, duration)
    }

    /**
     * Adds a control point to the amplitude effect.
     *
     * @param duration The transition time to this control point.
     * @receiver The amplitude value as a Float. Must be between 0.0 and 1.0.
     */
    infix fun Float.after(duration: Duration) = times(255).roundToInt().coerceIn(0..255).after(duration)

    /**
     * Adds a control point to the amplitude effect at a specific time.
     *
     * **Warning:** For best performance, add points in chronological order
     * and without overlap. Overlapping or out-of-order points can cause
     * significant performance issues.
     *
     * @param time The absolute time at which the effect should reach this
     *   control point.
     * @receiver The amplitude value. Must be between 0 and 255.
     */
    infix fun Int.at(time: Duration) = addControlPoint(
        data = this,
        time = time,
        pointer = pointer,
        list = list,
        setPointer = { pointer = it },
        getDuration = { it.duration },
        copyWithDuration = { point, duration -> point.copy(duration = duration) },
        createControlPoint = { amplitude, duration -> ControlPoint(amplitude, duration) },
    )

    /**
     * Adds a control point to the amplitude effect at a specific time.
     *
     * **Warning:** For best performance, add points in chronological order
     * and without overlap. Overlapping or out-of-order points can cause
     * significant performance issues.
     *
     * @param time The absolute time at which the effect should reach this
     *   control point.
     * @receiver The amplitude value as a Float. Must be between 0.0 and 1.0.
     */
    infix fun Float.at(time: Duration) = times(255).roundToInt().coerceIn(0..255).at(time)

    private fun beforeSet(point: ControlPoint) {
        require(point.amplitude in 0..255) { "Amplitude must be between 0 and 255." }
        require(point.duration > Duration.ZERO) { "Duration must be positive." }
    }

    /**
     * A pre-build hook that can be overridden to perform actions before the
     * effect is built.
     *
     * This method is called before the vibration effect is built, allowing for
     * any necessary setup or validation.
     */
    protected open fun preBuild() {}

    /**
     * Builds the [VibrationEffect] using the current list of durations and
     * amplitudes.
     *
     * @return The constructed [VibrationEffect].
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun build(): VibrationEffect {
        preBuild()
        lock()
        val list = list.toList()
        return VibrationEffect.createWaveform(
            list.map { it.duration.inWholeMilliseconds }.toLongArray(),
            list.map { it.amplitude }.toIntArray(),
            repeat,
        )
    }

    /**
     * Represents a pair of duration and amplitude for the vibration effect.
     *
     * @property duration The duration for which the amplitude is applied.
     * @property amplitude The amplitude value. Must be between 0 and 255.
     */
    data class ControlPoint(
        @field:IntRange(from = 0, to = 255) val amplitude: Int,
        val duration: Duration,
    )

    @DslMarker
    annotation class Dsl
}

/**
 * Creates a [VibrationEffect] from a pattern of durations and amplitudes.
 *
 * @param repeat The index at which to repeat the vibration pattern, or -1
 *   for no repetition.
 * @param block The DSL block to configure the amplitude vibration effect.
 * @sample samples.dsl.amplitude
 */
@RequiresApi(Build.VERSION_CODES.O)
inline fun amplitudeVibrationEffect(
    repeat: Int = -1,
    block: AmplitudeVibrationEffectBuilder.() -> Unit,
): VibrationEffect = AmplitudeVibrationEffectBuilder(repeat).apply(block).build()

/**
 * A builder for creating on-off vibration effects from a pattern of
 * durations.
 *
 * **Warning:** For best performance, add effects in chronological order
 * and without overlap. Overlapping or out-of-order effects can cause
 * significant performance issues.
 *
 * @property repeat The index at which to repeat the vibration pattern, or
 *   -1 for no repetition.
 */
@OnOffVibrationEffectBuilder.Dsl
open class OnOffVibrationEffectBuilder(private val repeat: Int = -1) : LockableValueDsl() {
    private var pointer = Duration.ZERO

    /**
     * The vibration on-off pattern, represented as a list of durations in
     * milliseconds.
     *
     * **Warning:** Direct manipulation of this list should be done with
     * caution. Ensure you understand the impact of your changes when modifying
     * this list.
     */
    val list by list<Long>(
        beforeAccess = {
            if (!isLocked) return@list
            check(isNotEmpty()) { "On-off effects must have at least one effect." }
        },
    )

    /**
     * Adds an [Effect] to the vibration pattern.
     *
     * **Warning:** For best performance, add effects in chronological order
     * and without overlap. Overlapping or out-of-order effects can cause
     * significant performance issues.
     *
     * @param effect The effect to add.
     */
    fun add(effect: Effect) {
        check(list.size % 2 == 0) { "Pattern size should be even." }
        val newStart = effect.start
        val newEnd = effect.end
        if (newStart == newEnd) return
        require(newStart >= Duration.ZERO) { "Effect start time must be non-negative." }
        require(newEnd >= Duration.ZERO) { "Effect end time must be non-negative." }
        require(newStart < newEnd) { "Effect start time must be less than end time." }

        if (newStart > pointer) {
            val offDuration = newStart - pointer
            val onDuration = newEnd - newStart
            list += offDuration.inWholeMilliseconds
            list += onDuration.inWholeMilliseconds
            pointer = newEnd
            return
        }

        if (newStart == pointer && list.isNotEmpty()) {
            val lastIndex = list.lastIndex
            val currentOnTime = list[lastIndex]
            val deltaOnTime = newEnd - pointer
            val newOnTime = currentOnTime + deltaOnTime.inWholeMilliseconds
            list[lastIndex] = newOnTime
            pointer = newEnd
            return
        }

        val effects = mutableListOf(Effect(newStart, newEnd))
        var currentTime = Duration.ZERO

        for (i in list.indices step 2) {
            val offDuration = list[i].milliseconds
            val onDuration = list[i + 1].milliseconds

            val start = currentTime + offDuration
            val end = start + onDuration

            effects += Effect(start, end)
            currentTime = end
        }

        effects.sortBy { it.start }

        val merged = mutableListOf<Effect>()
        var currentStart = effects.first().start
        var currentEnd = effects.first().end

        for ((nextStart, nextEnd) in effects.drop(1)) {
            if (nextStart <= currentEnd) {
                currentEnd = maxOf(currentEnd, nextEnd)
            } else {
                merged += Effect(currentStart, currentEnd)
                currentStart = nextStart
                currentEnd = nextEnd
            }
        }

        merged += Effect(currentStart, currentEnd)

        list.clear()
        currentTime = Duration.ZERO

        for ((start, end) in merged) {
            val offDuration = start - currentTime
            val onDuration = end - start

            list += offDuration.inWholeMilliseconds
            list += onDuration.inWholeMilliseconds

            currentTime = end
        }

        pointer = currentTime
    }

    /**
     * Adds an effect to the vibration pattern using start and end durations.
     *
     * **Warning:** For best performance, add effects in chronological order
     * and without overlap. Overlapping or out-of-order effects can cause
     * significant performance issues.
     *
     * @param start The start time of the effect.
     * @param end The end time of the effect.
     */
    fun range(start: Duration, end: Duration) {
        add(Effect(start, end))
    }

    /**
     * Adds an effect to the vibration pattern from a [ClosedRange] of
     * durations.
     *
     * **Warning:** For best performance, add effects in chronological order
     * and without overlap. Overlapping or out-of-order effects can cause
     * significant performance issues.
     *
     * @param range The range representing the start and end of the effect.
     */
    fun range(range: ClosedRange<Duration>) {
        range(range.start, range.endInclusive)
    }

    /**
     * Adds a pattern of on and off durations to the vibration effect.
     *
     * This method allows you to specify a sequence of off and on durations for
     * the vibration effect. Each pair of durations represents an off duration
     * followed by an on duration.
     *
     * @param off The duration for which the vibration is off. Must be
     *   non-negative.
     * @param on The duration for which the vibration is on. Must be
     *   non-negative.
     */
    fun pattern(off: Duration, on: Duration) {
        require(off >= Duration.ZERO) { "Off duration must be non-negative." }
        require(on >= Duration.ZERO) { "On duration must be non-negative." }
        list += off.inWholeMilliseconds
        list += on.inWholeMilliseconds
    }

    /**
     * A pre-build hook that can be overridden to perform actions before the
     * effect is built.
     *
     * This method is called before the vibration effect is built, allowing for
     * any necessary setup or validation.
     */
    protected open fun preBuild() {}

    /**
     * Builds the [VibrationEffect] from the current pattern.
     *
     * @return The constructed [VibrationEffect].
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun build(): VibrationEffect {
        preBuild()
        lock()
        return VibrationEffect.createWaveform(list.toLongArray(), repeat)
    }

    /**
     * Represents a single on-off effect, with a start and end [Duration].
     *
     * @property start The start time of the effect.
     * @property end The end time of the effect.
     */
    data class Effect(
        val start: Duration,
        val end: Duration,
    )

    @DslMarker
    annotation class Dsl
}

/**
 * Creates a [VibrationEffect] using an on-off pattern.
 *
 * @param repeat The index at which to repeat the vibration pattern, or -1
 *   for no repetition.
 * @param block The DSL block to configure the on-off vibration effect.
 * @sample samples.dsl.onOff
 */
@RequiresApi(Build.VERSION_CODES.O)
inline fun onOffVibrationEffect(repeat: Int = -1, block: OnOffVibrationEffectBuilder.() -> Unit) =
    OnOffVibrationEffectBuilder(repeat).apply(block).build()

/**
 * An extension function to safely retrieve the [Vibrator] service from the
 * [Context].
 *
 * @param block A block to execute with the [Vibrator] service. The service
 *   can be null if it is not available.
 * @return The result of the block.
 * @receiver The [Context] from which to retrieve the [Vibrator] service.
 */
inline fun <R> Context.withVibratorOrNull(block: Vibrator?.() -> R) = getSystemService<Vibrator>().block()

/**
 * An extension function to safely retrieve the [Vibrator] service from the
 * [Context] and execute a block if the service is available.
 *
 * @param failed An action to perform if the [Vibrator] service is not
 *   available.
 * @param block A block to execute with the [Vibrator] service.
 * @return The result of the block, or null if the service is not
 *   available.
 * @receiver The [Context] from which to retrieve the [Vibrator] service.
 */
inline fun <R> Context.tryWithVibrator(failed: () -> Unit = {}, block: Vibrator.() -> R) =
    getSystemService<Vibrator>()?.block() ?: run {
        failed()
        null
    }

internal inline fun <D, C> addControlPoint(
    data: D,
    time: Duration,
    pointer: Duration,
    list: MutableList<C>,
    setPointer: (Duration) -> Unit,
    getDuration: (C) -> Duration,
    copyWithDuration: (C, Duration) -> C,
    createControlPoint: (D, Duration) -> C
) {
    require(time >= Duration.ZERO) { "Control point time must be non-negative." }
    if (time > pointer) {
        list.add(createControlPoint(data, time - pointer))
        setPointer(time)
        return
    }

    var cumulativeTime = Duration.ZERO
    for (i in list.indices) {
        val currentPoint = list[i]
        val nextPointTime = cumulativeTime + getDuration(currentPoint)

        require(time != nextPointTime) { "There is already a control point at $time." }
        if (time > nextPointTime) {
            cumulativeTime = nextPointTime
            continue
        }

        val newPointDuration = time - cumulativeTime
        val subsequentPointNewDuration = nextPointTime - time

        list[i] = copyWithDuration(currentPoint, subsequentPointNewDuration)
        list.add(i, createControlPoint(data, newPointDuration))
        return
    }

    list.add(createControlPoint(data, time - pointer))
    setPointer(time)
}

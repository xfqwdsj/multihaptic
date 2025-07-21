package top.ltfan.multihaptic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import top.ltfan.dslutilities.LockableValueDsl
import top.ltfan.multihaptic.vibrator.Vibrator
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The default duration for haptic effects, used when no specific duration
 * is provided.
 *
 * This value is used as a fallback for haptic effects that do not specify
 * a duration explicitly.
 */
val DefaultDuration = 50.milliseconds

/**
 * Haptic effect that consists of multiple [Primitive]s.
 *
 * @property primitives A list of [Primitive]s that make up this haptic
 *   effect.
 */
data class HapticEffect(val primitives: List<Primitive>) {
    /** Adds a [Primitive] to this haptic effect. */
    operator fun plus(primitive: Primitive) = HapticEffect(primitives + primitive)

    /** Adds another [HapticEffect] effect to this one. */
    operator fun plus(other: HapticEffect) = HapticEffect(primitives + other.primitives)

    /** Adds a [PrimitiveType] as a new [Primitive] to this haptic effect. */
    operator fun plus(type: PrimitiveType) = HapticEffect(primitives + Primitive(type))

    /** Removes a [Primitive] from this haptic effect. */
    operator fun minus(primitive: Primitive) = HapticEffect(primitives - primitive)

    /** Removes another [HapticEffect] effect from this one. */
    operator fun minus(other: HapticEffect) = HapticEffect(primitives - other.primitives)

    /**
     * Removes all [Primitive]s of a specific [PrimitiveType] from this haptic
     * effect.
     */
    operator fun minus(type: PrimitiveType) =
        HapticEffect(primitives.filter { it.basic !is BasicPrimitive.Predefined || it.basic.type != type })

    /**
     * Builder for creating [HapticEffect] instances.
     *
     * @property primitives List of [Primitive]s in the composition.
     */
    @Builder.Dsl
    class Builder : LockableValueDsl() {
        val primitives by list<Primitive>(
            beforeSet = { _, primitive ->
                require(primitive.delay >= Duration.ZERO) { "Primitive delay must be non-negative, but was ${primitive.delay}." }
            },
        )

        /**
         * Adds a [Primitive] to the composition with specified type, scale, delay,
         * and delay type.
         *
         * @param type The [PrimitiveType] of the primitive haptic effect.
         * @param scale The scale factor for the haptic effect, default is 1f.
         * @param delay The delay before the haptic effect starts, default is
         *   [Duration.ZERO].
         * @param delayType The [DelayType] of delay, default is [DelayType.Pause].
         */
        fun predefined(
            type: PrimitiveType,
            scale: Float = 1f,
            delay: Duration = Duration.ZERO,
            delayType: DelayType = DelayType.Pause
        ) {
            primitives.add(Primitive(type, scale, delay, delayType))
        }

        /**
         * Adds a [Primitive] to the composition using a builder block.
         *
         * @param type The [PrimitiveType] of the primitive haptic effect.
         * @param block The block to configure the [PredefinedBuilder].
         */
        inline fun predefined(type: PrimitiveType, block: PredefinedBuilder.() -> Unit = {}) {
            primitives.add(PredefinedBuilder(type).apply(block).build())
        }

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.Click] to the
         * composition with default parameters.
         */
        val click inline get() = click()

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.Click] to the
         * composition.
         *
         * @param block The block to configure the [PredefinedBuilder].
         */
        inline fun click(block: PredefinedBuilder.() -> Unit = {}) {
            predefined(PrimitiveType.Click, block)
        }

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.Thud] to the
         * composition with default parameters.
         */
        val thud inline get() = thud()

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.Thud] to the
         * composition.
         *
         * @param block The block to configure the [PredefinedBuilder].
         */
        inline fun thud(block: PredefinedBuilder.() -> Unit = {}) {
            predefined(PrimitiveType.Thud, block)
        }

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.Spin] to the
         * composition with default parameters.
         */
        val spin inline get() = spin()

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.Spin] to the
         * composition.
         *
         * @param block The block to configure the [PredefinedBuilder].
         */
        inline fun spin(block: PredefinedBuilder.() -> Unit = {}) {
            predefined(PrimitiveType.Spin, block)
        }

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.QuickRise] to the
         * composition with default parameters.
         */
        val quickRise inline get() = quickRise()

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.QuickRise] to the
         * composition.
         *
         * @param block The block to configure the [PredefinedBuilder].
         */
        inline fun quickRise(block: PredefinedBuilder.() -> Unit = {}) {
            predefined(PrimitiveType.QuickRise, block)
        }

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.SlowRise] to the
         * composition with default parameters.
         */
        val slowRise inline get() = slowRise()

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.SlowRise] to the
         * composition.
         *
         * @param block The block to configure the [PredefinedBuilder].
         */
        inline fun slowRise(block: PredefinedBuilder.() -> Unit = {}) {
            predefined(PrimitiveType.SlowRise, block)
        }

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.QuickFall] to the
         * composition with default parameters.
         */
        val quickFall inline get() = quickFall()

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.QuickFall] to the
         * composition.
         *
         * @param block The block to configure the [PredefinedBuilder].
         */
        inline fun quickFall(block: PredefinedBuilder.() -> Unit = {}) {
            predefined(PrimitiveType.QuickFall, block)
        }

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.Tick] to the
         * composition with default parameters.
         */
        val tick inline get() = tick()

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.Tick] to the
         * composition.
         *
         * @param block The block to configure the [PredefinedBuilder].
         */
        inline fun tick(block: PredefinedBuilder.() -> Unit = {}) {
            predefined(PrimitiveType.Tick, block)
        }

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.LowTick] to the
         * composition with default parameters.
         */
        val lowTick inline get() = lowTick()

        /**
         * Adds a predefined [Primitive] of type [PrimitiveType.LowTick] to the
         * composition.
         *
         * @param block The block to configure the [PredefinedBuilder].
         */
        inline fun lowTick(block: PredefinedBuilder.() -> Unit = {}) {
            predefined(PrimitiveType.LowTick, block)
        }

        /**
         * Adds a custom [Primitive] to the composition using a builder block.
         *
         * @param duration The duration of the custom haptic effect, default is
         *   [DefaultDuration].
         * @param block The block to configure the [CustomBuilder].
         */
        inline fun custom(duration: Duration = DefaultDuration, block: CustomBuilder.() -> Unit) {
            primitives.add(CustomBuilder(duration).apply(block).build())
        }

        /**
         * Builder for predefined haptic primitives.
         *
         * Allows configuration of a predefined [Primitive] using DSL style.
         *
         * @property type The [PrimitiveType] of the primitive haptic effect.
         * @property scale Intensity scaling factor, default is 1f.
         * @property delay Delay before the primitive starts, default is
         *   [Duration.ZERO].
         * @property delayType Type of delay, default is [DelayType.Pause].
         */
        @Dsl
        class PredefinedBuilder(val type: PrimitiveType) : LockableValueDsl() {
            var scale: Float by prepared(1f)
            var delay: Duration by prepared(Duration.ZERO)
            var delayType: DelayType by prepared(DelayType.Pause)

            /**
             * Builds the [Primitive] instance.
             *
             * @return The constructed [Primitive] object.
             */
            fun build(): Primitive {
                lock()
                return Primitive(type, scale, delay, delayType)
            }
        }

        /**
         * Builder for custom haptic primitives.
         *
         * Allows configuration of a custom [Primitive] using DSL style.
         *
         * @property duration The duration of the custom haptic effect, default is
         *   [DefaultDuration].
         * @property scale Intensity scaling factor, default is 1f.
         * @property fallback Optional fallback [PrimitiveType] if custom curves
         *   are not supported.
         * @property curves The [HapticCurves] defining intensity and sharpness
         *   over time. Required.
         * @property delay Delay before the primitive starts, default is
         *   [Duration.ZERO].
         * @property delayType Type of delay, default is [DelayType.Pause].
         */
        @Dsl
        class CustomBuilder(val duration: Duration = DefaultDuration) : LockableValueDsl() {
            var scale: Float by prepared(1f)
            var fallback: PrimitiveType? by prepared(null)
            var curves: HapticCurves by required()
            var delay: Duration by prepared(Duration.ZERO)
            var delayType: DelayType by prepared(DelayType.Pause)

            /**
             * Configures the [curves] property using a builder block.
             *
             * If you do not provide intensity or sharpness keyframes, we will add a
             * default keyframe at time 0 with value 1f.
             *
             * @param block The block to configure the [HapticCurves.Builder].
             */
            inline fun curves(block: HapticCurves.Builder.() -> Unit) {
                curves = HapticCurves.Builder().apply(block).build()
            }

            /** Uses the [PrimitiveType.Click] as a fallback for this custom primitive. */
            val clickFallback: Unit
                inline get() {
                    fallback = PrimitiveType.Click
                }

            /** Uses the [PrimitiveType.Thud] as a fallback for this custom primitive. */
            val thudFallback: Unit
                inline get() {
                    fallback = PrimitiveType.Thud
                }

            /** Uses the [PrimitiveType.Spin] as a fallback for this custom primitive. */
            val spinFallback: Unit
                inline get() {
                    fallback = PrimitiveType.Spin
                }

            /**
             * Uses the [PrimitiveType.QuickRise] as a fallback for this custom
             * primitive.
             */
            val quickRiseFallback: Unit
                inline get() {
                    fallback = PrimitiveType.QuickRise
                }

            /**
             * Uses the [PrimitiveType.SlowRise] as a fallback for this custom
             * primitive.
             */
            val slowRiseFallback: Unit
                inline get() {
                    fallback = PrimitiveType.SlowRise
                }

            /**
             * Uses the [PrimitiveType.QuickFall] as a fallback for this custom
             * primitive.
             */
            val quickFallFallback: Unit
                inline get() {
                    fallback = PrimitiveType.QuickFall
                }

            /** Uses the [PrimitiveType.Tick] as a fallback for this custom primitive. */
            val tickFallback: Unit
                inline get() {
                    fallback = PrimitiveType.Tick
                }

            /**
             * Uses the [PrimitiveType.LowTick] as a fallback for this custom
             * primitive.
             */
            val lowTickFallback: Unit
                inline get() {
                    fallback = PrimitiveType.LowTick
                }

            /**
             * Builds the [Primitive] instance using the configured properties.
             *
             * @return The constructed [Primitive] object.
             */
            fun build(): Primitive {
                lock()
                return Primitive(BasicPrimitive.Custom(duration, curves, scale, fallback), delay, delayType)
            }
        }

        /**
         * Builds the haptic effect.
         *
         * @return The built [HapticEffect] instance.
         */
        fun build(): HapticEffect {
            lock()
            return HapticEffect(primitives.toList())
        }

        @DslMarker
        internal annotation class Dsl
    }
}

/**
 * Creates a new haptic effect using the provided [HapticEffect.Builder]
 * block.
 *
 * @param block The block to configure the [HapticEffect.Builder].
 * @return A new haptic effect instance with the specified primitives.
 * @sample samples.effect.buildHapticEffect
 */
fun HapticEffect(block: HapticEffect.Builder.() -> Unit) = HapticEffect.Builder().apply(block).build()

/**
 * Represents the basic primitive for a haptic effect. Can be either a
 * predefined primitive or a custom primitive.
 *
 * @property scale The intensity scaling factor for the primitive.
 */
sealed class BasicPrimitive {
    /** The intensity scaling factor for the primitive. */
    abstract val scale: Float

    /**
     * Represents a predefined haptic primitive.
     *
     * @property type The type of the predefined primitive.
     * @property scale The intensity scaling factor, default is 1f.
     */
    data class Predefined(
        val type: PrimitiveType,
        override val scale: Float = 1f,
    ) : BasicPrimitive()

    /**
     * Represents a custom haptic primitive.
     *
     * @property duration The duration of the custom haptic effect.
     * @property curves The haptic curves for intensity and sharpness.
     * @property scale The intensity scaling factor, default is 1f.
     * @property fallback Optional fallback primitive type if custom curves are
     *   not supported.
     */
    data class Custom(
        val duration: Duration = DefaultDuration,
        val curves: HapticCurves,
        override val scale: Float = 1f,
        val fallback: PrimitiveType? = null,
    ) : BasicPrimitive()
}

/**
 * Represents a haptic primitive, which can be either predefined or custom.
 *
 * @property basic The basic primitive type, either predefined or custom.
 * @property delay The delay before the primitive starts, default is
 *   [Duration.ZERO].
 * @property delayType The type of delay, default is [DelayType.Pause].
 */
data class Primitive(
    val basic: BasicPrimitive,
    val delay: Duration = Duration.ZERO,
    val delayType: DelayType = DelayType.Pause,
) {
    constructor(
        type: PrimitiveType,
        scale: Float = 1f,
        delay: Duration = Duration.ZERO,
        delayType: DelayType = DelayType.Pause,
    ) : this(BasicPrimitive.Predefined(type, scale), delay, delayType)
}

/**
 * Represents different types of primitive haptic effects with their
 * associated durations.
 *
 * Each type has a predefined duration that determines how long the
 * vibration lasts.
 *
 * @property duration The [Duration] of the haptic effect.
 */
enum class PrimitiveType(val duration: Duration) {
    /** This effect should produce a sharp, crisp click sensation. */
    Click(50.milliseconds),

    /**
     * A haptic effect that simulates downwards movement with gravity. Often
     * followed by extra energy of hitting and reverberation to augment
     * physicality.
     */
    Thud(80.milliseconds),

    /** A haptic effect that simulates spinning momentum. */
    Spin(120.milliseconds),

    /** A haptic effect that simulates quick upward movement against gravity. */
    QuickRise(50.milliseconds),

    /** A haptic effect that simulates slow upward movement against gravity. */
    SlowRise(200.milliseconds),

    /** A haptic effect that simulates quick downwards movement with gravity. */
    QuickFall(60.milliseconds),

    /**
     * This very short effect should produce a light crisp sensation intended
     * to be used repetitively for dynamic feedback.
     */
    Tick(10.milliseconds),

    /**
     * This very short low frequency effect should produce a light crisp
     * sensation intended to be used repetitively for dynamic feedback.
     */
    LowTick(20.milliseconds);
}

/** Represents the type of delay for a haptic primitive in a composition. */
enum class DelayType {
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
     * @sample samples.primitive.delayTypePauseExample
     */
    Pause,

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
     * The sample be performed on the device as follows:
     * ```
     *  0ms               20ms                     100ms
     *  PRIMITIVE_CLICK---PRIMITIVE_TICK-----------PRIMITIVE_THUD
     * ```
     *
     * @sample samples.primitive.delayTypeRelativeStartOffsetExample
     */
    RelativeStartOffset;
}

/**
 * Represents a set of haptic curves.
 *
 * The keyframes built by the builder are both non-empty.
 */
data class HapticCurves(
    val intensity: List<Keyframe>,
    val sharpness: List<Keyframe>,
) {
    /**
     * Represents a keyframe in a haptic curve.
     *
     * @property time The time at which this keyframe occurs.
     * @property value The value at the specified time.
     */
    data class Keyframe(val time: Duration, val value: Float)

    /**
     * Builder for constructing `HapticCurves` instances.
     *
     * Provides methods to add intensity and sharpness keyframes for haptic
     * curves. Use DSL-style blocks for convenient curve definition.
     *
     * @property intensityFrames List of intensity keyframes.
     * @property sharpnessFrames List of sharpness keyframes.
     */
    @Builder.Dsl
    class Builder : LockableValueDsl() {
        val intensityFrames by list<Keyframe>(
            beforeSet = { _, frame ->
                require(frame.time >= Duration.ZERO) { "Keyframe time must be non-negative, but was ${frame.time}." }
                require(frame.value in 0f..1f) { "Keyframe value must be between 0 and 1, but was ${frame.value}." }
            },
        )

        val sharpnessFrames by list<Keyframe>(
            beforeSet = { _, frame ->
                require(frame.time >= Duration.ZERO) { "Keyframe time must be non-negative, but was ${frame.time}." }
                require(frame.value in 0f..1f) { "Keyframe value must be between 0 and 1, but was ${frame.value}." }
            },
        )

        /**
         * Adds an intensity keyframe.
         *
         * @param frame The keyframe to add.
         */
        fun intensity(frame: Keyframe) {
            intensityFrames.add(frame)
        }

        /**
         * Adds an intensity keyframe with specified time and value.
         *
         * @param time The time of the keyframe.
         * @param value The value at the specified time.
         */
        fun intensity(time: Duration, value: Float) {
            intensity(Keyframe(time, value))
        }

        /**
         * Adds an intensity keyframe at the start of the curve.
         *
         * @param value The value at the current time.
         */
        fun intensity(value: Float = 1f) {
            intensity(Duration.ZERO, value)
        }

        /**
         * Adds multiple intensity keyframes from a collection.
         *
         * @param frames The collection of keyframes to add.
         */
        fun intensity(frames: Collection<Keyframe>) {
            intensityFrames.addAll(frames)
        }

        /**
         * Adds multiple intensity keyframes.
         *
         * @param frames The keyframes to add.
         */
        fun intensity(vararg frames: Keyframe) {
            intensityFrames.addAll(frames)
        }

        /**
         * DSL block to add intensity keyframes.
         *
         * @param block The block to configure intensity keyframes.
         */
        inline fun intensity(block: Keyframes.() -> Unit) {
            intensity(Keyframes().apply(block).build())
        }

        /**
         * Adds a sharpness keyframe.
         *
         * @param frame The keyframe to add.
         */
        fun sharpness(frame: Keyframe) {
            sharpnessFrames.add(frame)
        }

        /**
         * Adds a sharpness keyframe with specified time and value.
         *
         * @param time The time of the keyframe.
         * @param value The value at the specified time.
         */
        fun sharpness(time: Duration, value: Float) {
            sharpness(Keyframe(time, value))
        }

        /**
         * Adds a sharpness keyframe at the start of the curve.
         *
         * @param value The value at the current time.
         */
        fun sharpness(value: Float = 1f) {
            sharpness(Duration.ZERO, value)
        }

        /**
         * Adds multiple sharpness keyframes from a collection.
         *
         * @param frames The collection of keyframes to add.
         */
        fun sharpness(frames: Collection<Keyframe>) {
            sharpnessFrames.addAll(frames)
        }

        /**
         * Adds multiple sharpness keyframes.
         *
         * @param frames The keyframes to add.
         */
        fun sharpness(vararg frames: Keyframe) {
            sharpnessFrames.addAll(frames)
        }

        /**
         * DSL block to add sharpness keyframes.
         *
         * @param block The block to configure sharpness keyframes.
         */
        inline fun sharpness(block: Keyframes.() -> Unit) {
            sharpness(Keyframes().apply(block).build())
        }

        /**
         * Builder for a list of [Keyframe]s used in haptic curves.
         *
         * Provides DSL-style methods to add keyframes for intensity or sharpness
         * curves.
         *
         * @property keyframes List of all [Keyframe]s.
         */
        @Dsl
        class Keyframes : LockableValueDsl() {
            val keyframes by list<Keyframe>(
                beforeSet = { _, frame ->
                    require(frame.time >= Duration.ZERO) { "Keyframe time must be non-negative, but was ${frame.time}." }
                    require(frame.value in 0f..1f) { "Keyframe value must be between 0 and 1, but was ${frame.value}." }
                },
            )

            /**
             * Adds a [Keyframe] with the specified time and value.
             *
             * @param time The time of the keyframe.
             * @param value The value at the specified time.
             */
            fun add(time: Duration, value: Float) {
                keyframes.add(Keyframe(time, value))
            }

            /**
             * Adds a [Keyframe] using a builder block.
             *
             * @param block The block to configure the [KeyframeBuilder].
             */
            inline fun add(block: KeyframeBuilder.() -> Unit) {
                keyframes.add(KeyframeBuilder().apply(block).build())
            }

            /**
             * Adds all [Keyframe]s from a collection.
             *
             * @param frames The collection of [Keyframe]s to add.
             */
            fun addAll(frames: Collection<Keyframe>) {
                keyframes.addAll(frames)
            }

            /**
             * Adds all [Keyframe]s from the given arguments.
             *
             * @param frames The [Keyframe]s to add.
             */
            fun addAll(vararg frames: Keyframe) {
                keyframes.addAll(frames)
            }

            /** DSL operator to add a [Keyframe] using unary plus. */
            operator fun Keyframe.unaryPlus() {
                keyframes.add(this)
            }

            /** DSL operator to add a collection of [Keyframe]s using unary plus. */
            operator fun Collection<Keyframe>.unaryPlus() {
                addAll(this)
            }

            /**
             * DSL infix function to add a [Keyframe] with a value at a specific time.
             *
             * @param time The time of the keyframe.
             * @receiver The value of the keyframe.
             */
            infix fun Float.at(time: Duration) {
                add(time, this)
            }

            /**
             * Builds and returns the list of [Keyframe]s.
             *
             * @return The list of [Keyframe]s added to the builder.
             */
            fun build(): List<Keyframe> {
                lock()
                return keyframes.toList()
            }
        }

        /**
         * Builder for a single [HapticCurves.Keyframe].
         *
         * Allows configuration of the time and value for a keyframe in a haptic
         * curve.
         *
         * @property time The time at which the keyframe occurs.
         * @property value The value at the specified time.
         */
        @Dsl
        class KeyframeBuilder : LockableValueDsl() {
            var time: Duration by required()
            var value: Float by required()

            /**
             * Builds a [HapticCurves.Keyframe] instance using the configured
             * properties.
             *
             * @return The constructed [Keyframe] object.
             */
            fun build(): Keyframe {
                lock()
                return Keyframe(time, value)
            }
        }

        /**
         * Builds the [HapticCurves] instance using the configured keyframes.
         *
         * Validates that all time values are non-negative.
         *
         * @return The constructed [HapticCurves] object.
         */
        fun build(): HapticCurves {
            if (intensityFrames.isEmpty()) intensity()
            if (sharpnessFrames.isEmpty()) sharpness()
            lock()
            val intensityFrames = intensityFrames.toList()
            val sharpnessFrames = sharpnessFrames.toList()
            return HapticCurves(intensityFrames, sharpnessFrames)
        }

        @DslMarker
        internal annotation class Dsl
    }
}

/**
 * Factory function to get a [Vibrator] instance.
 *
 * Warning: This is a low-level API and need additional configuration
 * in specific platforms. Consider using the vibrator with
 * Compose Multiplatform API from `multihaptic-compose` module.
 */
expect fun getVibrator(coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default), config: Any?): Vibrator

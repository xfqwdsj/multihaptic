package platform.CoreHaptics.dsl

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreHaptics.*
import platform.apple.dsl.runThrowing
import top.ltfan.dslutilities.LockableValueDsl
import top.ltfan.dslutilities.ValueDsl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The default duration for a continuous haptic event.
 *
 * This is set to 50 milliseconds, which is a common duration for short
 * haptic feedback events.
 */
val DefaultDuration = 50.milliseconds

/**
 * A Kotlin definition of a haptic event of the Core Haptics framework.
 *
 * Documentation:
 * [CHHapticEvent](https://developer.apple.com/documentation/corehaptics/chhapticevent),
 * [CHHapticEventParameter](https://developer.apple.com/documentation/corehaptics/chhapticeventparameter),
 *
 * @see CHHapticEvent
 * @see CHHapticEventParameter
 */
sealed class HapticEvent {
    /**
     * The strength of a haptic event.
     *
     * This parameter maps to the haptic pattern’s amplitude or strength. Its
     * value ranges from 0.0 (weak) to 1.0 (strong). Think of intensity as the
     * volume of a haptic pattern, indicating how impactful it feels in the
     * user’s hand. The higher the haptic intensity, the stronger the resulting
     * haptic.
     *
     * Documentation:
     * [hapticIntensity](https://developer.apple.com/documentation/corehaptics/chhapticevent/parameterid/hapticintensity)
     *
     * @see CHHapticEventParameterIDHapticIntensity
     */
    abstract val hapticIntensity: Float?

    /**
     * The feel of a haptic event.
     *
     * Specify a pattern’s sharpness by setting this value from 0.0 to 1.0.
     * Haptic patterns with low sharpness have a round and organic feel,
     * whereas haptic patterns with high sharpness feel more crisp and precise.
     *
     * Documentation:
     * [hapticSharpness](https://developer.apple.com/documentation/corehaptics/chhapticevent/parameterid/hapticsharpness)
     *
     * @see CHHapticEventParameterIDHapticSharpness
     */
    abstract val hapticSharpness: Float?

    /**
     * The start time of the event, relative to other events in the same
     * pattern.
     *
     * A relativeTime of zero indicates immediate playback. Another haptic
     * event starting one second later would have a relativeTime of 1.
     *
     * Documentation:
     * [relativeTime](https://developer.apple.com/documentation/corehaptics/chhapticevent/relativetime)
     *
     * @see CHHapticEvent
     */
    abstract val relativeTime: Duration

    /**
     * A brief impulse occurring at a specific point in time, like the feedback
     * from toggling a switch.
     *
     * Documentation:
     * [hapticTransient](https://developer.apple.com/documentation/corehaptics/chhapticevent/eventtype/haptictransient)
     *
     * @see CHHapticEventTypeHapticTransient
     */
    data class Transient(
        override val hapticIntensity: Float? = null,
        override val hapticSharpness: Float? = null,
        override val relativeTime: Duration = Duration.ZERO,
    ) : HapticEvent() {
        override fun build() = CHHapticEvent(
            eventType = CHHapticEventTypeHapticTransient,
            parameters = parameters,
            relativeTime = relativeTime.seconds,
        )
    }

    /**
     * A haptic event with a looped waveform of arbitrary length.
     *
     * Continuous haptic patterns, like the sustained vibration from a
     * ringtone, take the form of lengthier feedback over a period of time.
     * You must provide continuous events with a duration to determine their
     * endpoint. The maximum duration of a continuous haptic event is 30
     * seconds.
     *
     * Documentation:
     * [hapticContinuous](https://developer.apple.com/documentation/corehaptics/chhapticevent/eventtype/hapticcontinuous)
     *
     * @see CHHapticEventTypeHapticContinuous
     */
    data class Continuous(
        override val hapticIntensity: Float? = null,
        override val hapticSharpness: Float? = null,
        override val relativeTime: Duration = Duration.ZERO,

        /**
         * The duration of the haptic event.
         *
         * The maximum duration of a continuous haptic event is 30 seconds.
         *
         * Documentation:
         * [duration](https://developer.apple.com/documentation/corehaptics/chhapticevent/duration)
         *
         * @see CHHapticEvent
         */
        val duration: Duration = DefaultDuration,

        /**
         * The time at which a haptic pattern’s intensity begins increasing.
         *
         * This parameter can be an event parameter or a dynamic parameter. An
         * event parameter indicates that the haptic begins increasing in intensity
         * at the set time, where time 0 indicates now, or the current time.
         *
         * A dynamic value indicates that the time at which ramp-up begins can
         * change. For example, a value of 0 indicates that the attack time is
         * at its default value. Positive values up to 1.0 increase the attack
         * time exponentially, while negative values down to -1.0 decrease the
         * attack time exponentially. Haptic intensity responds to this parameter.
         *
         * Documentation:
         * [attackTime](https://developer.apple.com/documentation/corehaptics/chhapticevent/parameterid/attacktime)
         *
         * @see CHHapticEventParameterIDAttackTime
         */
        val attackTime: Duration? = null,

        /**
         * The time at which a haptic pattern’s intensity begins decreasing.
         *
         * This parameter can be an event parameter or a dynamic parameter. A fixed
         * value indicates that the haptic begins decreasing in intensity at the
         * set time, where time 0 indicates now, or the current time.
         *
         * A dynamic value indicates that the start time of the decrease can
         * change. For example, a value of 0 indicates that the decay time is at
         * its default value. Positive values up to 1.0 increase the decay time
         * exponentially, while negative values down to -1.0 decrease the decay
         * time exponentially.
         *
         * Haptic intensity responds to this parameter. For example, the following
         * graphic shows the intensity of a haptic pattern in gray. At the
         * beginning, the haptic pattern’s intensity increases from zero to its
         * final value over a certain amount of time; this duration is called the
         * attack. As the haptic pattern reaches its end, the intensity gradually
         * transitions to zero over a certain amount of time; this duration is
         * called the decay.
         *
         * Documentation:
         * [decayTime](https://developer.apple.com/documentation/corehaptics/chhapticevent/parameterid/decaytime)
         *
         * @see CHHapticEventParameterIDDecayTime
         */
        val decayTime: Duration? = null,

        /**
         * The time at which to begin fading the haptic pattern.
         *
         * Specify the release time relative to the current time (t = 0), in
         * seconds. It indicates when the pattern’s decay process begins. Its value
         * ranges from 0 to 1, with a default value of 0.
         *
         * Documentation:
         * [releaseTime](https://developer.apple.com/documentation/corehaptics/chhapticevent/parameterid/releasetime)
         *
         * @see CHHapticEventParameterIDReleaseTime
         */
        val releaseTime: Duration = Duration.ZERO,

        /**
         * A Boolean value that indicates whether to sustain a haptic event for its
         * specified duration.
         *
         * This parameter is an event parameter. It determines whether or not the
         * haptic continues playing at full strength after attack has finished, and
         * before decay begins.
         *
         * If true, the engine sustains the haptic pattern throughout its specified
         * duration, increasing only during its attackTime, and decreasing only
         * after its decayTime. If false, the haptic doesn’t stay at full strength
         * between attack and decay, tailing off even before its decay has begun.
         *
         * Documentation:
         * [sustained](https://developer.apple.com/documentation/corehaptics/chhapticevent/parameterid/sustained)
         *
         * @see CHHapticEventParameterIDSustained
         */
        val sustained: Boolean = false,
    ) : HapticEvent() {
        override fun build() = CHHapticEvent(
            eventType = CHHapticEventTypeHapticContinuous,
            parameters = parameters {
                attackTime?.let { CHHapticEventParameterIDAttackTime to it.seconds }
                decayTime?.let { CHHapticEventParameterIDDecayTime to it.seconds }
                CHHapticEventParameterIDReleaseTime to releaseTime.seconds
                CHHapticEventParameterIDSustained to if (sustained) 1f else 0f
            },
            relativeTime = relativeTime.seconds,
            duration = duration.seconds,
        )
    }

    internal val parameters inline get() = parameters()
    internal inline fun parameters(block: ParametersBuilder.() -> Unit = {}) = ParametersBuilder().apply(block).build()

    /**
     * Builds and returns a corresponding [CHHapticEvent] instance based on the
     * properties of the concrete [HapticEvent] subclass.
     *
     * @return A [CHHapticEvent] representing this haptic event.
     */
    abstract fun build(): CHHapticEvent

    @ParametersBuilder.Dsl
    internal class ParametersBuilder {
        private val _parameters = mutableListOf<CHHapticEventParameter>()

        /**
         * A list of [CHHapticEventParameter]s that will be used to build the
         * [CHHapticEvent].
         *
         * This list is mutable and can be modified using the infix function [to]
         * to add parameters.
         */
        val parameters get() = _parameters.toList()

        /**
         * Adds a [CHHapticEventParameter] to the internal parameter list using the
         * given parameter ID and value.
         *
         * @param value The parameter value as [Float].
         * @receiver The parameter ID as a nullable [String].
         */
        infix fun String?.to(value: Float) = _parameters.add(CHHapticEventParameter(this, value))

        /**
         * Builds a list of [CHHapticEventParameter]s for the current
         * [HapticEvent].
         *
         * This function collects the haptic intensity and sharpness parameters (if
         * present) and adds them as [CHHapticEventParameter]s. It then appends any
         * additional parameters that have been added to the builder.
         *
         * @return A list of [CHHapticEventParameter]s representing all parameters
         *   for the event.
         * @receiver event The [HapticEvent] context for which parameters are being
         *   built.
         */
        context(event: HapticEvent) fun build() = buildList {
            event.hapticIntensity?.let {
                add(CHHapticEventParameter(CHHapticEventParameterIDHapticIntensity, it))
            }
            event.hapticSharpness?.let {
                add(CHHapticEventParameter(CHHapticEventParameterIDHapticSharpness, it))
            }
            addAll(parameters)
        }

        @DslMarker
        annotation class Dsl
    }
}

/**
 * A curve that you send to a haptic pattern player to alter a property
 * value gradually during playback.
 *
 * Parameter curves serve the same purpose as dynamic parameters in
 * that they alter a property value during playback. Unlike dynamic
 * parameters, which change a property value instantaneously, parameter
 * curves interpolate linearly between parameter values to ensure a smooth
 * transition.
 *
 * For example, a parameter curven'tr haptic intensity modulates the
 * intensity over time, ensuring a smooth transition between the current
 * intensity and the upcoming one. Parameter curves apply to all events
 * in a pattern; it isn’t possible to apply one to only a single event.
 *
 * Documentation:
 * [CHHapticParameterCurve](https://developer.apple.com/documentation/corehaptics/chhapticparametercurve)
 *
 * @see CHHapticParameterCurve
 */
sealed class HapticParameterCurve {
    /**
     * A list containing the curve’s control points.
     *
     * Documentation:
     * [controlPoints](https://developer.apple.com/documentation/corehaptics/chhapticparametercurve/controlpoints)
     *
     * @see CHHapticParameterCurve
     */
    abstract val controlPoints: List<ControlPoint>

    /**
     * The time at which this parameter curve is applied, relative to the start
     * time of the pattern.
     *
     * Documentation:
     * [relativeTime](https://developer.apple.com/documentation/corehaptics/chhapticparametercurve/relativetime)
     *
     * @see CHHapticParameterCurve
     */
    abstract val relativeTime: Duration

    /**
     * A dynamic parameter to change the strength of a haptic pattern.
     *
     * Documentation:
     * [hapticIntensityControl](https://developer.apple.com/documentation/corehaptics/chhapticdynamicparameter/id/hapticintensitycontrol)
     *
     * @see HapticEvent.hapticIntensity
     * @see CHHapticDynamicParameterIDHapticIntensityControl
     */
    data class Intensity(
        override val controlPoints: List<ControlPoint>,
        override val relativeTime: Duration = Duration.ZERO,
    ) : HapticParameterCurve() {
        override val parameterID = CHHapticDynamicParameterIDHapticIntensityControl
    }

    /**
     * A dynamic parameter to change the sharpness of a haptic pattern.
     *
     * Documentation:
     * [hapticSharpnessControl](https://developer.apple.com/documentation/corehaptics/chhapticdynamicparameter/id/hapticsharpnesscontrol)
     *
     * @see HapticEvent.hapticSharpness
     * @see CHHapticDynamicParameterIDHapticSharpnessControl
     */
    data class Sharpness(
        override val controlPoints: List<ControlPoint>,
        override val relativeTime: Duration = Duration.ZERO,
    ) : HapticParameterCurve() {
        override val parameterID = CHHapticDynamicParameterIDHapticSharpnessControl
    }

    /**
     * A dynamic parameter to change the time when a haptic pattern’s intensity
     * begins increasing.
     *
     * Documentation:
     * [hapticAttackTimeControl](https://developer.apple.com/documentation/corehaptics/chhapticdynamicparameter/id/hapticattacktimecontrol)
     *
     * @see HapticEvent.Continuous.attackTime
     * @see CHHapticDynamicParameterIDHapticAttackTimeControl
     */
    data class AttackTime(
        override val controlPoints: List<ControlPoint>,
        override val relativeTime: Duration = Duration.ZERO,
    ) : HapticParameterCurve() {
        override val parameterID = CHHapticDynamicParameterIDHapticAttackTimeControl
    }

    /**
     * A dynamic parameter to change the time when a haptic pattern’s intensity
     * begins decreasing.
     *
     * Documentation:
     * [hapticDecayTimeControl](https://developer.apple.com/documentation/corehaptics/chhapticdynamicparameter/id/hapticdecaytimecontrol)
     *
     * @see HapticEvent.Continuous.decayTime
     * @see CHHapticDynamicParameterIDHapticDecayTimeControl
     */
    data class DecayTime(
        override val controlPoints: List<ControlPoint>,
        override val relativeTime: Duration = Duration.ZERO,
    ) : HapticParameterCurve() {
        override val parameterID = CHHapticDynamicParameterIDHapticDecayTimeControl
    }

    /**
     * A dynamic parameter to change the time at which to begin fading the
     * haptic pattern.
     *
     * Documentation:
     * [hapticReleaseTimeControl](https://developer.apple.com/documentation/corehaptics/chhapticdynamicparameter/id/hapticreleasetimecontrol)
     *
     * @see HapticEvent.Continuous.releaseTime
     * @see CHHapticDynamicParameterIDHapticReleaseTimeControl
     */
    data class ReleaseTime(
        override val controlPoints: List<ControlPoint>,
        override val relativeTime: Duration = Duration.ZERO,
    ) : HapticParameterCurve() {
        override val parameterID = CHHapticDynamicParameterIDHapticReleaseTimeControl
    }

    /**
     * A single control point in a parameter curve.
     *
     * Documentation:
     * [CHHapticParameterCurve.ControlPoint](https://developer.apple.com/documentation/corehaptics/chhapticparametercurve/controlpoint)
     *
     * @see CHHapticParameterCurveControlPoint
     */
    data class ControlPoint(
        /**
         * The time at which the associated parameter reaches this value, relative
         * to the start time of the parameter curve.
         *
         * Think of the time as the control point’s x-coordinate.
         *
         * Documentation:
         * [relativeTime](https://developer.apple.com/documentation/corehaptics/chhapticparametercurve/controlpoint/relativetime)
         */
        val relativeTime: Duration,

        /**
         * The parameter value of the point.
         *
         * Think of the value as the control point’s y-coordinate.
         *
         * Note: The range of possible values varies between different parameters.
         *
         * Documentation:
         * [value](https://developer.apple.com/documentation/corehaptics/chhapticparametercurve/controlpoint/value)
         *
         * @see HapticParameterCurve
         * @see CHHapticParameterCurveControlPoint
         */
        val value: Float
    ) {
        /**
         * Builds and returns a corresponding [CHHapticParameterCurveControlPoint]
         * instance based on the properties of this [ControlPoint].
         *
         * @return A [CHHapticParameterCurveControlPoint] representing this control
         *   point in the parameter curve.
         */
        fun build() = CHHapticParameterCurveControlPoint(
            relativeTime = relativeTime.seconds, value = value
        )
    }

    /**
     * The parameter ID defining the type of parameter that the curve
     * represents.
     *
     * Documentation:
     * [parameterID](https://developer.apple.com/documentation/corehaptics/chhapticparametercurve/parameterid)
     *
     * @see CHHapticParameterCurve
     * @see CHHapticDynamicParameterID
     */
    abstract val parameterID: CHHapticDynamicParameterID

    /**
     * Builds and returns a corresponding [CHHapticParameterCurve] instance
     * based on the properties of the concrete [HapticParameterCurve] subclass.
     *
     * @return A [CHHapticParameterCurve] representing this haptic parameter
     *   curve.
     */
    fun build(): CHHapticParameterCurve {
        return CHHapticParameterCurve(
            parameterID = parameterID,
            controlPoints = controlPoints.map { it.build() },
            relativeTime = relativeTime.seconds
        )
    }
}

/**
 * A builder for creating a haptic pattern using the Core Haptics
 * framework.
 *
 * Documentation:
 * [CHHapticPattern](https://developer.apple.com/documentation/corehaptics/chhapticpattern)
 *
 * @property events
 * @property curves
 */
@HapticPatternBuilder.Dsl
open class HapticPatternBuilder : LockableValueDsl() {
    /**
     * A list of events that make up the haptic pattern.
     *
     * Documentation:
     * [init(events:parameterCurves:)](https://developer.apple.com/documentation/corehaptics/chhapticpattern/init(events:parametercurves:))
     */
    var events: List<HapticEvent> by required()

    /**
     * A list of parameter curves that define how the haptic pattern’s
     * parameters change over time.
     *
     * Documentation:
     * [init(events:parameterCurves:)](https://developer.apple.com/documentation/corehaptics/chhapticpattern/init(events:parametercurves:))
     */
    var curves: List<HapticParameterCurve> by prepared(emptyList())

    /**
     * Sets the list of haptic events for the pattern using a DSL block.
     *
     * @param block A lambda with receiver that configures the [Events]
     *   builder.
     * @see HapticEvent
     */
    inline fun events(block: Events.() -> Unit) {
        events = Events().apply(block).build()
    }

    /**
     * Sets the list of parameter curves for the pattern using a DSL block.
     *
     * @param block A lambda with receiver that configures the [Curves]
     *   builder.
     */
    inline fun curves(block: Curves.() -> Unit) {
        curves = Curves().apply(block).build()
    }

    /**
     * Pre-build hook that can be overridden to perform actions before the
     * pattern is built.
     *
     * This method is called before the actual building of the haptic pattern
     * occurs, allowing for any necessary setup or validation.
     */
    protected open fun preBuild() {}

    /**
     * Builds and returns a [CHHapticPattern] instance based on the configured
     * events and parameter curves.
     *
     * This method locks the builder to prevent further modifications and
     * constructs the haptic pattern using the provided events and curves.
     *
     * @return A [CHHapticPattern] representing the configured haptic pattern.
     */
    @ExperimentalForeignApi
    @BetaInteropApi
    fun build(): CHHapticPattern {
        preBuild()
        lock()
        return runThrowing {
            CHHapticPattern(
                events = events.map { event -> event.build() },
                parameterCurves = curves.map { curve -> curve.build() },
                error = it,
            )
        }
    }

    /**
     * A DSL for building a list of haptic events and parameter curves for a
     * haptic pattern.
     *
     * This class provides functions to add transient and continuous haptic
     * events, as well as parameter curves for intensity, sharpness, attack
     * time, decay time, and release time.
     *
     * @property transient
     * @property continuous
     */
    @Dsl
    open class Events : LockableValueDsl() {
        /**
         * A list of [HapticEvent]s that make up the haptic pattern.
         *
         * This list is mutable and can be modified using the [add] function or
         * the DSL functions [transient] and [continuous] to add specific types of
         * haptic events.
         */
        val events by list<HapticEvent>(
            beforeAccess = {
                if (!isLocked) return@list
                check(isNotEmpty()) { "At least one event must be defined." }
            },
        )

        /**
         * Adds a [HapticEvent] to the events list.
         *
         * @param event The [HapticEvent] to be added to the list.
         */
        open fun add(event: HapticEvent) {
            events.add(event)
        }

        /**
         * Adds a transient haptic event to the event list.
         *
         * This function is a shorthand for adding a transient event with default
         * parameters.
         */
        val transient inline get() = transient()

        /**
         * Adds a transient haptic event to the event list using a DSL block.
         *
         * @param relativeTime The relative time at which the transient event
         *   starts, defaulting to zero.
         * @param block A lambda with receiver that configures the [Transient]
         *   parameters.
         */
        inline fun transient(relativeTime: Duration = Duration.ZERO, block: Transient.() -> Unit = {}) {
            val parameters = Transient().apply(block)
            add(
                HapticEvent.Transient(
                    hapticIntensity = parameters.intensity,
                    hapticSharpness = parameters.sharpness,
                    relativeTime = relativeTime,
                )
            )
        }

        /**
         * Adds a continuous haptic event to the event list.
         *
         * This function is a shorthand for adding a continuous event with default
         * parameters.
         */
        val continuous inline get() = continuous()

        /**
         * Adds a continuous haptic event to the event list using a DSL block.
         *
         * @param duration The duration of the continuous haptic event, defaulting
         *   to [DefaultDuration].
         * @param relativeTime The relative time at which the continuous event
         *   starts, defaulting to zero.
         * @param block A lambda with receiver that configures the [Continuous]
         *   parameters.
         */
        inline fun continuous(
            duration: Duration = DefaultDuration,
            relativeTime: Duration = Duration.ZERO,
            block: Continuous.() -> Unit = {},
        ) {
            val parameters = Continuous().apply(block)
            add(
                HapticEvent.Continuous(
                    hapticIntensity = parameters.intensity,
                    hapticSharpness = parameters.sharpness,
                    relativeTime = relativeTime,
                    duration = duration,
                    attackTime = parameters.attackTime,
                    decayTime = parameters.decayTime,
                    releaseTime = parameters.releaseTime,
                    sustained = parameters.sustained,
                )
            )
        }

        /**
         * Builds and returns the list of [HapticEvent]s.
         *
         * This function checks that at least one event has been defined and
         * returns the list of events.
         *
         * @return A list of [HapticEvent]s representing the haptic pattern.
         */
        fun build(): List<HapticEvent> {
            lock()
            return events.toList()
        }

        /**
         * A DSL for defining parameters for a transient haptic event.
         *
         * This class allows setting the intensity, sharpness, and relative time
         * for a transient haptic event.
         *
         * @see HapticEvent.Transient
         */
        @Dsl
        class Transient : ValueDsl() {
            var intensity: Float? by optional()
            var sharpness: Float? by optional()
        }

        /**
         * A DSL for defining parameters for a continuous haptic event.
         *
         * This class allows setting the intensity, sharpness, relative time,
         * duration, attack time, decay time, release time, and sustained state for
         * a continuous haptic event.
         *
         * @see HapticEvent.Continuous
         */
        @Dsl
        class Continuous : ValueDsl() {
            var intensity: Float? by optional()
            var sharpness: Float? by optional()
            var attackTime: Duration? by optional()
            var decayTime: Duration? by optional()
            var releaseTime by prepared(Duration.ZERO)
            var sustained by prepared(false)
        }
    }

    /**
     * A DSL for building a list of parameter curves for a haptic pattern.
     *
     * This class provides functions to add curves for intensity, sharpness,
     * attack time, decay time, and release time, allowing for smooth
     * transitions in haptic feedback.
     *
     * @property intensity
     * @property sharpness
     * @property attackTime
     * @property decayTime
     * @property releaseTime
     */
    @Dsl
    open class Curves {
        private val _curves = mutableListOf<HapticParameterCurve>()

        /**
         * A list of [HapticParameterCurve]s that define how the haptic pattern’s
         * parameters change over time.
         *
         * This list is mutable and can be modified using the [add] function or the
         * DSL functions [intensity], [sharpness], [attackTime], [decayTime], and
         * [releaseTime] to add specific types of parameter curves.
         */
        val curves get() = _curves

        /**
         * Adds a [HapticParameterCurve] to the curves list.
         *
         * @param curve The [HapticParameterCurve] to be added to the list.
         */
        open fun add(curve: HapticParameterCurve) {
            _curves.add(curve)
        }

        /**
         * Adds a curve for haptic intensity using a DSL block.
         *
         * @param relativeTime The relative time at which the release curve starts,
         *   defaulting to zero.
         * @param block A lambda with receiver that configures the [Curve]
         *   parameters for intensity.
         */
        inline fun intensity(relativeTime: Duration = Duration.ZERO, block: Curve.() -> Unit) {
            val curve = Curve().apply(block)
            add(HapticParameterCurve.Intensity(curve.controlPoints, relativeTime))
        }

        /**
         * Adds a curve for haptic sharpness using a DSL block.
         *
         * @param relativeTime The relative time at which the release curve starts,
         *   defaulting to zero.
         * @param block A lambda with receiver that configures the [Curve]
         *   parameters for sharpness.
         */
        inline fun sharpness(relativeTime: Duration = Duration.ZERO, block: Curve.() -> Unit) {
            val curve = Curve().apply(block)
            add(HapticParameterCurve.Sharpness(curve.controlPoints, relativeTime))
        }

        /**
         * Adds a curve for attack time using a DSL block.
         *
         * @param relativeTime The relative time at which the release curve starts,
         *   defaulting to zero.
         * @param block A lambda with receiver that configures the [Curve]
         *   parameters for attack time.
         */
        inline fun attackTime(relativeTime: Duration = Duration.ZERO, block: Curve.() -> Unit) {
            val curve = Curve().apply(block)
            add(HapticParameterCurve.AttackTime(curve.controlPoints, relativeTime))
        }

        /**
         * Adds a curve for decay time using a DSL block.
         *
         * @param relativeTime The relative time at which the release curve starts,
         *   defaulting to zero.
         * @param block A lambda with receiver that configures the [Curve]
         *   parameters for decay time.
         */
        inline fun decayTime(relativeTime: Duration = Duration.ZERO, block: Curve.() -> Unit) {
            val curve = Curve().apply(block)
            add(HapticParameterCurve.DecayTime(curve.controlPoints, relativeTime))
        }

        /**
         * Adds a curve for release time using a DSL block.
         *
         * @param relativeTime The relative time at which the release curve starts,
         *   defaulting to zero.
         * @param block A lambda with receiver that configures the [Curve]
         *   parameters for release time.
         */
        inline fun releaseTime(relativeTime: Duration = Duration.ZERO, block: Curve.() -> Unit) {
            val curve = Curve().apply(block)
            add(HapticParameterCurve.ReleaseTime(curve.controlPoints, relativeTime))
        }

        /**
         * Builds and returns the list of [HapticParameterCurve]s.
         *
         * This function returns the list of curves that have been added to the
         * builder.
         *
         * @return A list of [HapticParameterCurve]s representing the haptic
         *   parameter curves.
         */
        fun build() = curves

        /**
         * A DSL for defining a curve with control points for haptic parameters.
         *
         * This class allows adding control points to a curve, which define how a
         * parameter value changes over time during haptic playback.
         *
         * @property at
         */
        @Dsl
        open class Curve {
            private val _controlPoints = mutableListOf<HapticParameterCurve.ControlPoint>()

            /**
             * A list of [HapticParameterCurve.ControlPoint]s that define the control
             * points of the curve.
             *
             * This list is mutable and can be modified using the [at] function or the
             * [add] function to add specific control points.
             */
            val controlPoints get() = _controlPoints.toList()

            /**
             * Adds a control point to the curve.
             *
             * @param point The [HapticParameterCurve.ControlPoint] to be added to the
             *   curve.
             */
            open fun add(point: HapticParameterCurve.ControlPoint) {
                _controlPoints.add(point)
            }

            /**
             * Adds a control point to the curve using a relative time and value.
             *
             * @param relativeTime The time at which the control point occurs, relative
             *   to the start of the curve.
             * @param value The value of the control point.
             */
            fun add(relativeTime: Duration, value: Float) {
                add(HapticParameterCurve.ControlPoint(relativeTime, value))
            }

            /**
             * Adds a control point to the curve using a DSL block.
             *
             * @param block A lambda with receiver that configures the [Point]
             *   parameters for the control point.
             */
            inline fun add(block: Point.() -> Unit) {
                val point = Point().apply(block)
                add(point.relativeTime, point.value)
            }

            /**
             * Adds a control point to the curve at a specific relative time.
             *
             * This function allows using an infix notation to add a control point with
             * a value at a specified relative time.
             *
             * @param relativeTime The time at which the control point occurs, relative
             *   to the start of the curve.
             * @receiver The value of the control point.
             */
            infix fun Float.at(relativeTime: Duration) {
                add(relativeTime, this)
            }

            /**
             * A DSL for defining a point in a haptic parameter curve.
             *
             * This class allows setting the relative time and value for a control
             * point in the curve.
             *
             * @see HapticParameterCurve.ControlPoint
             */
            @Dsl
            class Point : ValueDsl() {
                var relativeTime: Duration by required()
                var value: Float by required()
            }
        }
    }

    @DslMarker
    annotation class Dsl
}

/**
 * Creates a [CHHapticPattern] using the provided DSL block.
 *
 * This function allows you to define a haptic pattern using a DSL builder
 * that configures events and parameter curves.
 *
 * @param block A lambda with receiver that configures the
 *   [HapticPatternBuilder].
 * @return A [CHHapticPattern] representing the defined haptic pattern.
 * @sample samples.dsl.buildHapticPattern
 */
@ExperimentalForeignApi
@BetaInteropApi
inline fun CHHapticPattern(block: HapticPatternBuilder.() -> Unit) = HapticPatternBuilder().apply(block).build()

private val Duration.seconds get() = this.inWholeMilliseconds.toDouble() / 1000

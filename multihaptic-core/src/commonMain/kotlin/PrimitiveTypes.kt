package top.ltfan.multihaptic

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public enum class PrimitiveType(public val duration: Duration) {
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
public enum class DelayType {
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
     * The sample schedules the primitives as follows:
     * ```
     *  0ms               20ms                     100ms
     *  PRIMITIVE_CLICK---PRIMITIVE_TICK-----------PRIMITIVE_THUD
     * ```
     *
     * @sample samples.primitive.delayTypeRelativeStartOffsetExample
     */
    RelativeStartOffset;
}

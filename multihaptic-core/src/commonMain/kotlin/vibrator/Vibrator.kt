package top.ltfan.multihaptic.vibrator

import top.ltfan.multihaptic.HapticEffect
import top.ltfan.multihaptic.HapticEffectDsl

/**
 * Represents a vibrator that can perform haptic effects.
 *
 * This interface provides methods to vibrate with a specific
 * [HapticEffect], vibrate with a composed effect using a builder, and
 * cancel any ongoing vibrations.
 */
public interface Vibrator {
    /**
     * Vibrates with the specified [HapticEffect].
     *
     * @param effect The [HapticEffect] to use for vibration.
     */
    public fun vibrate(effect: HapticEffect)

    /**
     * Vibrates with a haptic effect defined by the provided builder block.
     *
     * @param builder The block to configure the effect.
     */
    public fun vibrate(builder: HapticEffectDsl.() -> Unit): Unit = vibrate(HapticEffect(builder))

    /** Cancels any ongoing vibrations. */
    public fun cancel()

    /**
     * Checks if vibration is supported on this platform/device.
     *
     * @return `true` if vibration hardware and API are available, `false`
     *   otherwise.
     */
    public val isVibrationSupported: Boolean
        get() = false
}

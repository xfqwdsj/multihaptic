package top.ltfan.multihaptic.vibrator

import top.ltfan.multihaptic.HapticEffect

/**
 * Represents a vibrator that can perform haptic effects.
 *
 * This interface provides methods to vibrate with a specific
 * [HapticEffect], vibrate with a composed effect using a builder, and
 * cancel any ongoing vibrations.
 */
interface Vibrator {
    /**
     * Vibrates with the specified [HapticEffect].
     *
     * @param effect The [HapticEffect] to use for vibration.
     */
    fun vibrate(effect: HapticEffect)

    /**
     * Vibrates with a haptic effect defined by the provided builder block.
     *
     * @param builder The block to configure the [HapticEffect.Builder].
     */
    fun vibrate(builder: HapticEffect.Builder.() -> Unit) =
        vibrate(HapticEffect(builder))

    /** Cancels any ongoing vibrations. */
    fun cancel()

    /**
     * Checks if vibration is supported on this platform/device.
     *
     * @return `true` if vibration hardware and API are available, `false`
     *   otherwise.
     */
    val isVibrationSupported: Boolean
        get() = false
}

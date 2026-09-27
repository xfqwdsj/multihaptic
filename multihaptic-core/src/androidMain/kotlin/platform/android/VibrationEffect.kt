package top.ltfan.multihaptic.platform.android

import android.os.Build
import android.os.VibrationEffect
import androidx.annotation.RequiresApi
import top.ltfan.multihaptic.platform.android.dsl.AmplitudeConfig
import top.ltfan.multihaptic.platform.android.dsl.AmplitudeDsl
import top.ltfan.multihaptic.platform.android.dsl.EnvelopeConfig
import top.ltfan.multihaptic.platform.android.dsl.EnvelopeDsl
import top.ltfan.multihaptic.platform.android.dsl.WaveformConfig
import top.ltfan.multihaptic.platform.android.dsl.WaveformDsl
import top.ltfan.multihaptic.platform.android.dsl.amplitudeConfig
import top.ltfan.multihaptic.platform.android.dsl.envelopeConfig
import top.ltfan.multihaptic.platform.android.dsl.toNative
import top.ltfan.multihaptic.platform.android.dsl.waveformConfig
import kotlin.math.roundToInt

public fun WaveformConfig.scaled(factor: Float): WaveformConfig = copy(
    controlPoints = controlPoints.map { point ->
        val amplitude = point.data.amplitude * factor
        require(amplitude in 0f..1f) { "Scaled amplitude must be between 0 and 1." }
        point.copy(data = point.data.copy(amplitude = amplitude))
    },
)

public fun EnvelopeConfig.scaled(factor: Float): EnvelopeConfig = copy(
    controlPoints = controlPoints.map { point ->
        val intensity = point.data.intensity * factor
        require(intensity in 0f..1f) { "Scaled intensity must be between 0 and 1." }
        point.copy(data = point.data.copy(intensity = intensity))
    },
)

public fun AmplitudeConfig.scaled(factor: Float): AmplitudeConfig = copy(
    controlPoints = controlPoints.map { point ->
        point.copy(amplitude = (point.amplitude * factor).roundToInt().coerceIn(0..255))
    },
)

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public fun waveformVibrationEffect(
    factor: Float = 1f,
    initialFrequencyHz: Float? = null,
    block: WaveformDsl.() -> Unit,
): VibrationEffect = waveformConfig(initialFrequencyHz, block).scaled(factor).toNative()

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public fun envelopeVibrationEffect(
    factor: Float = 1f,
    initialSharpness: Float? = null,
    block: EnvelopeDsl.() -> Unit,
): VibrationEffect = envelopeConfig(initialSharpness, block).scaled(factor).toNative()

@RequiresApi(Build.VERSION_CODES.O)
public fun amplitudeVibrationEffect(
    factor: Float = 1f,
    repeat: Int = -1,
    block: AmplitudeDsl.() -> Unit,
): VibrationEffect = amplitudeConfig(repeat, block).scaled(factor).toNative()

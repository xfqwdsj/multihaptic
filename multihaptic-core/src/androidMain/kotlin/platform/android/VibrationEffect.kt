package top.ltfan.multihaptic.platform.android

import android.os.Build
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import platform.android.dsl.AmplitudeVibrationEffectBuilder
import platform.android.dsl.EnvelopeVibrationEffectBuilder
import platform.android.dsl.WaveformVibrationEffectBuilder
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
inline fun waveformVibrationEffect(
    factor: Float = 1f,
    @FloatRange(from = 0.0) initialFrequencyHz: Float? = null,
    block: WaveformVibrationEffectBuilder.() -> Unit,
) = ScalableWaveformVibrationEffectBuilder(factor, initialFrequencyHz).apply(block).build()

class ScalableWaveformVibrationEffectBuilder(
    private val factor: Float,
    @FloatRange(from = 0.0) initialFrequencyHz: Float?,
) : WaveformVibrationEffectBuilder(initialFrequencyHz) {
    override fun preBuild() {
        for (i in list.indices) {
            val point = list[i]
            val scaledAmplitude = point.data.amplitude * factor
            val newData = point.data.copy(amplitude = scaledAmplitude)
            list[i] = point.copy(data = newData)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
inline fun envelopeVibrationEffect(
    factor: Float = 1f,
    @FloatRange(from = 0.0) initialSharpness: Float? = null,
    block: EnvelopeVibrationEffectBuilder.() -> Unit,
) = ScalableEnvelopeVibrationEffectBuilder(factor, initialSharpness).apply(block).build()

class ScalableEnvelopeVibrationEffectBuilder(
    private val factor: Float,
    @FloatRange(from = 0.0) initialSharpness: Float?,
) : EnvelopeVibrationEffectBuilder(initialSharpness) {
    override fun preBuild() {
        for (i in list.indices) {
            val point = list[i]
            val scaledIntensity = point.data.intensity * factor
            val newData = point.data.copy(intensity = scaledIntensity)
            list[i] = point.copy(data = newData)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
inline fun amplitudeVibrationEffect(
    factor: Float = 1f,
    repeat: Int = -1,
    block: AmplitudeVibrationEffectBuilder.() -> Unit,
) = ScalableAmplitudeVibrationEffectBuilder(factor, repeat).apply(block).build()

class ScalableAmplitudeVibrationEffectBuilder(
    private val factor: Float,
    repeat: Int,
) : AmplitudeVibrationEffectBuilder(repeat) {
    override fun preBuild() {
        for (i in list.indices) {
            val point = list[i]
            val scaledAmplitude = (point.amplitude * factor).roundToInt().coerceIn(0..255)
            list[i] = point.copy(amplitude = scaledAmplitude)
        }
    }
}

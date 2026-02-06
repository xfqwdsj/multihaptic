package top.ltfan.multihaptic.vibrator

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import platform.android.dsl.ComposedVibrationEffectBuilder
import platform.android.dsl.EnvelopeVibrationEffectBuilder
import platform.android.dsl.composedVibrationEffect
import platform.android.dsl.onOffVibrationEffect
import top.ltfan.multihaptic.*
import top.ltfan.multihaptic.platform.android.OffTimeOfCustomOnOffEffect
import top.ltfan.multihaptic.platform.android.amplitudeVibrationEffect
import top.ltfan.multihaptic.platform.android.envelopeVibrationEffect
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AndroidVibrator internal constructor(private val vibrator: Vibrator, coroutineScope: CoroutineScope) :
    AbstractVibrator(coroutineScope) {

    override suspend fun perform(effect: HapticEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && effect.isComposedSupported) {
            composed(effect)
            return
        }

        effect.unpack { basic ->
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && basic.isComposedSupported -> composed(basic)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && isEnvelopeSupported -> envelope(basic)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isAmplitudeSupported -> amplitude(basic)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> onOff(basic)
                else -> fallback(basic)
            }
        }
    }

    override fun cancel() = vibrator.cancel()

    override fun isVibrationSupported(): Boolean = vibrator.hasVibrator()

    private val HapticEffect.isComposedSupported: Boolean
        @RequiresApi(Build.VERSION_CODES.R) inline get() = primitives.all { it.basic is BasicPrimitive.Predefined } && vibrator.areAllPrimitivesSupported(
            *primitives.map { (it.basic as? BasicPrimitive.Predefined)?.composedPrimitiveType?.id ?: return false }
                .toIntArray(),
        )

    private val BasicPrimitive.isComposedSupported: Boolean
        @RequiresApi(Build.VERSION_CODES.R) inline get() =
            this is BasicPrimitive.Predefined && vibrator.areAllPrimitivesSupported(composedPrimitiveType.id)

    @RequiresApi(Build.VERSION_CODES.R)
    private fun composed(effect: HapticEffect) {
        vibrator.vibrate(effect.composedEffect)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun composed(basic: BasicPrimitive) {
        if (basic !is BasicPrimitive.Predefined) {
            delay(basic.duration)
            return
        }

        composedVibrationEffect {
            primitive(basic.composedPrimitiveType) {
                scale = basic.scale
            }
        }.let { vibrator.vibrate(it) }
    }

    private val isEnvelopeSupported: Boolean
        @RequiresApi(Build.VERSION_CODES.BAKLAVA) inline get() = vibrator.areEnvelopeEffectsSupported()

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun envelope(basic: BasicPrimitive) {
        vibrator.vibrate(basic.envelopeEffect)
    }

    private val isAmplitudeSupported @RequiresApi(Build.VERSION_CODES.O) inline get() = vibrator.hasAmplitudeControl()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun amplitude(basic: BasicPrimitive) {
        vibrator.vibrate(basic.amplitudeEffect)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onOff(basic: BasicPrimitive) {
        vibrator.vibrate(basic.onOffEffect)
    }

    private fun fallback(basic: BasicPrimitive) {
        @Suppress("DEPRECATION") vibrator.vibrate(basic.duration.inWholeMilliseconds)
    }

    private val BasicPrimitive.envelopeEffect
        @RequiresApi(Build.VERSION_CODES.BAKLAVA) get() = when (this) {
            is BasicPrimitive.Predefined -> when (type) {
                PrimitiveType.Click -> envelopeVibrationEffect(scale) {
                    point(.8f, 1f) at type.duration
                    point(0f, 1f) after 5.milliseconds
                }

                PrimitiveType.Thud -> envelopeVibrationEffect(scale) {
                    point(1f, .6f) at 20.milliseconds
                    point(.5f, .2f) after 60.milliseconds
                    point(0f, 1f) after 5.milliseconds
                }

                PrimitiveType.Spin -> envelopeVibrationEffect(scale, .4f) {
                    point(.8f, .4f) at 40.milliseconds
                    point(.7f, .4f) at 80.milliseconds
                    point(0f, .4f) at 120.milliseconds
                    point(0f, 1f) after 5.milliseconds
                }

                PrimitiveType.QuickRise -> envelopeVibrationEffect(scale, .2f) {
                    point(1f, 1f) at 50.milliseconds
                    point(0f, 1f) after 5.milliseconds
                }

                PrimitiveType.SlowRise -> envelopeVibrationEffect(scale, .1f) {
                    point(.7f, .5f) at 200.milliseconds
                    point(0f, 1f) after 5.milliseconds
                }

                PrimitiveType.QuickFall -> envelopeVibrationEffect(scale, 1f) {
                    point(1f, 1f) at 5.milliseconds
                    point(.2f, .2f) at 60.milliseconds
                    point(0f, 1f) after 5.milliseconds
                }

                PrimitiveType.Tick -> envelopeVibrationEffect(scale, 1f) {
                    point(1f, 1f) at type.duration
                    point(0f, 1f) after 5.milliseconds
                }

                PrimitiveType.LowTick -> envelopeVibrationEffect(scale, .8f) {
                    point(.8f, .8f) at type.duration
                    point(0f, 1f) after 5.milliseconds
                }
            }

            is BasicPrimitive.Custom -> {
                val allTimes = (curves.intensity + curves.sharpness).map { it.time }.distinct().sorted()

                var timeShift = Duration.ZERO
                var initialSharpness: Float? = null

                if (curves.intensity.firstOrNull()?.time == Duration.ZERO) {
                    timeShift = 5.milliseconds
                }
                if (curves.sharpness.firstOrNull()?.time == Duration.ZERO) {
                    initialSharpness = curves.sharpness.first().value
                }

                val controlPoints = mutableListOf<Pair<EnvelopeVibrationEffectBuilder.PointData, Duration>>()
                allTimes.forEach { time ->
                    val intensity = getValueAt(time, curves.intensity)
                    val sharpness = getValueAt(time, curves.sharpness)
                    controlPoints.add(
                        EnvelopeVibrationEffectBuilder.PointData(intensity, sharpness) to time + timeShift,
                    )
                }

                envelopeVibrationEffect(scale, initialSharpness) {
                    controlPoints.forEach { (data, time) -> data at time }

                    val lastPoint = controlPoints.lastOrNull()
                    if (lastPoint != null && lastPoint.first.intensity > 0f) {
                        point(0f, lastPoint.first.sharpness) after 5.milliseconds
                    }
                }
            }
        }

    private val HapticEffect.composedEffect
        @RequiresApi(Build.VERSION_CODES.R) get() = composedVibrationEffect {
            primitives.forEach { primitive ->
                if (primitive.basic !is BasicPrimitive.Predefined) return@forEach

                primitive(primitive.basic.composedPrimitiveType) {
                    scale = primitive.basic.scale
                    delay = primitive.delay
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                        delayType = primitive.delayType.androidDelayType
                    }
                }
            }
        }

    private val BasicPrimitive.Predefined.composedPrimitiveType
        @RequiresApi(Build.VERSION_CODES.R) get() = when (type) {
            PrimitiveType.Click -> ComposedVibrationEffectBuilder.PrimitiveType.Click
            PrimitiveType.Thud -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ComposedVibrationEffectBuilder.PrimitiveType.Thud
            } else {
                ComposedVibrationEffectBuilder.PrimitiveType.Click
            }

            PrimitiveType.Spin -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ComposedVibrationEffectBuilder.PrimitiveType.Spin
            } else {
                ComposedVibrationEffectBuilder.PrimitiveType.QuickRise
            }

            PrimitiveType.QuickRise -> ComposedVibrationEffectBuilder.PrimitiveType.QuickRise
            PrimitiveType.SlowRise -> ComposedVibrationEffectBuilder.PrimitiveType.QuickRise
            PrimitiveType.QuickFall -> ComposedVibrationEffectBuilder.PrimitiveType.QuickFall
            PrimitiveType.Tick -> ComposedVibrationEffectBuilder.PrimitiveType.Tick
            PrimitiveType.LowTick -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ComposedVibrationEffectBuilder.PrimitiveType.LowTick
            } else {
                ComposedVibrationEffectBuilder.PrimitiveType.Tick
            }
        }

    private val BasicPrimitive.amplitudeEffect
        @RequiresApi(Build.VERSION_CODES.O) get() = when (this) {
            is BasicPrimitive.Predefined -> when (type) {
                PrimitiveType.Click -> amplitudeVibrationEffect(scale) {
                    .9f after type.duration
                }

                PrimitiveType.Thud -> amplitudeVibrationEffect(scale) {
                    1f after 20.milliseconds
                    .5f after 60.milliseconds
                }

                PrimitiveType.Spin -> amplitudeVibrationEffect(scale) {
                    .2f after 40.milliseconds
                    .8f after 40.milliseconds
                    .7f after 40.milliseconds
                }

                PrimitiveType.QuickRise -> amplitudeVibrationEffect(scale) {
                    .1f after 25.milliseconds
                    1f after 25.milliseconds
                }

                PrimitiveType.SlowRise -> amplitudeVibrationEffect(scale) {
                    .1f after 100.milliseconds
                    .7f after 100.milliseconds
                }

                PrimitiveType.QuickFall -> amplitudeVibrationEffect(scale) {
                    1f after 30.milliseconds
                    .2f after 30.milliseconds
                }

                PrimitiveType.Tick -> amplitudeVibrationEffect(scale) {
                    1f after type.duration
                }

                PrimitiveType.LowTick -> amplitudeVibrationEffect(scale) {
                    .6f after type.duration
                }
            }

            is BasicPrimitive.Custom -> {
                val times = mutableListOf<Long>()
                val amplitudes = mutableListOf<Int>()

                val intensityCurve = buildList {
                    val first = curves.intensity.first()
                    val last = curves.intensity.last()
                    if (first.time != Duration.ZERO) {
                        add(HapticCurves.Keyframe(Duration.ZERO, first.value))
                    }
                    addAll(curves.intensity)
                    if (last.time != duration) {
                        add(HapticCurves.Keyframe(duration, last.value))
                    }
                }

                intensityCurve.forEachIndexed { index, (time, value) ->
                    val lastTime = if (index == 0) 0L else intensityCurve[index - 1].time.inWholeMilliseconds
                    val nextTime = if (index == intensityCurve.lastIndex) {
                        time.inWholeMilliseconds
                    } else {
                        intensityCurve[index + 1].time.inWholeMilliseconds
                    }

                    times += (nextTime - lastTime) / 2
                    amplitudes += value.scale(scale)
                }

                VibrationEffect.createWaveform(
                    times.toLongArray(), amplitudes.toIntArray(), -1,
                )
            }
        }

    private val BasicPrimitive.onOffEffect
        @RequiresApi(Build.VERSION_CODES.O) get() = when (this) {
            is BasicPrimitive.Predefined -> when (type) {
                PrimitiveType.Click -> onOffVibrationEffect {
                    range(0.milliseconds..type.duration)
                }

                PrimitiveType.Thud -> onOffVibrationEffect {
                    range(0.milliseconds..10.milliseconds)
                    range(20.milliseconds..80.milliseconds)
                }

                PrimitiveType.Spin -> onOffVibrationEffect {
                    range(0.milliseconds..20.milliseconds)
                    range(30.milliseconds..60.milliseconds)
                    range(70.milliseconds..120.milliseconds)
                }

                PrimitiveType.QuickRise -> onOffVibrationEffect {
                    range(0.milliseconds..10.milliseconds)
                    range(20.milliseconds..50.milliseconds)
                }

                PrimitiveType.SlowRise -> onOffVibrationEffect {
                    range(0.milliseconds..10.milliseconds)
                    range(20.milliseconds..30.milliseconds)
                    range(40.milliseconds..60.milliseconds)
                    range(70.milliseconds..100.milliseconds)
                }

                PrimitiveType.QuickFall -> onOffVibrationEffect {
                    range(0.milliseconds..40.milliseconds)
                    range(50.milliseconds..60.milliseconds)
                }

                PrimitiveType.Tick -> onOffVibrationEffect {
                    range(0.milliseconds..type.duration)
                }

                PrimitiveType.LowTick -> onOffVibrationEffect {
                    range(0.milliseconds..type.duration)
                }
            }

            is BasicPrimitive.Custom -> {
                val times = mutableListOf<Long>()

                val intensityCurve = mutableListOf<HapticCurves.Keyframe>().apply {
                    if (curves.intensity.first().time != Duration.ZERO) {
                        add(HapticCurves.Keyframe(Duration.ZERO, 0f))
                    }
                    addAll(curves.intensity)
                }

                intensityCurve.forEachIndexed { index, (time, value) ->
                    val lastTime = if (index == 0) 0L else intensityCurve[index - 1].time.inWholeMilliseconds
                    val nextTime = if (index == intensityCurve.lastIndex) {
                        time.inWholeMilliseconds
                    } else {
                        intensityCurve[index + 1].time.inWholeMilliseconds
                    }

                    val delta = if (index == 0 || index == intensityCurve.lastIndex) {
                        OffTimeOfCustomOnOffEffect / 2
                    } else {
                        OffTimeOfCustomOnOffEffect
                    }.inWholeMilliseconds

                    times += if (index == 0) 0 else OffTimeOfCustomOnOffEffect.inWholeMilliseconds
                    times += if (value > 0) (nextTime - lastTime) / 2 - delta else 0
                }

                VibrationEffect.createWaveform(times.toLongArray(), -1)
            }
        }

    private val DelayType.androidDelayType
        @RequiresApi(Build.VERSION_CODES.BAKLAVA) get() = when (this) {
            DelayType.Pause -> ComposedVibrationEffectBuilder.DelayType.Pause
            DelayType.RelativeStartOffset -> ComposedVibrationEffectBuilder.DelayType.RelativeStartOffset
        }

    private fun getValueAt(time: Duration, curve: List<HapticCurves.Keyframe>): Float {
        curve.find { it.time == time }?.let { return it.value }

        val prev = curve.lastOrNull { it.time < time } ?: return curve.firstOrNull()?.value ?: 0f
        val next = curve.firstOrNull { it.time > time }

        if (next == null) return prev.value

        val timeRatio = (time - prev.time).inWholeMilliseconds.toFloat() / (next.time - prev.time).inWholeMilliseconds
        return prev.value + (next.value - prev.value) * timeRatio
    }

    private fun Float.scale(factor: Float) = (this * factor * 255).roundToInt().coerceIn(0..255)
}

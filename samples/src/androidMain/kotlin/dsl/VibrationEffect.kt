package samples.dsl

import android.annotation.SuppressLint
import android.content.Context
import platform.android.dsl.*
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("NewApi")
fun onOff() {
    onOffVibrationEffect {
        range(10.milliseconds..20.milliseconds)
        range(30.milliseconds..40.milliseconds)
        pattern(10.milliseconds, 10.milliseconds)
    }
}

@SuppressLint("NewApi")
fun amplitude() {
    amplitudeVibrationEffect {
        60 at 0.milliseconds
        80 at 10.milliseconds
        100 at 20.milliseconds
    }
}

@SuppressLint("NewApi")
fun composed() {
    composedVibrationEffect {
        click
        click {
            scale = 0.5f // Adjust the intensity of the click
        }
        thud
        spin
        quickRise
        slowRise
        quickFall
        tick
        lowTick {
            delay = 50.milliseconds // Delay before the effect
            relativeStartOffsetDelay // Delay relative to the start of the previous effect
        }
    }
}

@SuppressLint("NewApi")
fun Context.envelope() {
    if (withVibratorOrNull { this?.areEnvelopeEffectsSupported() } != true) return
    envelopeVibrationEffect {
        point(.5f, 1f) after 10.milliseconds
        point(.9f, 1f) at 20.milliseconds
    }
}

@SuppressLint("NewApi")
fun Context.waveform() {
    tryWithVibrator {
        if (!areEnvelopeEffectsSupported()) return
        waveformVibrationEffect {
            point(.5f, 300f) after 10.milliseconds
            point(.9f, 500f) at 20.milliseconds
        }
    }
}

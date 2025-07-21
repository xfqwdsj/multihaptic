package samples.dsl

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreHaptics.dsl.CHHapticPattern
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun buildHapticPattern() {
    CHHapticPattern {
        events {
            transient {
                intensity = .8f
                sharpness = 1f
            }
            transient(20.milliseconds) // A transient event starts at 20 milliseconds with default intensity and sharpness
            continuous // A continuous event with default duration, intensity, and sharpness
        }
        curves {
            sharpness {
                .4f at 20.milliseconds // A sharpness curve with one control point at 20 milliseconds and value of 0.4
            }
        }
    }
}

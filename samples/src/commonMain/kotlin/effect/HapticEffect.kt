package samples.effect

import top.ltfan.multihaptic.DelayType
import top.ltfan.multihaptic.HapticEffect
import top.ltfan.multihaptic.PrimitiveType
import kotlin.time.Duration.Companion.milliseconds

fun buildHapticEffect() {
    HapticEffect {
        predefined(PrimitiveType.Click) {
            scale = .5f // Adjust the intensity of the click
        }
        predefined(PrimitiveType.Thud) {
            scale = .8f // Adjust the intensity of the thud
            delay = 100.milliseconds // Delay before the thud effect
            delayType = DelayType.Pause // 100 milliseconds pause after the previous effect
        }
        predefined(PrimitiveType.Tick) {
            scale = .3f // Adjust the intensity of the tick
            delay = 50.milliseconds // Delay before the tick effect
            delayType = DelayType.RelativeStartOffset // 50 milliseconds relative to the start of the previous effect
        }
        lowTick {
            delay = 30.milliseconds
        }
        quickRise
        custom {
            fallback = PrimitiveType.Spin
            // or
            spinFallback

            curves {
                intensity {
                    0f at 0.milliseconds // Start with no intensity
                    1f at 5.milliseconds // Increase to full intensity
                    0f at 10.milliseconds // Decrease back to no intensity
                }
                sharpness {
                    .3f at 0.milliseconds // Start with low sharpness
                }
            }
        }
    }
}

package samples.primitive

import top.ltfan.multihaptic.DelayType
import top.ltfan.multihaptic.HapticEffect
import top.ltfan.multihaptic.PrimitiveType
import kotlin.time.Duration.Companion.milliseconds

fun delayTypePauseExample() {
    HapticEffect {
        predefined(PrimitiveType.QuickRise)
        predefined(PrimitiveType.Click, 0.7f, 50.milliseconds, DelayType.Pause)
    }
}

fun delayTypeRelativeStartOffsetExample() {
    HapticEffect {
        predefined(PrimitiveType.Click, 1f)
        predefined(PrimitiveType.Tick, 1f, 20.milliseconds, DelayType.RelativeStartOffset)
        predefined(PrimitiveType.Thud, 1f, 80.milliseconds, DelayType.RelativeStartOffset)
    }
}

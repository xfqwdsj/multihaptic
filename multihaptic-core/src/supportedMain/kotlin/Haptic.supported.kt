package top.ltfan.multihaptic

import kotlinx.coroutines.delay
import kotlin.time.Duration

internal val HapticEffect.duration
    get() = (primitives zip delayTimes).fold(Duration.ZERO) { acc, (primitive, delay) ->
        acc + primitive.basic.duration + delay
    }

internal val BasicPrimitive.duration
    inline get() = when (this) {
        is BasicPrimitive.Predefined -> type.duration
        is BasicPrimitive.Custom -> duration
    }

internal suspend inline fun BasicPrimitive.performByDuration(performer: suspend (Duration) -> Unit) {
    when (this) {
        is BasicPrimitive.Predefined -> performer(type.duration)
        is BasicPrimitive.Custom -> fallback?.let { performer(it.duration) } ?: delay(duration)
    }
}

internal suspend fun HapticEffect.unpack(processor: suspend (basic: BasicPrimitive) -> Unit) {
    (primitives zip delayTimes).forEach { (primitive, delay) ->
        delay(delay)
        processor(primitive.basic)
    }
}

internal val HapticEffect.delayTimes: List<Duration>
    get() {
        val list = mutableListOf<Duration>()
        if (primitives.isEmpty()) return list

        list += primitives.first().delay

        for (i in 0 until primitives.lastIndex) {
            val currentDelay = primitives[i].delay
            val (_, nextDelay, nextType) = primitives[i + 1]

            list += when (nextType) {
                DelayType.Pause -> nextDelay
                DelayType.RelativeStartOffset -> (nextDelay - currentDelay).coerceAtLeast(Duration.ZERO)
            }
        }

        return list
    }

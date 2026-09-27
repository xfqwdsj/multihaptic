package top.ltfan.multihaptic.platform.android.dsl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

class AndroidConfigTest {
    @Test
    fun waveformOrdersAbsolutePointsAndRetainsDurations() {
        val config = waveformConfig {
            point(0.8f, 400f) at 20.milliseconds
            point(0.3f, 200f) at 10.milliseconds
            point(0.5f, 300f) at 40.milliseconds
        }
        assertEquals(listOf(10L, 10L, 20L), config.controlPoints.map { it.duration.inWholeMilliseconds })
        assertEquals(listOf(200f, 400f, 300f), config.controlPoints.map { it.data.frequencyHz })
        assertFailsWith<IllegalArgumentException> {
            waveformConfig {
                point(0.5f, 200f) at 10.milliseconds
                point(0.5f, 200f) at 10.milliseconds
            }
        }
    }

    @Test
    fun invalidInsertionDoesNotChangeExistingTimeline() {
        val config = waveformConfig {
            point(0.5f, 200f) at 10.milliseconds
            assertFailsWith<IllegalArgumentException> {
                point(2f, 200f) at 5.milliseconds
            }
        }
        assertEquals(10.milliseconds, config.controlPoints.single().duration)
    }

    @Test
    fun onOffMergesOverlappingAndAdjacentIntervals() {
        val config = onOffConfig {
            range(11.milliseconds..13.milliseconds)
            range(1.milliseconds..4.milliseconds)
            range(10.milliseconds..14.milliseconds)
            range(12.milliseconds..16.milliseconds)
            range(16.milliseconds..18.milliseconds)
        }
        assertEquals(
            listOf(
                OnOffInterval(1.milliseconds, 4.milliseconds),
                OnOffInterval(10.milliseconds, 18.milliseconds),
            ),
            config.intervals,
        )
    }

    @Test
    fun onOffNormalizesDirectListWritesBeforeBuild() {
        val config = onOffConfig {
            intervals += OnOffInterval(10.milliseconds, 15.milliseconds)
            intervals += OnOffInterval(1.milliseconds, 4.milliseconds)
            intervals += OnOffInterval(4.milliseconds, 12.milliseconds)
        }
        assertEquals(listOf(OnOffInterval(1.milliseconds, 15.milliseconds)), config.intervals)
    }

    @Test
    fun amplitudeAndComposedDefaults() {
        val amplitude = amplitudeConfig {
            60 after 10.milliseconds
            80 at 20.milliseconds
        }
        assertEquals(listOf(10L, 10L), amplitude.controlPoints.map { it.duration.inWholeMilliseconds })
        val composed = composedConfig { click { scale = 0.5f } }
        assertEquals(0.5f, composed.primitives.single().scale)
        assertEquals(AndroidDelayType.Pause, composed.primitives.single().delayType)
    }
}

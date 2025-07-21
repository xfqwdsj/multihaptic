import android.os.VibrationEffect
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import platform.android.dsl.*
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class VibrationEffectTest {
    @Test
    fun testWaveformEffectEquivalence() {
        val dslCreated1 = waveformVibrationEffect {
            point(.5f, 300f) after 10.milliseconds
            point(.9f, 500f) at 20.milliseconds
        }

        val androidApi1 = VibrationEffect.WaveformEnvelopeBuilder().apply {
            addControlPoint(.5f, 300f, 10)
            addControlPoint(.9f, 500f, 10)
        }.build()

        assertEquals(androidApi1, dslCreated1)

        val dslCreated2 = waveformVibrationEffect(200f) {
            point(.5f, 300f) after 10.milliseconds
            point(.9f, 500f) after 10.milliseconds
        }

        val androidApi2 = VibrationEffect.WaveformEnvelopeBuilder().apply {
            setInitialFrequencyHz(200f)
            addControlPoint(.5f, 300f, 10)
            addControlPoint(.9f, 500f, 10)
        }.build()

        assertEquals(androidApi2, dslCreated2)
    }

    @Test
    fun testWaveformEffectChecks() {
        assertFailsWith<IllegalStateException> { waveformVibrationEffect {} } // Empty

        assertFailsWith<IllegalStateException> { // Empty
            waveformVibrationEffect {
                assertFailsWith<IllegalArgumentException> { point(-1f, 0f) at 0.milliseconds }
                assertFailsWith<IllegalArgumentException> { point(2f, 0f) at 0.milliseconds }
                assertFailsWith<IllegalArgumentException> { point(1f, -1f) at 0.milliseconds }
                list // Not locked, will not fail
            }
        }
    }

    @Test
    fun testEnvelopeEffectEquivalence() {
        val dslCreated1 = envelopeVibrationEffect {
            point(.5f, 1f) after 10.milliseconds
            point(0f, 1f) at 20.milliseconds
        }

        val androidApi1 = VibrationEffect.BasicEnvelopeBuilder().apply {
            addControlPoint(.5f, 1f, 10)
            addControlPoint(0f, 1f, 10)
        }.build()

        assertEquals(androidApi1, dslCreated1)

        val dslCreated2 = envelopeVibrationEffect(.5f) {
            point(.5f, 1f) after 10.milliseconds
            point(0f, 1f) after 20.milliseconds
        }

        val androidApi2 = VibrationEffect.BasicEnvelopeBuilder().apply {
            setInitialSharpness(.5f)
            addControlPoint(.5f, 1f, 10)
            addControlPoint(0f, 1f, 20)
        }.build()

        assertEquals(androidApi2, dslCreated2)
    }

    @Test
    fun testEnvelopeEffectChecks() {
        assertFailsWith<IllegalStateException> { envelopeVibrationEffect {} } // Empty

        assertFailsWith<IllegalStateException> { // Empty
            envelopeVibrationEffect {
                assertFailsWith<IllegalArgumentException> { point(-1f, 0f) after 0.milliseconds }
                assertFailsWith<IllegalArgumentException> { point(2f, 0f) after 0.milliseconds }
                assertFailsWith<IllegalArgumentException> { point(1f, -1f) after 0.milliseconds }
                list // Not locked, will not fail
            }
        }

        assertFailsWith<IllegalStateException> { // Not ended with zero intensity
            envelopeVibrationEffect {
                assertFailsWith<IllegalArgumentException> { point(-1f, 0f) after 0.milliseconds }
                assertFailsWith<IllegalArgumentException> { point(2f, 0f) after 0.milliseconds }
                assertFailsWith<IllegalArgumentException> { point(1f, -1f) after 0.milliseconds }
                point(1f, 1f) after 1.milliseconds // Last point not zero intensity
                list // Not locked, will not fail
            }
        }
    }

    @Test
    fun testEnvelopeEffectOutOfOrder() {
        val dslCreated = waveformVibrationEffect {
            point(.5f, 500f) at 20.milliseconds
            point(.5f, 300f) at 10.milliseconds
            point(.5f, 300f) at 40.milliseconds
            point(.5f, 300f) at 30.milliseconds
            point(.5f, 300f) at 80.milliseconds
            point(.5f, 300f) at 70.milliseconds
        }

        val androidApi = VibrationEffect.WaveformEnvelopeBuilder().apply {
            addControlPoint(.5f, 300f, 10)
            addControlPoint(.5f, 500f, 10)
            addControlPoint(.5f, 300f, 10)
            addControlPoint(.5f, 300f, 10)
            addControlPoint(.5f, 300f, 30)
            addControlPoint(.5f, 300f, 10)
        }.build()

        assertEquals(androidApi, dslCreated)

        waveformVibrationEffect {
            point(.5f, 500f) at 20.milliseconds
            point(.5f, 500f) after 20.milliseconds
            assertFailsWith<IllegalArgumentException> { point(.5f, 500f) at 40.milliseconds }
        }
    }

    @Test
    fun testComposedEffect() {
        val dslCreated = composedVibrationEffect {
            click
            click { scale = 0.5f } // Adjust the intensity of the click
            thud
            spin
            quickRise
            slowRise
            quickFall
            tick { delay = 50.milliseconds }
            lowTick {
                delay = 50.milliseconds
                relativeStartOffsetDelay
            }
        }

        val androidApi = VibrationEffect.startComposition().apply {
            addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK)
            addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.5f)
            addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD)
            addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN)
            addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE)
            addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE)
            addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL)
            addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 1f, 50)
            addPrimitive(
                VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
                1f,
                50,
                VibrationEffect.Composition.DELAY_TYPE_RELATIVE_START_OFFSET
            )
        }.compose()

        assertEquals(androidApi, dslCreated)
    }

    @Test
    fun testAmplitudeEffect() {
        val dslCreated = amplitudeVibrationEffect {
            60 after 10.milliseconds
            80 at 20.milliseconds
            100 at 30.milliseconds
            .3f after 20.milliseconds
        }

        val androidApi = VibrationEffect.createWaveform(
            longArrayOf(10, 10, 10, 20),
            intArrayOf(60, 80, 100, .3f.times(255).roundToInt().coerceIn(0..255)),
            -1,
        )

        assertEquals(androidApi, dslCreated)

        assertFailsWith<IllegalStateException> { // Empty
            amplitudeVibrationEffect {
                assertFailsWith<IllegalArgumentException> { -1 at 1.milliseconds }
                assertFailsWith<IllegalArgumentException> { 256 at 1.milliseconds }
                assertFailsWith<IllegalArgumentException> { 60 at 0.milliseconds }
                list // Not locked, will not fail
            }
        }
    }

    @Test
    fun testOnOffEffectEquivalence() {
        val dslCreated = onOffVibrationEffect {
            range(10.milliseconds..20.milliseconds)
            range(30.milliseconds..50.milliseconds)
            pattern(10.milliseconds, 10.milliseconds)
        }

        val androidApi = VibrationEffect.createWaveform(
            longArrayOf(10, 10, 10, 20, 10, 10),
            -1,
        )

        assertEquals(androidApi, dslCreated)
    }

    @Test
    fun testOnOffEffectChecks() {
        assertFailsWith<IllegalStateException> { // Empty
            onOffVibrationEffect {
                assertFailsWith<IllegalArgumentException> { range((-1).milliseconds..0.milliseconds) }
                assertFailsWith<IllegalArgumentException> { range(0.milliseconds..(-1).milliseconds) }
                assertFailsWith<IllegalArgumentException> { range(2.milliseconds, 1.milliseconds) }
                assertFailsWith<IllegalArgumentException> { pattern((-1).milliseconds, 0.milliseconds) }
                list // Not locked, will not fail
            }
        }
    }

    @Test
    fun testOnOffEffectOverlapping() {
        val dslCreated = onOffVibrationEffect {
            range(11.milliseconds..13.milliseconds)
            range(15.milliseconds..17.milliseconds)
            range(18.milliseconds..19.milliseconds)

            // Out-of-order ranges
            range(1.milliseconds..4.milliseconds)
            range(6.milliseconds..9.milliseconds)

            // Overlapping ranges
            range(10.milliseconds..14.milliseconds)
            range(12.milliseconds..16.milliseconds)
            range(17.milliseconds..18.milliseconds)

            // Overlapping and out-of-order ranges
            range(0.milliseconds..2.milliseconds)
            range(3.milliseconds..5.milliseconds)
            range(7.milliseconds..8.milliseconds)
        }

        val androidApi = VibrationEffect.createWaveform(
            longArrayOf(0, 5, 1, 3, 1, 9),
            -1,
        )

        assertEquals(androidApi, dslCreated)
    }
}

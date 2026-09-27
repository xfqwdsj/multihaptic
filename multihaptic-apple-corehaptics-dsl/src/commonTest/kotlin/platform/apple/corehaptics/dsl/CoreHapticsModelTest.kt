package top.ltfan.multihaptic.platform.apple.corehaptics.dsl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration

class CoreHapticsModelTest {
    @Test
    fun patternRequiresAnEventAndRepeatedBlocksAppend() {
        assertFailsWith<IllegalStateException> { buildHapticPattern() }
        val pattern = buildHapticPattern {
            events { transient }
            events { continuous }
        }
        assertEquals(2, pattern.events.size)
    }

    @Test
    fun curveRangesDependOnKind() {
        val pattern = buildHapticPattern {
            events { transient }
            curves {
                sharpness { (-0.5f) at Duration.ZERO }
                attackTime { (-1f) at Duration.ZERO }
                decayTime { (-0.75f) at Duration.ZERO }
                releaseTime { (-0.25f) at Duration.ZERO }
                intensity { 0f at Duration.ZERO }
            }
        }
        assertEquals(-0.5f, pattern.curves.first().controlPoints.single().value)
        assertEquals(
            listOf(-0.5f, -1f, -0.75f, -0.25f, 0f),
            pattern.curves.map { it.controlPoints.single().value },
        )
        assertEquals(5, pattern.curves.size)
        assertFailsWith<IllegalArgumentException> {
            buildHapticParameterCurve(CurveKind.Intensity) {
                controlPoints += CurveControlPoint(Duration.ZERO, -0.1f)
            }
        }
        assertFailsWith<IllegalArgumentException> {
            buildHapticParameterCurve(CurveKind.Sharpness) {
                controlPoints += CurveControlPoint(Duration.ZERO, -1.1f)
            }
        }
    }
}

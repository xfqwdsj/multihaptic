package top.ltfan.multihaptic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

class HapticDslMigrationTest {
    @Test
    fun `generated primitive list validates direct writes`() {
        val builder = HapticEffectDslBuilder()

        assertFailsWith<IllegalArgumentException> {
            builder.primitives += Primitive(PrimitiveType.Click, delay = (-1).milliseconds)
        }
    }

    @Test
    fun `generated keyframe lists validate direct writes`() {
        val builder = HapticCurvesDslBuilder()

        assertFailsWith<IllegalArgumentException> {
            builder.intensity += Keyframe((-1).milliseconds, 0.5f)
        }
        assertFailsWith<IllegalArgumentException> {
            builder.sharpness += Keyframe(0.milliseconds, 1.5f)
        }
    }

    @Test
    fun `generated provider defaults preserve primitive configuration`() {
        val effect = HapticEffect {
            click()
        }

        val primitive = effect.primitives.single()
        assertEquals(BasicPrimitive.Predefined(PrimitiveType.Click), primitive.basic)
        assertEquals(0.milliseconds, primitive.delay)
        assertEquals(DelayType.Pause, primitive.delayType)
    }

    @Test
    fun `curves default independently and repeated blocks append`() {
        val curves = buildHapticCurves {
            intensity { 0.2f at 10.milliseconds }
            intensity { 0.5f at 20.milliseconds }
        }
        assertEquals(listOf(Keyframe(10.milliseconds, 0.2f), Keyframe(20.milliseconds, 0.5f)), curves.intensity)
        assertEquals(listOf(Keyframe(0.milliseconds, 1f)), curves.sharpness)
    }

    @Test
    fun `keyframe requires both mutable values`() {
        val builder = KeyframeDslBuilder()
        assertFailsWith<IllegalStateException> { builder.build() }
        builder.time = 10.milliseconds
        assertFailsWith<IllegalStateException> { builder.build() }
        builder.value = 0.4f
        assertEquals(Keyframe(10.milliseconds, 0.4f), builder.build())
    }

    @Test
    fun `effect combinations preserve primitive order and snapshots`() {
        val builder = HapticEffectDslBuilder()
        val click = Primitive(PrimitiveType.Click)
        val tick = Primitive(PrimitiveType.Tick)
        builder.primitives += click
        val original = builder.build()
        builder.primitives += tick
        assertEquals(listOf(click), original.primitives)
        assertEquals(listOf(click, tick), (original + tick).primitives)
        assertEquals(emptyList(), (original - click).primitives)
        assertEquals(listOf(click, tick), (original + HapticEffect(listOf(tick))).primitives)
    }
}

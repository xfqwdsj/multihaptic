package top.ltfan.multihaptic.vibrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import top.ltfan.multihaptic.HapticEffect
import top.ltfan.multihaptic.PrimitiveType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for AbstractVibrator to verify that multiple identical haptic effects
 * are processed correctly (fixes issue #17).
 */
class AbstractVibratorTest {

    /**
     * A test implementation of AbstractVibrator that records all performed effects.
     */
    private class TestVibrator(coroutineScope: CoroutineScope) : AbstractVibrator(coroutineScope) {
        val performedEffects = mutableListOf<HapticEffect>()

        override suspend fun perform(effect: HapticEffect) {
            performedEffects.add(effect)
        }

        override fun cancel() {
            // No-op for testing
        }
    }

    @Test
    fun testMultipleIdenticalEffectsAreProcessed() = runTest {
        val vibrator = TestVibrator(backgroundScope)
        
        // Create the same haptic effect multiple times
        val effect = HapticEffect {
            predefined(PrimitiveType.Click) {
                scale = 0.3f
            }
        }
        
        // Trigger the same effect 5 times
        repeat(5) {
            vibrator.vibrate(effect)
        }
        
        // Advance time to allow all effects to be processed
        testScheduler.advanceUntilIdle()
        
        // Verify all 5 effects were processed
        assertEquals(5, vibrator.performedEffects.size, "Expected 5 effects to be performed")
        
        // Verify all effects are equal (same content)
        vibrator.performedEffects.forEach { performedEffect ->
            assertEquals(effect, performedEffect, "Performed effect should match the original")
        }
    }

    @Test
    fun testRapidSuccessiveIdenticalEffects() = runTest {
        val vibrator = TestVibrator(backgroundScope)
        
        val effect = HapticEffect {
            predefined(PrimitiveType.Click) {
                scale = 0.5f
            }
        }
        
        // Trigger 10 identical effects rapidly without delay
        repeat(10) {
            vibrator.vibrate(effect)
        }
        
        // Advance time to process all effects
        testScheduler.advanceUntilIdle()
        
        // All 10 should be processed
        assertEquals(10, vibrator.performedEffects.size, "Expected 10 effects to be performed")
    }

    @Test
    fun testDifferentEffectsAreProcessed() = runTest {
        val vibrator = TestVibrator(backgroundScope)
        
        val clickEffect = HapticEffect {
            predefined(PrimitiveType.Click)
        }
        
        val thudEffect = HapticEffect {
            predefined(PrimitiveType.Thud)
        }
        
        // Trigger different effects
        vibrator.vibrate(clickEffect)
        vibrator.vibrate(thudEffect)
        vibrator.vibrate(clickEffect)
        vibrator.vibrate(thudEffect)
        
        testScheduler.advanceUntilIdle()
        
        // All 4 should be processed
        assertEquals(4, vibrator.performedEffects.size, "Expected 4 effects to be performed")
        
        // Verify the order
        assertEquals(clickEffect, vibrator.performedEffects[0])
        assertEquals(thudEffect, vibrator.performedEffects[1])
        assertEquals(clickEffect, vibrator.performedEffects[2])
        assertEquals(thudEffect, vibrator.performedEffects[3])
    }

    @Test
    fun testMixedIdenticalAndDifferentEffects() = runTest {
        val vibrator = TestVibrator(backgroundScope)
        
        val effect1 = HapticEffect {
            predefined(PrimitiveType.Click) {
                scale = 0.3f
            }
        }
        
        val effect2 = HapticEffect {
            predefined(PrimitiveType.Tick)
        }
        
        // Mix of identical and different effects
        vibrator.vibrate(effect1)
        vibrator.vibrate(effect1)
        vibrator.vibrate(effect2)
        vibrator.vibrate(effect1)
        vibrator.vibrate(effect2)
        vibrator.vibrate(effect2)
        
        testScheduler.advanceUntilIdle()
        
        // All 6 should be processed
        assertEquals(6, vibrator.performedEffects.size, "Expected 6 effects to be performed")
    }

    @Test
    fun testSingleEffectIsProcessed() = runTest {
        val vibrator = TestVibrator(backgroundScope)
        
        val effect = HapticEffect {
            predefined(PrimitiveType.Click)
        }
        
        vibrator.vibrate(effect)
        
        testScheduler.advanceUntilIdle()
        
        assertEquals(1, vibrator.performedEffects.size, "Expected 1 effect to be performed")
        assertEquals(effect, vibrator.performedEffects[0])
    }

    @Test
    fun testEffectsWithSameTypeButDifferentParameters() = runTest {
        val vibrator = TestVibrator(backgroundScope)
        
        val effect1 = HapticEffect {
            predefined(PrimitiveType.Click) {
                scale = 0.3f
            }
        }
        
        val effect2 = HapticEffect {
            predefined(PrimitiveType.Click) {
                scale = 0.7f
            }
        }
        
        // These are different effects (different scale)
        vibrator.vibrate(effect1)
        vibrator.vibrate(effect2)
        vibrator.vibrate(effect1)
        
        testScheduler.advanceUntilIdle()
        
        assertEquals(3, vibrator.performedEffects.size, "Expected 3 effects to be performed")
        assertEquals(effect1, vibrator.performedEffects[0])
        assertEquals(effect2, vibrator.performedEffects[1])
        assertEquals(effect1, vibrator.performedEffects[2])
    }

    @Test
    fun testHighFrequencyIdenticalEffects() = runTest {
        val vibrator = TestVibrator(backgroundScope)
        
        val effect = HapticEffect {
            predefined(PrimitiveType.Tick)
        }
        
        // Simulate very high frequency usage (e.g., user rapidly tapping)
        repeat(20) {
            vibrator.vibrate(effect)
        }
        
        testScheduler.advanceUntilIdle()
        
        // All 20 should be processed
        assertEquals(20, vibrator.performedEffects.size, "Expected 20 effects to be performed")
    }

    @Test
    fun testEffectWithMultiplePrimitives() = runTest {
        val vibrator = TestVibrator(backgroundScope)
        
        val effect = HapticEffect {
            predefined(PrimitiveType.Click)
            predefined(PrimitiveType.Tick)
        }
        
        // Trigger the same complex effect multiple times
        repeat(3) {
            vibrator.vibrate(effect)
        }
        
        testScheduler.advanceUntilIdle()
        
        assertEquals(3, vibrator.performedEffects.size, "Expected 3 effects to be performed")
        vibrator.performedEffects.forEach { performedEffect ->
            assertEquals(2, performedEffect.primitives.size, "Each effect should have 2 primitives")
        }
    }

    @Test
    fun testBufferCapacityHandling() = runTest {
        val vibrator = TestVibrator(backgroundScope)
        
        val effect = HapticEffect {
            predefined(PrimitiveType.Click)
        }
        
        // Emit many effects very quickly to test that the buffer can handle rapid emissions
        // All effects should be queued and processed
        repeat(50) {
            vibrator.vibrate(effect)
        }
        
        testScheduler.advanceUntilIdle()
        
        // All or most should be processed
        // With our fix using MutableSharedFlow, we should get significantly more than just 1
        assertTrue(vibrator.performedEffects.size > 1, 
            "Expected more than 1 effect to be processed (got ${vibrator.performedEffects.size})")
        
        // In practice, with proper buffer handling, we should get most or all of them
        assertTrue(vibrator.performedEffects.size >= 40,
            "Expected at least 40 effects to be processed (got ${vibrator.performedEffects.size})")
    }
}

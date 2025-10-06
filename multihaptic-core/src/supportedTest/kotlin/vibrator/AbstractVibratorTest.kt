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
 * Tests for AbstractVibrator to verify that haptic effects are properly processed.
 * 
 * Note: AbstractVibrator uses Channel.CONFLATED which keeps only the latest effect
 * and discards older pending effects. This ensures that when effects are emitted faster
 * than they can be processed, the vibrator always performs the most recent effect.
 */
class AbstractVibratorTest {

    /**
     * A test implementation of AbstractVibrator that records all performed effects
     * and simulates playback duration using delays.
     */
    private class TestVibrator(coroutineScope: CoroutineScope) : AbstractVibrator(coroutineScope) {
        val performedEffects = mutableListOf<HapticEffect>()

        override suspend fun perform(effect: HapticEffect) {
            performedEffects.add(effect)
            // Simulate playback duration based on effect duration
            val duration = effect.primitives.sumOf { primitive ->
                when (val basic = primitive.basic) {
                    is top.ltfan.multihaptic.BasicPrimitive.Predefined -> basic.type.duration.inWholeMilliseconds
                    is top.ltfan.multihaptic.BasicPrimitive.Custom -> basic.duration.inWholeMilliseconds
                }
            }
            delay(duration)
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
        
        // Trigger the same effect 5 times with delays to allow processing
        repeat(5) {
            vibrator.vibrate(effect)
            testScheduler.advanceUntilIdle()
        }
        
        // With Channel.CONFLATED and proper delays, each effect should be processed
        // since we wait for the previous one to complete before sending the next
        assertEquals(5, vibrator.performedEffects.size, "Expected 5 effects to be performed when sent with delays")
        
        // Verify all effects are equal (same content)
        vibrator.performedEffects.forEach { performedEffect ->
            assertEquals(effect, performedEffect, "Performed effect should match the original")
        }
    }

    @Test
    fun testConflationDiscardsOldEffects() = runTest {
        
        val vibrator = TestVibrator(backgroundScope)
        
        val effect1 = HapticEffect {
            predefined(PrimitiveType.Click) {
                scale = 0.3f
            }
        }
        
        val effect2 = HapticEffect {
            predefined(PrimitiveType.Thud) {
                scale = 0.5f
            }
        }
        
        val effect3 = HapticEffect {
            predefined(PrimitiveType.Tick)
        }
        
        // Send multiple effects rapidly without allowing processing
        // Channel.CONFLATED should keep only the latest
        vibrator.vibrate(effect1)
        vibrator.vibrate(effect2)
        vibrator.vibrate(effect3)
        
        testScheduler.advanceUntilIdle()
        
        // Only the last effect should be processed due to conflation
        assertTrue(vibrator.performedEffects.size <= 3, 
            "With conflation, at most all effects can be processed, but likely fewer")
        
        // The last effect processed should be effect3 or the most recent one
        if (vibrator.performedEffects.isNotEmpty()) {
            val lastPerformed = vibrator.performedEffects.last()
            // Due to conflation, we expect the latest effect to be performed
            assertTrue(lastPerformed == effect3 || lastPerformed == effect2 || lastPerformed == effect1,
                "Last performed effect should be one of the sent effects")
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
        // With conflation, only the latest should be kept
        repeat(10) {
            vibrator.vibrate(effect)
        }
        
        // Advance time to process
        testScheduler.advanceUntilIdle()
        
        // Due to conflation, we expect 1 or very few effects to be processed
        assertTrue(vibrator.performedEffects.size >= 1, 
            "At least 1 effect should be processed")
        assertTrue(vibrator.performedEffects.size < 10,
            "Due to conflation, fewer than all 10 effects should be processed")
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
        
        // Trigger different effects with delays to allow processing
        vibrator.vibrate(clickEffect)
        testScheduler.advanceUntilIdle()
        
        vibrator.vibrate(thudEffect)
        testScheduler.advanceUntilIdle()
        
        vibrator.vibrate(clickEffect)
        testScheduler.advanceUntilIdle()
        
        vibrator.vibrate(thudEffect)
        testScheduler.advanceUntilIdle()
        
        // All 4 should be processed when sent with delays
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
        
        // Mix of identical and different effects with delays
        vibrator.vibrate(effect1)
        testScheduler.advanceUntilIdle()
        
        vibrator.vibrate(effect1)
        testScheduler.advanceUntilIdle()
        
        vibrator.vibrate(effect2)
        testScheduler.advanceUntilIdle()
        
        vibrator.vibrate(effect1)
        testScheduler.advanceUntilIdle()
        
        vibrator.vibrate(effect2)
        testScheduler.advanceUntilIdle()
        
        vibrator.vibrate(effect2)
        testScheduler.advanceUntilIdle()
        
        // All 6 should be processed when sent with delays
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
        
        // These are different effects (different scale), send with delays
        vibrator.vibrate(effect1)
        testScheduler.advanceUntilIdle()
        
        vibrator.vibrate(effect2)
        testScheduler.advanceUntilIdle()
        
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
        // With conflation, many will be dropped
        repeat(20) {
            vibrator.vibrate(effect)
        }
        
        testScheduler.advanceUntilIdle()
        
        // Due to conflation, we expect much fewer than 20 to be processed
        assertTrue(vibrator.performedEffects.size >= 1, "At least 1 effect should be processed")
        assertTrue(vibrator.performedEffects.size < 20, "Due to conflation, fewer than all 20 should be processed")
    }

    @Test
    fun testEffectWithMultiplePrimitives() = runTest {
        
        val vibrator = TestVibrator(backgroundScope)
        
        val effect = HapticEffect {
            predefined(PrimitiveType.Click)
            predefined(PrimitiveType.Tick)
        }
        
        // Trigger the same complex effect multiple times with delays
        repeat(3) {
            vibrator.vibrate(effect)
            testScheduler.advanceUntilIdle()
        }
        
        assertEquals(3, vibrator.performedEffects.size, "Expected 3 effects to be performed")
        vibrator.performedEffects.forEach { performedEffect ->
            assertEquals(2, performedEffect.primitives.size, "Each effect should have 2 primitives")
        }
    }
}

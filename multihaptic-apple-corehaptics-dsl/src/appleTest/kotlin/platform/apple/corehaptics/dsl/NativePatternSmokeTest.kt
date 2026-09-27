package top.ltfan.multihaptic.platform.apple.corehaptics.dsl

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertNotNull

class NativePatternSmokeTest {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    @Test
    fun createsNativePatternFromDsl() {
        val pattern = buildHapticPattern { events { transient } }
        assertNotNull(pattern.toNative())
    }
}

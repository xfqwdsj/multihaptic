package top.ltfan.multihaptic.vibrator

import top.ltfan.multihaptic.getVibrator
import kotlin.test.Test

class PlatformVibratorTest {
    private val vibrator = getVibrator(config = null)

    @Test
    fun testSupportReporting() {
        println("isVibrationSupported(${vibrator::class.simpleName}): ${vibrator.isVibrationSupported}")
    }
}

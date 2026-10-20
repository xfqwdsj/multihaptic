package top.ltfan.multihaptic.vibrator

import kotlin.test.Test
import top.ltfan.multihaptic.getVibrator

class PlatformVibratorTest {
    private val vibrator = getVibrator(config = null)

    @Test
    fun testSupportReporting() {
        println(
            "isVibrationSupported(${vibrator::class.simpleName}): ${vibrator.isVibrationSupported}",
        )
    }
}

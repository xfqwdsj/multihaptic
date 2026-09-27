package top.ltfan.multihaptic.platform.android

import top.ltfan.multihaptic.platform.android.dsl.add
import top.ltfan.multihaptic.platform.android.dsl.amplitudeConfig
import top.ltfan.multihaptic.platform.android.dsl.waveformConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class AndroidScalingTest {
    @Test
    fun scalingDoesNotMutateOriginalConfig() {
        val waveform = waveformConfig { add(0.8f, 300f, 10.milliseconds) }
        val scaledWaveform = waveform.scaled(0.5f)
        assertEquals(0.8f, waveform.controlPoints.single().data.amplitude)
        assertEquals(0.4f, scaledWaveform.controlPoints.single().data.amplitude)

        val amplitude = amplitudeConfig { add(200, 10.milliseconds) }
        assertEquals(100, amplitude.scaled(0.5f).controlPoints.single().amplitude)
        assertEquals(200, amplitude.controlPoints.single().amplitude)
    }
}

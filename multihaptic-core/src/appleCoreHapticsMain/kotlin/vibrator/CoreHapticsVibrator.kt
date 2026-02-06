package top.ltfan.multihaptic.vibrator

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import platform.CoreHaptics.CHHapticEngine
import platform.CoreHaptics.CHHapticPattern
import platform.CoreHaptics.CHHapticTimeImmediate
import platform.Foundation.NSError
import platform.apple.dsl.AppleError
import platform.apple.dsl.runThrowing
import top.ltfan.multihaptic.*
import top.ltfan.multihaptic.platform.corehaptics.CHHapticPattern
import kotlin.time.Duration.Companion.milliseconds

class CoreHapticsVibrator internal constructor(coroutineScope: CoroutineScope, private val engine: CHHapticEngine) :
    AbstractVibrator(coroutineScope) {

    override fun isVibrationSupported(): Boolean = true

    @ExperimentalForeignApi
    @BetaInteropApi
    override suspend fun perform(effect: HapticEffect) {
        effect.unpack { basic ->
            val pattern = basic.toPattern() ?: run {
                delay(basic.duration)
                return@unpack
            }

            val player = try {
                runThrowing { engine.createPlayerWithPattern(pattern, it) }
            } catch (e: AppleError) {
                e.printNSErrorInfo()
                return@unpack
            }

            try {
                runThrowing { player?.startAtTime(CHHapticTimeImmediate, it) }
            } catch (e: AppleError) {
                e.printNSErrorInfo()
            } ?: delay(basic.duration)
        }
    }

    @ExperimentalForeignApi
    @BetaInteropApi
    private fun BasicPrimitive.toPattern() = patternOrNull {
        when (this) {
            is BasicPrimitive.Predefined -> when (type) {
                PrimitiveType.Click -> CHHapticPattern(scale) {
                    events {
                        transient {
                            intensity = .8f
                            sharpness = 1f
                        }
                    }
                }

                PrimitiveType.Thud -> CHHapticPattern(scale) {
                    events {
                        transient {
                            intensity = 1f
                            sharpness = .6f
                        }
                        continuous(60.milliseconds, 20.milliseconds) {
                            intensity = .5f
                            sharpness = .2f
                        }
                    }
                }

                PrimitiveType.Spin -> CHHapticPattern(scale) {
                    events {
                        continuous(120.milliseconds) {
                            sharpness = .4f
                        }
                    }
                    curves {
                        intensity {
                            .2f at 0.milliseconds
                            .8f at 40.milliseconds
                            .7f at 80.milliseconds
                            .0f at 120.milliseconds
                        }
                    }
                }

                PrimitiveType.QuickRise -> CHHapticPattern(scale) {
                    events { continuous(50.milliseconds) }
                    curves {
                        intensity {
                            .1f at 0.milliseconds
                            1f at 50.milliseconds
                        }
                        sharpness {
                            .2f at 0.milliseconds
                            1f at 50.milliseconds
                        }
                    }
                }

                PrimitiveType.SlowRise -> CHHapticPattern(scale) {
                    events { continuous(200.milliseconds) }
                    curves {
                        intensity {
                            .1f at 0.milliseconds
                            .7f at 200.milliseconds
                        }
                        sharpness {
                            .1f at 0.milliseconds
                            .5f at 200.milliseconds
                        }
                    }
                }

                PrimitiveType.QuickFall -> CHHapticPattern(scale) {
                    events { continuous(60.milliseconds) }
                    curves {
                        intensity {
                            1f at 0.milliseconds
                            .2f at 60.milliseconds
                        }
                        sharpness {
                            1f at 0.milliseconds
                            .2f at 60.milliseconds
                        }
                    }
                }

                PrimitiveType.Tick -> CHHapticPattern(scale) {
                    events {
                        transient {
                            intensity = 1f
                            sharpness = 1f
                        }
                    }
                }

                PrimitiveType.LowTick -> CHHapticPattern(scale) {
                    events {
                        transient {
                            intensity = .8f
                            sharpness = .8f
                        }
                    }
                }
            }

            is BasicPrimitive.Custom -> CHHapticPattern(scale) {
                events { continuous(duration) }
                curves {
                    intensity {
                        this@toPattern.curves.intensity.forEach { (time, value) -> add(time, value) }
                    }
                    sharpness {
                        this@toPattern.curves.sharpness.forEach { (time, value) -> add(time, value) }
                    }
                }
            }
        }
    }

    @ExperimentalForeignApi
    @BetaInteropApi
    private inline fun patternOrNull(block: (ptr: CPointer<ObjCObjectVar<NSError?>>) -> CHHapticPattern) = try {
        runThrowing(block)
    } catch (e: AppleError) {
        e.printNSErrorInfo()
        null
    }
}

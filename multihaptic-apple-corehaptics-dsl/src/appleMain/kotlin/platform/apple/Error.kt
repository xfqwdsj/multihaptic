package top.ltfan.multihaptic.platform.apple

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError

public class AppleError(public val nsError: NSError) : Error("AppleException: ${nsError.localizedDescription}") {
    public fun printNSErrorInfo() {
        nsError.printInfo()
    }
}

@ExperimentalForeignApi
@BetaInteropApi
public inline fun <R> runThrowing(block: (ptr: CPointer<ObjCObjectVar<NSError?>>) -> R): R = memScoped {
    val nsError: ObjCObjectVar<NSError?> = alloc()
    val result = block(nsError.ptr)
    nsError.value?.let { throw AppleError(it) }
    result
}

public fun NSError.printInfo() {
    println("NSError Domain: $domain, Code: $code")
    println("Description: $localizedDescription")
    userInfo.forEach { (key, value) ->
        println("User Info: $key = $value")
    }
}

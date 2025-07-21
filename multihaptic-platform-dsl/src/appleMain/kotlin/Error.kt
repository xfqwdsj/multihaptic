package platform.apple.dsl

import kotlinx.cinterop.*
import platform.Foundation.NSError

class AppleError(val nsError: NSError) : Error("AppleException: ${nsError.localizedDescription}") {
    fun printNSErrorInfo() {
        nsError.printInfo()
    }
}

@ExperimentalForeignApi
@BetaInteropApi
inline fun <R> runThrowing(block: (ptr: CPointer<ObjCObjectVar<NSError?>>) -> R) = memScoped {
    val nsError: ObjCObjectVar<NSError?> = alloc()
    val result = block(nsError.ptr)
    nsError.value?.let { throw AppleError(it) }
    result
}

@OptIn(UnsafeNumber::class)
fun NSError.printInfo() {
    println("NSError Domain: $domain, Code: $code")
    println("Description: $localizedDescription")
    userInfo.forEach { (key, value) ->
        println("User Info: $key = $value")
    }
}

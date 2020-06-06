package exception

import kotlinx.coroutines.*
import java.io.IOException

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught original $exception")
    }

    val scope = CoroutineScope(Job())

    val deferred = scope.async(handler) {
        delay(100)
        throw IOException()
        Unit
    }

    try {
        delay(1000)
        deferred.await()
    } catch (e: Exception) {
        println("exception")
    }
    deferred.join()
}
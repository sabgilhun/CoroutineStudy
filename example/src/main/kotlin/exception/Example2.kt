package exception

import kotlinx.coroutines.*
import java.io.IOException

fun main() = runBlocking {
    val scope = CoroutineScope(Job())
    val deferred = scope.async {
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
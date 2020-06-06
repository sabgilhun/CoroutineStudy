package cancellation

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val deferred = async {
        repeat(100) {
            delay(500)
            println("running")
        }
    }

    delay(3000)
    deferred.cancel()
    deferred.await()
}
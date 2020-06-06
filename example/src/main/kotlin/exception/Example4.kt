package exception

import kotlinx.coroutines.*
import java.io.IOException

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught original $exception")
    }

    val scope = CoroutineScope(Job())
    val job = scope.launch(handler) {
        async {
            delay(100)
            throw IOException()
        }
    }

    job.join()
}
package chapter9

import kotlinx.coroutines.*
import java.io.IOException

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught original $exception")
    }
    val parentJob = GlobalScope.launch(handler) {
        val childJob = launch {
            // Sub-child job
            launch {
                // Sub-child job
                launch {
                    throw IOException()
                }
            }
        }

        try {
            childJob.join() // delay로 기달려도 똑같음
        } catch (e: CancellationException) {
            println("Rethrowing CancellationException with original cause")
            throw e
        }
    }
    parentJob.join()
}
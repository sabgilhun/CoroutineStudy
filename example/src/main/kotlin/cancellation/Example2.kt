package cancellation

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val job = launch {
        repeat(100) {
            delay(500)
            println("running")
        }
    }

    delay(3000)
    job.cancel()
    job.join()
    println("no exception")
}
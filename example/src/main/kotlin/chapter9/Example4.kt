package chapter9

import kotlinx.coroutines.*
import java.io.IOException

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught original $exception")
    }
    val parentJob = GlobalScope.launch {
        val child = launch(handler) {
            delay(10)
//            throw IOException()
            throw CancellationException()
        }
        child.join()

        println("d")
        delay(200)
        // IOException 발생하면 호출안됨
        // CancellationException 발생하면 호출됨
        println("finish")
    }
    parentJob.join()
}

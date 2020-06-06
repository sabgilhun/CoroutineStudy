package chapter9

import kotlinx.coroutines.*

fun main() = runBlocking {
    val parentJob = GlobalScope.launch {
        val child = launch {
            repeat(2) {
                Thread.sleep(500)
                println(it)
            }

            throw CancellationException()
        }
        delay(750)
//        child.cancel()
//        child.cancelAndJoin()
        println("finish")
    }
    parentJob.join()
}

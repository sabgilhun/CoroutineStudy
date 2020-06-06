package chapter9

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // 자식의 예외가 부모에게 전파됨
    val job = GlobalScope.launch {
        try {
            delay(1000)
            println("parent job is finished")   // 예외가 전파 되기 때문에 도달하지 못함
        } catch (e: Exception) {
            println("occurred from child coroutine")
        }
    }

    launch(job) {
        delay(500)
        throw IllegalStateException()
    }
    job.join()
}
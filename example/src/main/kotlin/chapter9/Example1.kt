package chapter9

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Join 으로 연결된 job에겐 예외 전파 안됨
    val job = GlobalScope.launch {
        repeat(100) {
            println(it)
            delay(500)
            if (it == 1) error("")
        }
    }

    try {
        job.join()
        println("first job complete")
    } catch (e: Exception) {
        println("occurred from waiting coroutine")
    }
}
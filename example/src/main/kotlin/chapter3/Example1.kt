package chapter3

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun main(args: Array<String>) {
    val job1 = GlobalScope.launch(
        context = (Dispatchers.Default as CoroutineContext + Dispatchers.IO),
        start = CoroutineStart.LAZY
    ) {
        // 코루틴 시작
        delay(200)
        println("Pong")
        delay(200)
    }
    GlobalScope.launch {
        delay(200)
        println("Ping")
        job1.join() // 위의 코루틴의 Job 객체를 이용해서 위의 코루틴 Complete 상태까지 대기
        println("Ping")
        delay(200)
    }
    Thread.sleep(1000)
}
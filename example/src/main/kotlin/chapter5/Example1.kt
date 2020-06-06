package chapter5

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


fun main(args: Array<String>) {
    val job = GlobalScope.launch {}



    Thread.sleep(1000)
    job.cancel()

    Thread.sleep(3000)
}

suspend fun test(scope: CoroutineScope) {
    var cnt = 0
    while (true) {
        println("running: ${cnt++}")
//        Thread.sleep(100)
        delay(100)
    }
}
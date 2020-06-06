package chapter9

import kotlinx.coroutines.*

fun main() {
    val parentJob = Job()
    val scope = CoroutineScope(parentJob)

    val job = scope.launch(Dispatchers.IO) {
        delay(100)
        println("1:${this.coroutineContext[Job]}")
        launch {
            repeat(10) {
                delay(100)
                println("3:${this.coroutineContext[Job]}")
            }
        }
    }

    val job2 = scope.launch(Dispatchers.IO) {
        delay(100)
        println("1:${this.coroutineContext[Job]}")
        launch {
            repeat(10) {
                delay(100)
                println("3:${this.coroutineContext[Job]}")
            }
        }
    }

    Thread.sleep(300)

    if (parentJob.children.iterator().hasNext()) {
        println("The Job has children ${parentJob.children}")
    } else {
        println("The Job has NO children")
    }

    Thread.sleep(10000)
}
package cancellation

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val parentJob = Job()

    launch(parentJob) {
        repeat(100) {
            delay(500)
            println("child1")
        }
    }

    launch(parentJob) {
        repeat(100) {
            delay(500)
            println("child2")
        }
    }

    launch(parentJob) {
        repeat(100) {
            delay(500)
            println("child3")
        }
    }

    delay(3000)
    parentJob.cancel()
    println("parent cancel")
}
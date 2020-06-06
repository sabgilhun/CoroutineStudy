package chapter3

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main(args: Array<String>) {
    with(GlobalScope) {
        val parentJob = launch {
            launch {
                delay(200)
                println("I’m a child")
                delay(200)
            }
            delay(200)
            println("I’m the parent")
            delay(200)
        }

        Thread.sleep(5)
        if (parentJob.children.iterator().hasNext()) {
            println("The Job has children ${parentJob.children}")
        } else {
            println("The Job has NO children")
        }
        Thread.sleep(1000)
    }
}
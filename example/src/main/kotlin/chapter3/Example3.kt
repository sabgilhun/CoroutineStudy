package chapter3

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    var isDoorOpen = false
    println("Unlocking the door... please wait.\n")

    Executors.newWorkStealingPool().asCoroutineDispatcher()

    GlobalScope.launch {
        delay(3000)
        isDoorOpen = true
    }
    GlobalScope.launch {
        repeat(4) {
            println("Trying to open the door...\n")
            delay(800)
            if (isDoorOpen) {
                println("Opened the door!\n")
            } else {
                println("The door is still locked\n")
            }
        }
    }
    thread { }
    Thread.sleep(5000)
}
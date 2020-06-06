package chapter4

import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.suspendCoroutine


fun main(args: Array<String>) {
    val a: MutableSet<Item> = TreeSet(
        listOf(
            Item("20201231"),
            Item("20200101"),
            Item("20200301"),
            Item("20200201")
        )
    )
    println(a)

    a.addAll(listOf(Item("20191231")))
    println(a)

//    GlobalScope.launch(Dispatchers.Default) {
//        suspendFunctionWithDelay(1, 2)
//    }
//
//    Thread.sleep(5000)
}

data class Item(
    private val label: String
) : Comparable<Item> {
    override fun compareTo(other: Item): Int =
        when {
            this.label > other.label -> -1
            this.label == other.label -> 0
            else -> 1
        }
}

suspend fun suspendFunctionWithDelay(a: Int, b: Int): Int {
    val executor = Executors.newScheduledThreadPool(1)

    println(Thread.currentThread().name + ": start")

    suspendCoroutine<Unit> { cont ->
//        executor.schedule({ cont.resume(Unit) }, 1000, TimeUnit.MILLISECONDS)
    }

    println(Thread.currentThread().name + ": finish")
    return a + b
}

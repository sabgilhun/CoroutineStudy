import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Example1 {
    @JvmStatic
    fun main(args: Array<String>) {
        val job1 = GlobalScope.launch(start = CoroutineStart.LAZY) {
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

}

object Example2 {
    @JvmStatic
    fun main(args: Array<String>) {
        with(GlobalScope) {
            val parentJob = launch {
                delay(200)
                println("I’m the parent")
                delay(200)

            }
            launch (context = parentJob) {
                delay(200)
                println("I’m a child")
                delay(200)
            }
            if (parentJob.children.iterator().hasNext()) {
                println("The Job has children ${parentJob.children}")
            } else {
                println("The Job has NO children")
            }
            Thread.sleep(1000)
        }
    }
}


object Example3 {
    @JvmStatic
    fun main(args: Array<String>) {
        var isDoorOpen = false
        println("Unlocking the door... please wait.\n")
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
        Thread.sleep(5000)
    }
}

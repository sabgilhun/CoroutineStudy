## Chapter 9: Manage Cancellation

여러개의 비동기 작업이 서로에 대해 의존하는 상황에서의 작업 취소는 버그를 발생키실 수 있다. 코루틴에서는 이러한 문제를 해결 할 수 있는 방법을 제공하고 있다. 이번 챕터에서는 이에 대해 알아보자.



### Cancelling a coroutine

일반적인 멀티 스레딩과 동일하게 코루틴의 lifecycle 개념은 메모리 누수, 크래시와 같은 문제를 일으킬 수 있다. 이를 위해 코루틴에서는 간단하게 작업을 취소할 수 있는 방법을 제공한다.



### Job object

`launch()` 를 통해 반환되는 `Job` 객체는 동작중인 코루틴을 나타내며 이는 어느 시점이든 `cancel()` 을 통해 취소 될 수 있다. 또한 `Job`은 상위 job을 context를 통해 지정할 수 있으며 상위 job이 취소되면 하위 job도 같이 취소된다.

일반적으로 코루틴 내부에서 exception 발생시 시스템은 예상치 못한 예외로 다루어 프로그램을 종료시킨다. 여기서 추가로 `join()` 을 이용해서 Job을 연결시키는 것은 예외를 전파하지 않는다. 하지만 Job의 종속관계는 예외를 전파하여 자식 Job에서 예외가 발생하면 부모 Job도 취소된다.

```kotlin
fun main() = runBlocking {
    // Join 으로 연결된 job에겐 예외 전파 안됨	(하지만 await은 에러를 전파한다.)
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
```



```kotlin
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
```



### Cancle

코루틴 빌더를 통해 생성된 `Job`, `Deferred` 객체의 `cancle()` 을 호출하면 만약 내부에서 적절하게 `isActive` 플레그를 체크하고 있다면 코루틴은 멈출것이다. 그리고 suspending 함수는 내부에서 주기적으로 `isActive` 를 체크하고 false라면 해당 코루틴은 취소된다. 그렇기 때문에 내부에서 긴 작업을 할때에만 `isActive` 를 수동으로 체크해주면 된다.



### CancellationException

일반적인 코루틴은 취소 되면 CancellationException을 발생시키며 이는 모든 코루틴 에러 핸들러에서 무시된다. 그렇기 때문에 일반적으로는 자식 코루틴에서 예외가 발생하면 부모에게 전달되어 종료되지만 CancellationException은 핸들러에 의해 무시되기 때문에 계속 진행된다.



```kotlin
fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught original $exception")
    }
    val parentJob = GlobalScope.launch(handler) {
        val child = launch {
            delay(10)
            throw IOException()
//            throw CancellationException()
        }
        child.join()
        delay(200)
        println("finish") // IOException 발생하면 호출안됨
      										// CancellationException 발생하면 호출됨
    }
    parentJob.join()
}
```



### Join, CancelAndJoin and CancelChildren

코루틴 스탠다드 라이브러리에서 지원하는 완료, 취소를 핸들링 할 수 있는 함수들을 확인해보자.

* Join() : Join 함수는 다른 해당 코루틴이 취소 or 완료될때까지 기다린다.
* JoinAll() : 여러 job이 다 끝날때까지 기다린다.
* cancelAndJoin() : 작업을 취소하고 취소가 완료되길 기다린다.

```kotlin
fun main() = runBlocking {
    val parentJob = GlobalScope.launch {
        val child = launch {
            repeat(2) {
                Thread.sleep(500)
                println(it)
            }
            throw CancellationException()
        }
        delay(750)
        child.cancel()
      	// 1, finish, 2
        child.cancelAndJoin()
				// 1, 2, finish
        println("finish")
    }
    parentJob.join()
}
```

* cancelChildren : 자식 코루틴을 모두 취소한다.



### TimeOut

코루틴을 시작하고 특정 시간이 지났을때 해당 코루틴을 취소하고 싶을때는 `withTimeout` 을 사용하면 된다. 이 함수는 특정 시간이 지나면 `TimeoutCancellationException` 을 던지는데 우리는 이를 error handler나 try-catch로 핸들링하면 된다.
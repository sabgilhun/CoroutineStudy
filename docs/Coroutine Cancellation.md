## Cancellation in coroutines

더 이상 필요하지 않은 작업을 계속 진행하는 프로그램은 비효율이기 때문에 코루틴이 진행중인 작업이 필요 없어진다면 바로 취소시킬 수 있어야 한다. 



### 코루틴 취소하기

코틀린 코루틴에서는 Job, Scope에 cancel을 호출함으로써 관련 코루틴을 취소 시킬 수 있다.

첫번째로 Job은 코루틴 빌더를 통해 시작되면서 반환되는 객체로써 아래와 같이 코루틴을 취소 할 수 있다.

```kotlin
val job1 = scope.launch { … }
job1.cancel()
```



이에 추가로 여러 코루틴을 한번에 컨트롤 하기위해 Scope 개념(Job Hierarchy)이 존재한다. 아래와 같이 같은 scope를 통해 생성된 job1, job2는 scope를 취소함으로써 같이 취소된다.

( Scope를 취소한다. == 부모 Job을 취소한다. Scope.cancel 구현체를 보면 알 수 있음 )  

```kotlin
val parentJob = Job()
val scope = CoroutineScope(parentJob)

val job1 = scope.launch { … }
val job2 = scope.launch { … }

scope.cancel() // parentJob.cancel() 호출과 동일
```



### Job 계층 구조와 취소 규칙

Job을 통해 코루틴의 계층을 만들 수 있다. 방법은 아래와 같다.

```kotlin
val parentJob = Job()
val scope = CoroutineScope(parentJob)

val job1 = scope.launch { … }		
val job2 = scope.launch { … }		// job1, job2는 parentJob의 자식 코루틴

////////////////////////////////////////////////////////////////////////////////////////

val parentJob = Job()
CoroutineScope(parentJob).lauch {
  val job1 = launch { … }		
	val job2 = launch { … }		// job1, job2는 parentJob의 자식 코루틴
}
```



계층화 되어 있는 Job의 취소 동작은 몇가지의 규칙을 따른다.

1. 부모 Job을 취소하면 자식 Job은 모두 취소된다.
2. 자식 Job중 하나를 취소해도 다른 Job에게 영향을 주지 않는다.
3. 한번 취소된 Job으로는 다른 Job 계층을 만들 수 없다. 
	(여기서 주의해야할것은 error 없이 코루틴이 시작이 안되기 때문에 Job이 취소 될 수 있는지 유의하며 개발해야한다.)

이러한 규칙을 이해하면 여러 코루틴을 컨트롤 할때 좀 더 편리하게 취소 할 수 있을 것이다.



### 코루틴에서 Cancel을 다루는 방식

cancel을 호출하면 내부적으로 코루틴은 cancellaction 예외를 발생시켜 코루틴을 취소한다. 그리고 이 예외는 부모에게 전달되고 부모는 예외 객체가 무엇인지 확인해여 Cancellation이면 더 이상 전파하지 않는다.



### 코루틴 취소가 제대로 동작하게 하기

코루틴을 취소하더라도 취소될 코루틴이 resume 되지 않으면 실제로 취소가 되지 않는다. Suspend func을 호출하고 끝이나면 결국 resume이 호출되면서 취소 동작이 이루어지기 때문에 신경쓰지 않아도 되지만, 그렇지 않은 경우는 `job.isActive`, `ensureActive()`를 통해 주기적으로 확인해줘야 한다. 또 다른 방법으로는 `yeild()`를 호출함으로써 강제적으로 suspend point를 만들어 주는 방법도 있다.



### Job.join, Defferd.await 에서의 Cancellation

join, await 모두 해당 작업이 끝날때까지 기다리는 suspend 함수이다. 하지만 cancel 할때는 동작이 달라지기 때문에 인지하고 있어야한다.

join

* job.cancel -> job.join : 해당 job이 종료될때까지 대기

await

* defferd.cancel -> defferd.await : JobCancellationException 예외 발생 await 특성상 값을 리턴해야 하는데 그걸 못하기 때문에 예외로 처리함



### 작업 취소를 할때 생길 수 있는 Side Effect

만약 코루틴에서 어떠한 작업을 하며 필요한 리소스가 있다고 가정해보자. 이때 해당 코루틴을 취소하면서 필요한 리소스도 같이 정리되어야 한다.

* isActive를 사용하여 해제 해준다.

	```kotlin
	while (i < 5 && isActive) {
	    // print a message twice a second
	    if (…) {
	        println(“Hello ${i++}”)
	        nextPrintTime += 500L
	    }
	}
	// the coroutine work is completed so we can cleanup
	println(“Clean up!”)
	```

* Try-catch를 사용해서 해제 해준다.

	```kotlin
	val job = launch {
	   try {
	      work()
	   } catch (e: CancellationException){
	      println(“Work cancelled!”)
	    } finally {
	      withContext(NonCancellable){
	         delay(1000L) // or some other suspend fun 
	         println(“Cleanup done!”)
	      }
	    }
	}
	delay(1000L)
	println(“Cancel!”)
	job.cancel()
	println(“Done!”)
	```

	만약 finally에서 리소스를 해제할때 해제 작업도 하나의 suspend 함수라면 `withContext(NonCancellable)`를 써주어야 한다. 

* suspendCancellableCoroutine 을 사용한다면 invokeOnCancellation을 이용해서 쉽게 해제 가능하다.

	```kotlin
	suspend fun a() = suspendCancellableCoroutine<Unit> { cont ->
			cont.invokeOnCancellation { println(“Cleanup done!”) }
			workSomeThing()
	    cont.resume(Unit)
	}
	```

	


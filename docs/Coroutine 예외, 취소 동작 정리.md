## Cancellation

### 기본 원칙

* 부모 Job 취소시 자식 Job 모두 취소 (이때 CancellationException 전달)
* 자식 Job이 취소되면 부모에게 CancellationException 전달하지만 다른 자식들에게 전파하지 않음



### 예외 케이스

1. job.cancel -> job.join : 문제 없이 동작

	```kotlin
	fun main() = runBlocking {
	    val job = launch {
	        repeat(100) {
	            delay(500)
	            println("running")
	        }
	    }
	
	    delay(3000)
	    job.cancel()
	    job.join()
	    println("no exception")
	}
	```

2. deferred.cancel -> deferred.await : JobCancellationException 예외 발생 (await에서 기다리는 값을 전달해줄 수 없기 때문)

	```kotlin
	fun main() = runBlocking {
	    val deferred = async {
	        repeat(100) {
	            delay(500)
	            println("running")
	        }
	    }
	
	    delay(3000)
	    deferred.cancel()
	    deferred.await()			// JobCancellationException 발생
	}
	```



## Exception 

### 기본 원칙

* 일반 `Job`은 자식 `Job`에서 발생한 예외 부모로 전달되며 `CancellationException`를 제외한 예외는 다른 자식들에게 `CancellationException` 예외로 전달된다.
* 부모로 예외 전달을 막고 싶으면 try/catch를 써야한다.
* `SupervisorJob`은 자식 `Job`에서 발생한 예외를 다른 자식 `Job`에게 전달하지 않는다.
* 전달된 예외를 부모에서 잡지 않으면 프로그램이 죽는다.
* `CoroutineExceptionHandler`은 항상 최상단 부모에 설정되어야 하며 다른 곳에 설정된 것은 무시된다.



### 예외 케이스

1. async에 `CoroutineExceptionHandler` 설정 안된다.

	```kotlin
	fun main() = runBlocking {
	    val handler = CoroutineExceptionHandler { _, exception ->
	        println("Caught original $exception")
	    }
	
	    val scope = CoroutineScope(Job())
	
	    val deferred = scope.async(handler) {
	        delay(100)
	        throw IOException()
	        Unit
	    }
	
	    try {
	        delay(1000)
	        deferred.await()
	    } catch (e: Exception) {
	        println("exception")
	    }
	    deferred.join()
	}
	```

2. launch 블럭안의 예외는 바로 발생하지만 async 블럭안의 예외는 await 시점에 발생한다.

	```kotlin
	fun main() = runBlocking {
	    val handler = CoroutineExceptionHandler { _, exception ->
	        println("Caught original $exception")
	    }
	
	    val scope = CoroutineScope(Job())
	    val job = scope.launch(handler) {
	        delay(100)
	        throw IOException()
	    }
	
	    job.join()
	}
	
	fun main() = runBlocking {
	    val scope = CoroutineScope(Job())
	    val deferred = scope.async {
	        delay(100)
	        throw IOException()
	        Unit
	    }
	
	    try {
	        delay(1000)
	        deferred.await()
	    } catch (e: Exception) {
	        println("exception")
	    }
	    deferred.join()
	}
	```

3. root 코루틴이 async가 아니라면 바로 예외 발생한다.

	```kotlin
	fun main() = runBlocking {
	    val handler = CoroutineExceptionHandler { _, exception ->
	        println("Caught original $exception")
	    }
	
	    val scope = CoroutineScope(Job())
	    val job = scope.launch(handler) {
	        async {
	            delay(100)
	            throw IOException()
	        }
	    }
	
	    job.join()
	}
	```

	
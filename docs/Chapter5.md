## Chapter 5: Async/Await

코루틴은 suspend 개념을 이용해 동기, 비동기 방식 모두 순차적 코드 구조로 구현할 수 있다. 이번장에서는 suspendable function들이 내부적으로 어떻게 동작하는지 확인하고 기존 Callback 구조의 비동기 처리 방식을 코루틴으로 변환하는 방법을 살펴 볼 것이다.



### Suspending vs. non-suspending

코루틴은 suspending code, suspending functions에 기반한 컨셉을 갖고 있다. suspending code는 시스템에서 중지킬 수 있고, 재개할 수 있다는 점을 제외하면 일반적인 코드와 동일하다. 그리고 호출 방식은도 비슷한데 suspending function은 launch 블럭에서 호출해야한다는 점만 제외하면 일반 함수와 동일하다.

시스템은 `suspend` 수식어를 이용해 이 둘을 구별하여 compile 시점에 차별화 시킨다. (다르게 컴파일된다.) 이를 확인하기 위해 ByteCode를 분석하여 확인해보자.



### Analyzing a regular function

코틀린 코드

```kotlin
fun getUserStandard(userId: String): User { 
  Thread.sleep(1000)
  return User(userId, "Filip")
}
```

디컴파일된 자바 코드

```java
@NotNull
public static final User getUserStandard(@NotNull String userId)
{
  Intrinsics.checkParameterIsNotNull(userId, "userId"); 
  Thread.sleep(1000L);
  return new User(userId, "Filip");
}
```

일반 함수는 코틀린 코드와 디컴파일된 자바 코드의 차이가 거의 없다. 



### Implementing the function with callbacks

위의 코드는 UI 스레드를 sleep하기 때문에 좋은 방식이 아니기에 이를 callback 형태로 바꿔보자.

```kotlin
fun getUserFromNetworkCallback(
  userId: String,
  onUserReady: (User) -> Unit) { 
  thread {
    Thread.sleep(1000)
    val user = User(userId, "Filip")
    onUserReady(user)
  }
  println("end")
}

fun main() { 
  getUserFromNetworkCallback("101") { user -> println(user) }
  println("main end")
}
```

위의 코드는 새로운 스레드에서 sleep을 하고 끝나면 onUserReady를 호출하는 콜백방식이다. 이 코드를 자바 디컴파일 해보자.

```java
public static final void getUserFromNetworkCallback (
  @NotNull final String userId,
  @NotNull final Function1 onUserReady) 
{
  Intrinsics.checkParameterIsNotNull(userId, "userId");						// null check
  Intrinsics.checkParameterIsNotNull(onUserReady, "onUserReady"); // null check
  
  ThreadsKt.thread$default(									
    false, false, (ClassLoader) null,
    (String) null, 0,
    (Function0) (new Function0() {	// Function0 정의
      // $FF: synthetic method
      // $FF: bridge method
      public Object invoke() {
        this.invoke();
        return Unit.INSTANCE;
      }

      public final void invoke() {
        Thread.sleep(1000L);
        User user = new User(userId, "Filip");
        onUserReady.invoke(user);
      }
    }),
    31, (Object) null);
  String var2 = "end";
  System.out.println(var2);
}
```

많은 변화가 생겼지만 정리해보면 스레드를 만들고 `public final void invoke()` 를 호출한다. 여기서는 1000ms sleep후 user객체를 만들어 onUserReady에 넘겨준다. 이 방식은 맨 처음 방식보다는 좋지만 main thread가 종료되어도 thread가 살아있을 수 있다는 문제점을 갖고 있다. 



### Handling happy and unhappy paths

Handling happy and unhappy paths의 의미는 프로그램의 최상의 경우의 수, 최악의 경우의 수를 말한다. 위의 코드에선 최악의 경우를 생각해보면 백그라운드로 넘긴 block에서 예외가 발생하는 상황을 들 수 있고 **이런 상황의 대처법은 함수 호출을 try/catch로 감싸거나 스레드 내부에서 예외를 잡는 방법이 있다.** 

전자의 경우 좋지 않은 방법이고 차라리 후자가 낫다. 하지만 후자의 경우도 callback에 대한 값을 return하는 대신에 예외에 대한 값은 return해줘야 한다. 결국 이를 위해 nullable한 코드와 불필요한 파라미터가 추가된다.

```kotlin
fun getUserFromNetworkCallback(
  userId: String,
  onUserResponse: (User?, Throwable?) -> Unit) { 
  thread {
    try {
      Thread.sleep(1000)
      val user = User(userId, "Filip")
      onUserResponse(user, null) 		// 일반적인 상황에서 Throwable을 null로 반환
    } catch (error: Throwable) {
      onUserResponse(null, error)		// 에러, 예외 상황에서 값(user)를 null로 반환
    }
}
```

이 방법은 위에서 언급한 방법들 보다 낫지만 결국 호출이 많아지면 생기는 **콜백지옥** 그리고 매번 Thread를 만드는데에 생기는 **OverHaed**를 해결할 수 없다.



### Analyzing a suspendable functinon

일반 함수의 callback을 이용할때 생겼던 아래와 같은 문제점들을 코루틴을 통해 해결할 수 있다.

* 중첩된 callback 구조가 사용될때의 콜백지옥 현상
* Try/catch로 감싸야 하는 에러 핸들링
* 항상 새로운 thread를 생성하여 생기는 Overhead



```kotlin
suspend fun getUserSuspend(userId: String): User {
  delay(1000)				// suspendable function
  return User(userId, "Filip")
}
```

위의 코드는 일반 함수에서 callback으로 만들었던 코드와 거의 동일한 동작을 하는 코드이다. 이를 자바 코드로 디컴파일 해보면 아래와 같다.

```java
@Nullable
public static final Object getUserSuspend(
@NotNull String userId,
@NotNull Continuation var1) {
  Object $continuation;
  
  label28: {
    if (var1 instanceof < undefinedtype >) { 
      $continuation = (<undefinedtype>)var1;
      if ((((<undefinedtype>)$continuation).label & Integer.MIN_VALUE) != 0) {
        ((<undefinedtype>)$continuation).label -= Integer.MIN_VALUE;
        break label28;	// break label -> label 블럭 밖으로 pc 이동
      }
    }
    
    $continuation = new ContinuationImpl(var1) {
    // $FF: synthetic field
      Object result;
      int label;
      Object L $0;
      @Nullable
      public final Object invokeSuspend (@NotNull Object result) {
        this.result = result;
        this.label | = Integer.MIN_VALUE;
        return MainKt.getUserSuspend((String)null, this);
      } 
    };
  } // label28
  
  Object var2 =((<undefinedtype>)$continuation).result;
  Object var4 = IntrinsicsKt.getCOROUTINE_SUSPENDED ();
  
  switch(((<undefinedtype>)$continuation).label) {
    case 0:
      if (var2 instanceof Failure) {
        throw ((Failure) var2).exception; 
      }
      
      ((<undefinedtype>)$).L$0 = userId;
      ((<undefinedtype>)$continuation).label = 1;
      
      if (DelayKt.delay(1000L, (Continuation)$continuation) == var4) {
        return 
          ;
      }
      break;
      
    case 1:
      userId = (String)((<undefinedtype>)$continuation).L$0; 
      if (var2 instanceof Failure) { 
        throw ((Failure) var2).exception; 
      }
      break;
      
    default:
      throw new IllegalStateException ("call to ’resume’ before ’invoke’ with coroutine"); 
  }
  
  return new User (userId, "Filip");
}
```

많은 양의 코드가 추가되었다. 하나씩 살펴보자.

* 함수 시그니처를 보면 Continuation 파라미터가 추가된 것을 볼 수 있다. suspend function은 return을 이용하여 call site를 돌아가는게 아니라 이 Continuation을 통해 돌아간다.
* 사실은 모든 함수는 Continuation를 갖는다. 이는 위에서 말한 용도와 같다. suspendable function은 여기에 추가로 코루틴용 Continuation을 갖는다. 
* suspend function 내부의 suspend point가 각각의 flow 표현한다. 예를 들어 suspend point가 2개가 있다면 생성되는 Continuation은 2개의 상태를 갖게 되면 각각의 point가 종료되면 그 상태에 맞게 Continuation이 호출된다.
* 각각의 상태는 Continuation의 label값으로 체크된다. suspend point에 작업이 끝나면 caller의 Continuation label의 카운트를 올려준다.
* 마지막으로 모든 flow를 마치면 suspend function의 caller continuation을 resume 해준다.

짧게 말해 시스템은 작은 state-macthines을 continuation을  suspend function을 구현하고 있는 것이다.




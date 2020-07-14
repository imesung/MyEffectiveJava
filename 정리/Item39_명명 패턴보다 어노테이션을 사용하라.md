## 명명 패턴보다 어노테이션을 사용하라

테스트 프레임워크인  JUnit은 버전 3까지 테스트 메서드 이름을 test로 시작하게끔 했다. 하지만 이런 명명패턴은 단점이 존재했다.

## 
**명명패턴의 단점**

1. 오타가 나면 안된다.
   - 실수로 tsetSafetyOverride로 지으면 JUnit 3은 이 메소드를 무시하고 지나치기 때문에 해당 테스트가 통과했다고 오해할 수 있다.
2. 올바른 프로그램 요소에서만 사용되리라 보증할 방법이 없다.
   - 메서드가 아닌 클래스의 이름을 TestSafetyMechanisms으로 지어 JUnit에 던져줄 시 JUnit은 클래스의 이름에는 관심이 없어 무시해버린다.
   - 그로 인해, 테스트를 하지 않고 지나치기 때문에 개발자는 이번에도 테스트가 통과했다고 오해할 수 있다.
3. 프로그램 요소를 매개변수로 전달할 마땅한 방법이 없다는 것이다.
   - 만약, 특정 예외를 던져야만 성공하는 테스트가 있다고 가정해보자.
     - 즉, **기대하는 예외 타입을 테스트의 매개변수로 전달해야하는 상황이다.**
     - 이때 구분 방법은, **예외의 이름을 테스트 메소드 이름에 덧붙이는 방법이 있지만 보기도 그렇고 깨지기도 쉽다.**
     - 또한, **컴파일러는 메소드 이름에 덧붙인 문자열이 예외를 가리키는지 알 도리가 없다.**

**어노테이션이 이 모든 문제를 해결해준다. 이는 JUnit 버전 4부터 전면 도입되었다.**


## 
### 테스트 프레임워크를 사용하여 어노테이션의 동작방식을 살펴보자

~~~java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
    
}
~~~

@Retention과 @Target의 메타 어노테이션을 사용해서 어노테이션을 생성하고 있다.

- @Retention(RetentionPolicy.RUNTIME) : 런타임까지 유효한 어노테이션
- @Target(ElementType.METHOD) : 해당 어노테이션은 메소드 선언에서만 사용



## 
### **마커 어노테이션**

마커 어노테이션은 '아무 매개변수 없이 단순히 대상에 마킹한다'는 뜻에서 마커 어노테이션이라 한다.

해당 어노테이션을 사용하면 **Test이름에 오타를 내거나 메소드 선언 외의 프로그램 요소에 달면 컴파일 오류**를 내준다.

~~~java
public class Sample {
    
    @Test
    public static void m1() {}	//성공해야 한다.
    
    public static void m2() {}
    
    @Test
    public static void m3() {		//실패해야 한다.
        throw new RuntimeException("실패");
    }
    
    public static void m4() {}

    @Test
    public void m5() {}					//잘못 사용했다. 정적 메소드가 아니다.
    
    public static void m6() {}
    
    @Test
    public static void m7() {		//실패해야 한다.
        throw new RuntimeException("실패");
    }
    
    public static void m8() {}
    
}
~~~

Sample 클래스에는 정적 메소드가 7개가 있고, 그 중 4개에 @Test를 달았다.

- @Test가 달린 총 4개의 테스트중 1개는 성공, 2개는 실패, 1개는 잘못 사용했다.
- @Test가 붙지 않은 메소드는 테스트 도구가 무시할 것이다.


**해당 @Test의 사용 목적은**

- Sample 클래스의 의미에는 직접적인 영향을 주지 않고, @Test에 관심이 있는 도구에서 특별한 처리를 할 기회를 주는 것이다.

~~~java
public class RunTests {
    public static void main(String args[]) throws Exception {
        int tests = 0;
        int passed = 0;
        Class testClass = Class.forName("com.mesung.annotationTest." + args[0]);
        for(Method m : testClass.getDeclaredMethods()) {
            if(m.isAnnotationPresent(Test.class)) {
                tests++;
                try {
                    m.invoke(null);
                    passed++;
                } catch (InvocationTargetException wrappeedExc) {
                    Throwable exc = wrappeedExc.getCause();
                    System.out.println(m + "실패 : " + exc);
                } catch (Exception exc) {
                    System.out.println("잘못 사용한 @Test : " + m);
                }
            }
        }
        System.out.println("성공 : " + passed + ", 실패 : " + (tests - passed));
    }
}
~~~

이 테스트 러너는 **명령줄로부터 완전 정규화된 클래스 이름을 받아, 그 클래스에서 @Test 어노테이션이 달린 메소드를 차례로 호출한다.**

- testClass.getDeclaredMethod() : public + private 혹은 protected 메소드 호출
- m.isAnnotationPresent(Test.class) : Test 클래스의 메소드들을 확인하면서 어노테이션이 붙여진 것만 true로 반환


#
**테스트 메소드를 찾은 후 테스트 메소드가 예외를 던지게 되면 리플렉션 메커니즘이 InvocationTargetException으로 감싸서 다시 던지게 된다.**

- 그래서 **이 프로그램은 InvocationTargetException을 잡아 원래 예외에 담긴 실패 정보를 추출해(getCause()) 출력**하는 것이다.

만약, InvocationTargetException 외의 예외가 발생하면 @Test를 잘못 사용했다는 뜻이다.

- 인스턴스 메서드, 매개변수가 있는 메서드, 호출할 수 없는 메소드가 이에 해당할 것이다.
- *해당 소스에서는 Sample.m5()가 해당된다.*


#
**실행결과**

![image](https://user-images.githubusercontent.com/40616436/73935093-1f895b80-4923-11ea-804b-74ffb6437c45.png)



## 

### 특정 예외를 던져야 성공하는 테스트

**커스텀 어노테이션 설정**

~~~java
public class Sample2 {
    @ExceptionTest(ArithmeticException.class)
    public static void m1() {   //성공해야 한다.
        int i = 0;
        i = i/i;
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m2() {   //실패해야 한다. (다른 예외 발생)
        int [] a = new int[0];
        int i = a[1];
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m3() {   //실퍃해야 한다. (예외가 발생하지 않음)

    }
}
~~~


  
**Test 진행할 객체 설정**

~~~java
public class Sample2 {
    @ExceptionTest(ArithmeticException.class)
    public static void m1() {   //성공해야 한다.
        int i = 0;
        i = i/i;
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m2() {   //실패해야 한다. (다른 예외 발생)
        int [] a = new int[0];
        int i = a[1];
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m3() {   //실퍃해야 한다. (예외가 발생하지 않음)

    }
}
~~~



**Test Run**

~~~java
public class RunTests {
    public static void main(String args[]) throws Exception {
        int tests = 0;
        int passed = 0;
        Class testClass = Class.forName("com.mesung.annotationTest.Sample2");
        for(Method m : testClass.getDeclaredMethods()) {
            if(m.isAnnotationPresent(ExceptionTest.class)) {
                tests++;
                try {
                    m.invoke(null);
                    System.out.printf("테스트 %s 실패 : 예외를 던지지 않음 %n", m);
                } catch (InvocationTargetException wrappeedExc) {
                    Throwable exc = wrappeedExc.getCause();
                    Class<? extends Throwable> excType = m.getAnnotation(ExceptionTest.class).value();
                    if(excType.isInstance(exc)) {
                        passed++;
                    } else {
                        System.out.printf("테스트 %s 실패 : 기대한 예외 %s\n, 발생한 예외 %s\n", m, excType.getName(), exc);
                    }
                } catch (Exception exc) {
                    System.out.println("잘못 사용한 @ExceptionTest : " + m);
                }
            }
        }
        System.out.println("성공 : " + passed + ", 실패 : " + (tests - passed));
    }
}
~~~

@Test와 가장 큰 차이는 **어노테이션 매개변수의 값을 추출하여 테스트 메소드가 올바른 예외를 던지는 확인하는 것이다.**

테스트 프로그램이 문제없이 컴파일 되면 **어노테이션의 매개변수가 가리키는 예외가 정상적으로 나타난다는 뜻이다.**

단, 해당 **예외 클래스 파일이 컴파일 타임에는 존재했으나 런타임에는 존재하지 않을 수도** 있다. 이런 경우라면 **테스트 러너가 TypeNotPresentException을 던질 것이다.**


#
**실행결과**

![image](https://user-images.githubusercontent.com/40616436/73938060-d38de500-4929-11ea-9fce-def09e6bd096.png)



## 

### 예외를 여러 개 명시하고 그 중 하나가 발생하면 성공인 테스트

**커스텀 어노테이션 수정**

~~~java
public @interface ExceptionTest {
    Class<? extends Throwable>[] value();   //예외를 여러개 명시
}
~~~



**Test 진행할 객체 메소드 추가**

~~~java
public class Sample2 {
    ...

    @ExceptionTest(ArithmeticException.class)
    public static void m3() {   //실퍃해야 한다. (예외가 발생하지 않음)

    }

    //예외 두개중 하나만 걸려도 true
    @ExceptionTest({IndexOutOfBoundsException.class, NullPointerException.class})
    public static void doublyBad() {
        List<String> list = new ArrayList<>();
        list.addAll(5, null);
    }
}
~~~



**Test Run**

~~~java
for(Method m : testClass.getDeclaredMethods()) {
  if(m.isAnnotationPresent(ExceptionTest.class)) {
    tests++;
    try {
      m.invoke(null);
      System.out.printf("테스트 %s 실패 : 예외를 던지지 않음 %n", m);
    } catch (InvocationTargetException wrappeedExc) {
      Throwable exc = wrappeedExc.getCause();
      int oldPassed = passed;
      //Class<? extends Throwable> excType = m.getAnnotation(ExceptionTest.class).value();
      Class<? extends Throwable>[] excTypes = m.getAnnotation(ExceptionTest.class).value();
      for(Class<? extends Throwable> excType : excTypes) {
        if(excType.isInstance(exc)) {
          passed++;
          break;
        }
      }
      if(passed == oldPassed) {
        System.out.printf("테스트 %s 실패 : %s \n", m, exc);
      }
    } catch (Exception exc) {
      System.out.println("잘못 사용한 @ExceptionTest : " + m);
    }
  }
}
System.out.println("성공 : " + passed + ", 실패 : " + (tests - passed));
~~~



**실행 결과**

![image](https://user-images.githubusercontent.com/40616436/73938289-4c8d3c80-492a-11ea-8234-80cb62a07d7d.png)



성공이 하나 더 추가 되는 것을 볼 수 있다.



##

### 반복 가능 어노테이션을 사용(@Repeatable)

Java 8 에서는 여러 개의 값을 받는 어노테이션을 다른 방식으로도 만들 수 있다.

- 배열 매개변수를 사용하는 대신 어노테이션에 **@Repeatable의 메타 어노테이션을 다는 방식이다.**
- @Repeatable을 단 어노테이션은 하나의 프로그램 요소에 여러 번 달 수 있다.


#
단 주의할 점이 있다.

1. **@Repeatable을 단 어노테이션을 반환하는 '컨테이너 어노테이션'을 하나 더 정의하고, @Repeatable에 이 컨테이너 어노테이션의 class객체를 매개변수로 전달해야한다.**
2. **컨테이너 어노테이션은 내부 어노테이션 타입의 배열을 반환하는 value 메서드를 정의해야 한다.**
3. **컨테이너 어노테이션 타입에는 적절한 보존 정책(@Retention)과 적용 대상(@Target)을 명시해야 한다. 그렇지 않으면 컴파일이 되지 않는다.**


#
**커스텀 어노테이션 수정**

~~~java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ExceptionTestContainer.class)
public @interface ExceptionTest {
    Class<? extends Throwable> value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface ExceptionTestContainer {
    ExceptionTest[] value();
}
~~~



**Test 진행할 객체 메소드 수정**

```java
//@Repeatable 사용
@ExceptionTest(IndexOutOfBoundsException.class)
@ExceptionTest(NullPointerException.class)
public static void doublyBad() {
    List<String> list = new ArrayList<>();
    list.addAll(5, null);
}
```



**Test Run**

~~~java
if(m.isAnnotationPresent(ExceptionTest.class) || m.isAnnotationPresent(ExceptionTestContainer.class)) {
  tests++;
  try {
    m.invoke(null);
    System.out.printf("테스트 %s 실패 : 예외를 던지지 않음 %n", m);
  } catch (InvocationTargetException wrappeedExc) {
    Throwable exc = wrappeedExc.getCause();
    int oldPassed = passed;
    //Class<? extends Throwable> excType = m.getAnnotation(ExceptionTest.class).value();
    //Class<? extends Throwable>[] excTypes = m.getAnnotation(ExceptionTest.class).value();
    ExceptionTest[] excTests = m.getAnnotationsByType(ExceptionTest.class);
    for(ExceptionTest excTest : excTests) {
      if(excTest.value().isInstance(exc)) {
        passed++;
        break;
      }
    }
    if(passed == oldPassed) {
      System.out.printf("테스트 %s 실패 : %s \n", m, exc);
    }
  } catch (Exception exc) {
    System.out.println("잘못 사용한 @ExceptionTest : " + m);
  }
}
~~~

반복 가능 어노테이션은 처리할 때도 주의를 요하는데, **해당 어노테이션을 여러개 달면 하나만 달았을 때와 구분하기 위해 '컨테이너' 어노테이션 타입(@ExceptionTestContainer)이 적용**된다.

**getAnnotationsByType()는 @ExceptionTest 둘을 구분하지 않아,반복 가능 어노테이션과 그 컨테이너 어노테이션을 모두 가져오지만, isAnnotationPresent()는 둘을 명확히 구분한다.**

- 그로 인해, 반복 가능 어노테이션을 여러개 달고, isAnnotationPresent로 반복가능 어노테이션이 달렸는지 확인한다면, '그렇지 않다'라고 알려줄 것이다.(컨테이너가 달렸기 때문에 반복 가능 어노테이션이 아니라고 판단)
- 결과적으로 isAnnotationPresent()으로 확인을 진행하면 어노테이션을 여러 번 단 메소드들을 모두 무시하고 지나치게 될 것이다.
- 같은 이유로, isAnnotationPresent()로 컨테이너 어노테이션이 달렸는지 확인한다면 반복 가능 어노테이션의 존재로 인해 무시하고 지나치게 될 것이다.

**이런 이유로, 달려 있는 수와 상관없이 모두 검사를 진행하려면 getAnnotaionByType()으로 모두 가져와서 따로따로 검사를 진행해야 한다.**


#
**실행 결과**

![image](https://user-images.githubusercontent.com/40616436/73938289-4c8d3c80-492a-11ea-8234-80cb62a07d7d.png)



반복 가능 어노테이션을 사용해 하나의 프로그램 요소에 같은 어노테이션을 여러 번 달 때의 코드 가독성을 높여 보였다.

- @ExceptionTest() 반복 사용

하지만 어노테이션을 선언하고 이를 처리하는 부분에서는 코드 양이 늘어나며, 특히 처리 코드가 복잡해져 **오류가 날 가능성이 커진다는 것은 명심하고 넘어가야 한다.**



## 

**정리**

**어노테이션으로 할 수 있는 일을 명명 패턴으로 처리할 이유는 전혀 없다**

사실 일반 프로그래머가 어노테이션 타입을 직접 정의할 일은 거의 없다. 하지만, **자바 프로그래머라면 예외 없이 자바가 제공하는 어노테이션 타입들은 사용해야한다.**




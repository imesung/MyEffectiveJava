## 아이템04_인스턴스화를 막으려거든 private 생성자를 사용하라

어떤 시스템을 개발하다면 보면 정적 메서드와 정적 필드만을 담는 클래스를 만들어야 하거나 만들고 싶을 때가 있다.

예를 들면, java.lang.Math와 java.util.Arrays 클래스처럼 기본 타입 값이나 배열에 관련된 메서드들만 모아 둘수도 있다.

아래 코드는 Math 클래스의 일부분이다.

~~~java
public final class Math {
  private Math() {}
  public static final double E = 2.7182818284590452354;
  public static final double PI = 3.14159265358979323846;
  private static final double DEGREES_TO_RADIANS = 0.017453292519943295;
  private static final double RADIANS_TO_DEGREES = 57.29577951308232;

  @HotSpotIntrinsicCandidate
  public static double sin(double a) {
    return StrictMath.sin(a); // default impl. delegates to StrictMath
  }

  @HotSpotIntrinsicCandidate
  public static double cos(double a) {
    return StrictMath.cos(a); // default impl. delegates to StrictMath
  }
  ...
}
~~~

Math 클래스의 경우 final 클래스이기 때문에 하위 클래스에서 상속도 불가능하다. 또한, 생성자가 private이므로 컴파일러가 자동으로 기본 생성자를 만들지도 않는다. 즉, **해당 클래스를 인스턴스로 만드려고 설계한 것이 아니라는 것이다.**

그러므로 우리는 해당 클래스 같은 것들은 인스턴스화 되는 것을 막아야한다.

---

### 인스턴스화를 막는 방법

Math 같은 클래스를 추상 클래스로 만들면 인스턴스화를 막을 수 있지 않을까 라는 생각을 할 수도 있지만, 하위 클래스에서 추상 클래스를 상속해서 인스턴스화 하면 그만이다. 그럼 어떤 식으로 막아야하는가?!

인스턴스 생성을 막는 방법은 간단하다. private으로 선언된 기본 생성자를 만들고 내부에 예외를 던져버리면 되는 것이다.

~~~java
public class MyUtils {
  private MyUtils() throws Exception {
    throw new Exception("인스턴스화 불가");
  }

  public static int test() {
    return 0;
  }
}
~~~

만약 MyUtils 클래스를 상속 받아서 하위 클래스에서 생성하려고 할 때 에러가 발생할 것이다.

하위 클래스에서는 상위 클래스의 생성자를 반드시 호출하는데, 이를 private으로 선언함과 동시에 예외까지 던지고 있으므로 접근이 불가능하다.(리플렉션으로 접근 시)


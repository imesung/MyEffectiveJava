## public 클래스에서는 public 필드가 아닌 다른 접근자 메서드를 사용하라

**개발을 하다보면 인스턴스 필드들만 모아놓는 목적만 가진 아래와 같은 클래스를 구성하는 경우가 있다.**

~~~java
public class Point{
  public double x;
  public double y;
}
~~~

이유는, 이러한 클래스 형태로 구현했을 때는 클라이언트에서 필드에 직접 접근할 수 있으므로 캡슐화의 이점을 제공하지 못한다는 단점이 있다. 또한 좀 더 알아보면,

- API를 수정하지 않고는 내부 표현을 바꿀 수 없다.
  - public 필드로만 구성되어 있기 때문에 내부 표현을 변경하기 위해서는 API의 필드를 변경해야 한다. (*메소드가 존재할 땐 파라미터에 따라 내부 표현이 변경 가능*)
- 불변식을 보장할 수 없다.
  - 클라이언트에서 직접적으로 필드에 접근하고 있으므로 클라이언트에 의해 언제든지 변경이 가능하다.
- 외부에서 필드에 접근할 때 부수적인 로직을 추가할 수 없다.
  - Point.x 라는 필드를 조회했을 때 부수적인 로직(Ex. 연산 로직)을 추가할 수가 없다.



**객체지향 프로그래머의 데이터 캡슐화**

그래서 public 클래스라면 아래 처럼 구현하는 것이 옳다.

~~~java
class Point {
  private double x;
  private double y;

  public Point (double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() { return x; }
  public double getY() { return y; }

  public void setX(double x) { this.x = x; }
  public void setY(double y) { this.y = y; }
}
~~~

이 처럼 구현하게 되면,

- 클래스의 내부 표현 방식을 언제든 바꿀 수 있는 유연성을 제공하게 된다.
  - getter/setter 혹은 또 다른 메소드를 통해 로직을 언제든 추가할 수 있다.

이 전 소스 처럼 public 클래스가 필드값을 공개(pubilc 접근자)하게 되면 이를 사용하는 클라이언트가 생겨날 것이므로 내부 표현방식을 마음대로 바꿀 수 없게 된다.

**하지만, package-private 클래스 혹은 private 중첩 클래스라면 데이터 필드를 노출한다고 해도 문제될 것이 없다.**

> Package-private : 같은 패키지 및 같은 클래스에서만 접근 가능
>
> private 중첩 클래스 : 내부 클래스가 private 형태로 된 클래스

그 이유는 ***같은 패키지 안에서 어떤 특정 이유 때문에 사용하던가 탑 레벨 클래스에서만 접근하기 때문에 위에서 언급한 단점들이 나타날 이유가 없어 문제될 것이 없다.***



**정리**

public 클래스의 경우 가변 필드의 접근 제한자를 public으로 두면 안 되며, 불변 필드라고 해도 덜 위험하지만 안심할 수 없다. 만약 가변 필드를 노출하고 싶을 땐, package-private 클래스나 private 중첩 클래스를 활용해라.
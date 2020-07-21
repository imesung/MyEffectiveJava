## 아이템03_private 생성자나 열거 타입을 싱글턴임을 보증하라

싱글턴이란 모두가 알고 있듯이, 오직 하나의 인스턴스만을 생성할 수 있는 클래스를 말한다.

그런데 클래스를 싱글턴으로 만들면 이를 사용하는 클라이언트를 테스트하기가 어려워질 수 있다. 그 이유는 타입을 인터페이스로 정의한 다음 그 인터페이스를 구현해서 만든 싱글턴이 아니라면 싱글턴 인스턴스를 mock 구현으로 대체할 수 없기 때문이다.(이유는? https://bottom-to-top.tistory.com/30 참고)

**그럼 이제 싱글턴을 만드는 방식을 살펴보자**

---

### public static 멤버가 final 필드인 방식

~~~java
public class Elvis {
  public static final Elvis INSTANCE = new Elvis();

  private Elvis() { }

  public void leaveTheBuilding() {
    System.out.println("Whoa baby, I'm outta here!");
  }

  // 이 메서드는 보통 클래스 바깥(다른 클래스)에 작성해야 한다!
  public static void main(String[] args) {
    Elvis elvis = Elvis.INSTANCE;
    elvis.leaveTheBuilding();
  }
}
~~~

소스에서 보는 바와 같이 private 생성자는 public static final 필드인 Elvis.INSTANCE를 초기화할 때 딱 한 번 호출된다. public이나 protected 생성자가 없으므로 Elvis 클래스가 초기화될 때 만들어진 인스턴스가 단 하나뿐임을 보장하는 것이다. 하지만, 여기서 예외가 있는 경우가 있다. ***권한이 있는 클라이언트는 리플렉션 API인 AccessibleObject.setAccessible을 사용하여 private 생성자를 호출할 수 있다.***

~~~java
//리플렉션 사용 예시
public static void main( String[] args ) throws ClassNotFoundException {
  Elvis elvis1 = Elvis.INSTANCE;
  Elvis elvis2 = Elvis.INSTANCE;
  System.out.println(elvis1 == elvis2);

  Class<Elvis> elvisClass = Elvis.class;
  Arrays.stream(elvisClass.getDeclaredConstructors()).forEach(f -> {
    try {
      //모든 접근제한자 접근
      f.setAccessible(true);
			
      //private 생성자에 접근하여 호출하고 있다.
      Elvis reflectionElvis = (Elvis) f.newInstance();
      System.out.println(elvis1 == reflectionElvis);
    } catch (Exception e) {
      e.printStackTrace();
    }
  });
}
~~~

---

### 정적 팩터리 메서드를 public static 멤버로 제공하는 방식

~~~java
public class Elvis {
  private static NotRefElvis INSTANCE;
  private NotRefElvis() throws Exception {
    //리플렉션 방지
    if(true) {
      throw new Exception();
    }
  }

	//정적 팩터리 메서드를 활용한 싱글턴
  public static NotRefElvis getInstance() {
    if(INSTANCE == null) {
      try {
        System.out.println("첫번재 생성자 생성");
        INSTANCE = new NotRefElvis();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return INSTANCE;
  }
}

~~~

위 소스는 정적 팩터리 메서드를 활용하여 싱글턴을 만드는 방식이며, 리플렉션을 통해 private 생성자에 접근하는 것을 막기 위해 두번째 객체가 생성하려 할 때 예외를 던지는 모습을 볼 수 있다.

---

### Thread-safe 싱글톤

~~~java
public class ThreadSafeElvis {
  private static ThreadSafeElvis INSTANCE;
  private ThreadSafeElvis() throws Exception {
    if(true) {
      throw new Exception();
    }
  }

  public static ThreadSafeElvis getInstance() throws Exception {
    if(INSTANCE == null) {
      synchronized (ThreadSafeElvis.class) {
        if(INSTANCE == null) {
          INSTANCE = new ThreadSafeElvis();
        }
      }
    }
    return INSTANCE;
  }
  
  private Object readResolve() {
    // 싱글턴을 보장하기 위함!
    return INSTANCE;
	}
}
~~~

멀티 스레드 환경에서 동시에 getInstance 메소드를 접근하게 되면 싱글턴의 목적이 달라지기 때문에 synchronized(동기화) 처리를 하여 하나의 인스턴스만 만들 수 있도록 한다.

**하지만** 여기서 중요하게 생각해야하는 것이 있는데, 싱글턴 생성을 할때 직렬화를 한다고 해서 synchronized만 붙이고 끝내게되면, 추 후 역직렬화할 때 새로운 객체가 생기게 되므로 역직렬화할 때 동일한 객체가 반환될 수 있도록 readResolve 메소드를 추가해야 한다.

~~~java
private Object readResolve() {
  // 싱글턴을 보장하기 위함!
  return INSTANCE;
}
~~~

https://madplay.github.io/post/what-is-readresolve-method-and-writereplace-method

---

### Holder(내부 클래스)에 의한 싱글턴 초기화

~~~java
public class HolderElvis {
  private HolderElvis() throws Exception {
    if(true) {
      throw new Exception();
    }
  }

  private static class LazyHolder {
    public static HolderElvis INSTANCE;

    static {
      try {
        INSTANCE = new HolderElvis();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static HolderElvis getInstance() {
    return LazyHolder.INSTANCE;
  }
}
~~~

synchronized를 개발자가 직접 구현하려 한다면 프로그램 구조가 복잡해지고 정확하지 않을 수 있다는 이슈가 발생할 수도 있다. 이런 점을 방지하기 위해 위 같은 소스를 구현하는 것이다.

위 소스는 클래스 안에 정적 클래스(LazyHolder)를 두어 클래스가 로드되는 시점에 한번만 호출될 것이다. 또한, 리플렉션으로 생성자를 호출하는 방식도 막아놓은 것을 확인할 수 있다.

이 방법은 JVM이 클래스 초기화 과정에서 클래스가 로드되는 순간 한번만 호출하는 것으로 싱글턴의 초기화 되는 문제에 대한 책임을 JVM이 가지고 있게 된다. 더 이상 개발자가 멀티 스레드에 대해서 개연할 필요가 없어지게 되는 것이다.

---

### Enum을 활용한 싱글톤

~~~java
public enum Elvis {
  INSTANCE;

  private Elvis() {
    System.out.println("생성 완료");
  }
  
  public static void main(String[] args) {
    Elvis elvis = Elvis.INSTANCE;
    elvis.leaveTheBuilding();
  }
}
~~~

Enum을 사용하게 되면 매우 간결하고 직렬화할 수도 있으며, 리플렉션 공격에서도 막을 수 있다는 장점을 가지고 있다. 대부분 상황에서는 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법이다.




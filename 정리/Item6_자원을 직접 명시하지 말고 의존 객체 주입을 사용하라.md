## 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

- 많은 클래스가 하나 이상의 자원을 의존한다.

- 맞춤법 검사기(SpellChecker)가 사전(Dicitonary)에 의존하는 예를 확인해보자

  ```java
  //정적 유틸리티 활용
  public class SpellChecker {
      private static final Lexicon dictionary = ...;	//정적 유틸리티
      
      private SpellChecker() {}	//객체 생성을 방지
      
      public static boolean isValid(String word) {...}
      public static List<String> suggestions(String typo) {...}
  }
  
  //싱글턴 활용
  public class SpellChecker {
      private final Lexicon dictionary = ...;
      
      private SpellChecker() {}	//객체 생성을 방지
      public static SpellChecker INSTANCE = new SpellChecker(...);
      
      public static boolean isValid(String word) {...}
      public static List<String> suggestions(String typo) {...}
  }
  ```

- 두 방식(정적 유틸리티 클래스, 싱글턴) 모두 하나의 자원(하나의 사전)만 사용한다는 점에서 볼 때 그리 훌륭한 설계는 아니다.

  - why? 맞춤법 검사를 할 때 하나의 사전만을 보지는 않으니깐!

- **즉, 사용하는 자원에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글턴 방식은 적합하지 않다**

---

- 클래스(SpellChecker)가 여러 자원 인스턴스를 지원해야 하며, 클라이언트가 원하는 자원(Dictionary)을 사용해야한다.

- **결과적으로, 인스턴스를 생성할 때, 생성자에 필요한 자원을 넘겨주는 방식을 사용해야한다.**

  - 이는, 의존 객체 주입의 한 형태로 맞춤법 검사기를 생성할 때 의존 객체인 사전을 주입해주면 되는 것이다.

  ```java
  public class SpellChecker {
      private final Lexicon dictionary = ...;
      
      public SpellChecker(Lexion dictionary) {
          this.dictionary = Objects.requireNonNull(dictionary);
      }
      
      public static boolean isValid(String word) {...}
      public static List<String> suggestions(String typo) {...}
  }
  ```

- 일반적으로 사용한 생성자를 통해 자원을 받아오는 방식을 사용한 것이다.

---

- 그렇다면 이 패턴의 쓸만한 변형은 또 무엇이 있을까?

- 생성자에 자원 팩토리를 넘겨주는 방식이 있을 것이다. 즉, **팩토리 메서드 패턴**을 말하는 것이다.

- Java8에서는 `Supplier<T>`가 팩토리를 표현한 완벽한 예이다.

  ```java
  //클라이언트가 제공한 팩토리(Tile)로 구성된 Mosaic를 만드는 메서드
  Mosaic create(Supplier<? extends Tile> tileFactory) {...}
  ```

---

- 핵심 정리
  - 클래스가 내부적으로 하나 이상의 자원에 의존하고, 그 자원이 클래스 동작에 영향을 준다면 싱글턴과 정적 유틸리티 클래스에는 사용하지 않는 것이 좋다.
  - 필요로 하는 자원(혹은 그 자원을 만들어주는 팩토리)을 생성자에게 넘겨주자
  - **의존 객체 주입**은 클래스의 유연성, 재사용성, 테스트 용이성을 개선해준다.
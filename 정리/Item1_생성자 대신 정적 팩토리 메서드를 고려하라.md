## 생성자 대신 정적 팩토리 메서드를 고려하라

- 클래스 생성자와 별도로 정적 팩토리 메서드를 제공할 수 있다.

  - 

  ```java
  public static Boolean vlueOf(boolean b) {
      return b ? Boolean.TRUE : Boolean.FALSE;
  }
  ```

- 정적 팩토리 메서드가 생성자보다 좋은 장점 다섯가지

  1. **이름을 가질 수 있다.**
  	- 생성자는 반환 시 객체의 특성을 알기 어렵지만, 정적 팩터리는 이름만 잘 지으면 반환될 객체의 특성을 쉽게 묘사할 수 있다.
  	- 생성자인 BigInteger(int, int, Random)과 정적 팩토리 메서드인 BigInteger.probablePrime 중 어느 쪽이 '값이 소수인 BigInteger를 반환한다'의 의미를 더 잘 설명하는지는 확실히 확인할 수 있을 것이다.
  	- 또한, 생성자는 하나의 시그니처로 하나만 만들 수 있으나, 정적 팩토리 메서드는 하나의 시그니처로 여러 생성자를 만들 수 있다.
  	  - 매개변수 타입과 개수는 같으나, 메서드 이름을 다르게 주어 각각의 차이를 드러나게 한다.
  2. **호출될 때마다 인스턴스를 새로 생성하지 않아도 된다.**
     - 같은 객체가 자주 요청되는 상황이라면 성능을 상당히 끌어올려준다.
       - Ex. 플라이웨이트 패턴도 비슷한 기법
         - 여러 객체들이 참조하는 공통의 객체가 있을 시 공통의 객체를 공유하는 기법
  3. **반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다.**
     - API를 만들 대 이 유연성을 응용하면 구현 클래스를 공개하지 않고도 그 객체를 반환할 수 있어 API를 작게 유지할 수 있다.
     - 인터페이스 기반 프레임워크를 만드는 핵심 기술이기도 하다
       - 인터페이스를 정적 팩토리 메서드의 반환타입으로 사용 가능하다.
  4. **입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.**
     - 반환 타입의 하위 타입이기만 하면 어떤 클래스의 객체를 반환하든 상관없다.
  5. **정적 팩토리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.**
     - JDBC의 getConnection()에서 나오는 Connection 객체는 DB마다 다를 수 있다.

- 단점을 알아보자

  1.  **상속을 하려면 public이나 protected 생성자가 필요하니 정적 팩토리 메서드만 제공하면 하위 클래스를 만들 수 없다.**
  2. **정적 팩토리 메서드는 프로그래머가 찾기 어렵다.**
     - 생성자처럼 API 설명에 명확히 드러나지 않으니 사용자는 정적 팩토리 메서드 방식 클래스를 인스턴스화할 방법을 알아내야 한다.

- 정적 팩토리 메서드에서 흔히 사용하는 명명 방식

- ```java
  //from: 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환하는 형변환 메서드
  Date d = Date.from(instant);
  
  //of: 여러 매개변수를 받아 적절한 타입의 인스턴스 반환하는 집계 메서드
  Set<Rank> faceCards = EnumSet.of(JACK, QUEEN, KING);
  
  //valueOf: from과 of의 더 자세한 버전
  BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE);
  
  //instance, getInstance: 매개변수로 명시한 인스턴스 반환. 같은 인스턴스임을 보장하지는 않음
  StackWalker luke = StackWalker.getInstance(options);
  
  //create, newInstance: instance나 getInstance와 같지만, 매번 새로운 인스턴스를 생성하는 것을 보장
  Object newArray = Array.newInstance(classObject, arrayLen);
  
  //getType: getInstannce와 같으나, 다른 클래스에 팩토리 메서드를 정의할 때 사용.
  FileStore fs = Files.getFileStore(path);
  
  //newType: newInstance와 같으나, 다른 클래스에 팩토리 메서드를 정의할 때 사용.
  BufferedReader br = Files.newBufferedReader(path);
  
  //type: getType와 newType의 간결한 버전
  List<Complaint> litany = Collections.list(legacyLitany);
  ```

- 핵심 정리

  - 정적 팩토리를 생성자 대신 사용하는 것이 유리한 경우가 많음.
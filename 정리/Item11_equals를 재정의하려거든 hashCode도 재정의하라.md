## equals를 재정의하려거든 hashCode도 재정의하라

**equals를 재정의한 클래스 모두에서는 hashCode도 재정의해야한다.**

만약, hashCode를 재정의하지 않을 시, **HashMap이나 HashSet 같은 컬렉션의 원소로 사용할 때 문제를 일으킬 것이다.**



**Object 명세에서 발췌한 규약을 살펴보자**

- Equals 비교에 사용되는 정보가 변경되지 않았다면, **hashCode 메소드는 몇번을 호출해도 항상 같은 값을 반환해야 한다.** 단, 애플리케이션이 다시 시작할 시에는 이 값이 달라져도 상관 없다.
- **equals(Object)가 두 객체를 같다고 판단했다면, 두 객체의 hashCode는 똑같은 값을 반환해야 한다.**
- **equals(Object)가 두 객체르 다르다고 판단했더라도, 두 객체의 hashCode가 서로 다른 값을 반환할 필요는 없다.** 단, 다른 값에 대해서는 hashCode 역시 다른 값을 반환해야 **해시 테이블** 성능이 좋아진다.



**hashCode 재정의를 잘못했을 때 크게 문제가 되는 조항은 두 번째이다.** 즉, 논리적으로 같은 객체는 같은 해시코드를 반환해야 한다.

equals는 물리적으로 다른 두 객체를 논리적으로는 같다고 할 수 있다.(참조하고 있는 값이 같을 시) 하지만 Object의 기본 hashCode()에서는 이 둘이 전혀 다르다고 판단한다.

**Ex. 아이템 10의 PhoneNumber 클래스의 인스턴스를 HashMap의 원소로 사용한다고 해보자.**

~~~java
Map<PhoneNumber, String> m = new HashMap<>();
m.put(new PhoneNumber(707, 867, 5309), "제니");
~~~

- 이 코드 다음에

  ~~~java
  m.get(new PhoneNumber(707, 867, 5309))
  ~~~

- 해당 코드를 실행하면 **제니**가 나와야할 거 같지만, 실질적으로 **null** 이 반환된다.

- **Why?**

  - 여기에는 두 개의 PhoneNumber 클래스가 사용 되었다. **첫번째는 "제니"를 넣을 때(논리적 동치), 두번째는 이를 꺼내려고 할 때 사용되었다.**

  - PhoneNumber 클래스는 hashCode를 재정의하지 않았기 때문에 논리적 동치인 두 객체가 서로 다른 해시코드를 반환하여 **두 번째 규약을 지키지 못한 것이다.**

    - *new를 통해서 서로 다른 객체를 key로 사용하여 hashCode를 뽑아내어 두 객체는 서로 다른 해시코드가 반환된다.*
    - 즉, get 메소드는 엉뚱한 해시 버킷을 찾아가서 객체를 찾으려고 했던 것이다. **하지만, 두 객체가 동일한 버킷에 존재했더라도 결과값은 null이었을 것이다.**
    - 그 이유는, **HashMap은 해시코드가 다른 엔트리끼리는 동치성 비교를 시도조차 하지 않기 때문이다.**

  - 이 문제는 PhoneNumber에 hashCode()만 작성해주면 된다.

    ~~~java
    @Overrdie
    public int hashcode() {
      return 42;
    }
    ~~~

    - 하지만, **절대 사용해서는 안된다.** 왜? 해시코드를 고정값으로 지정하면 같은 해시 버킷에 축적되어 마치 링크드 리스트 처럼 동작하게 될 것이기 때문이다.
    - 결과적으로 평균 O(1)의 속도가 O(n)으로 느려지게 될 것이다.



**좋은 해시 함수라면 서로 다른 인스턴스에 다른 해시코드를 반환한다. (hashCode의 세번째 규약)**

- 이상적인 해시 함수는 서로 다른 인스턴스들을 32비트 정수 범위에 균일하게 분배해야한다.

**hashCode의 작성 요령을 살펴보자**

1. int 변수인 result를 선언한 후 값을 c로 초기화한다.
   - 이 때, c는 해당 객체의 첫번째 핵심 필드를 단계 2.1 방식으로 계산한 해시코드이다.
   - 여기서 핵심 필드는 아이템 10에서 equals 비교에 사용되는 필드를 말한다.
2. 해당 객체의 나머지 핵심 필드인 f 각각에 대해 다음 작업을 수행한다.
   1. 해당 필드의 해시코드 c 를 계산한다.
      - 기본 타입 필드라면, Type.hashCode(f)를 수행한다. 여기서 Type은 해당 기본타입의 박싱 클래스다.
      - 참조 타입 필드면서, 이 클래스의 equals 메소드가 이 필드의 equals를 재귀적으로 호출하여 비교한다면, 이 필드의 hashCode를 재귀적으로 호출한다.
      - 필드가 배열이라면, 핵심 원소 각각을 별도 필드처럼 다룬다.
   2. 단계 2.1에서 계산한 해시코드 c로 result를 갱신한다.
      - result = 31(소수) * result + c;
      - 31이라는 숫자대신 짝수가 오게되면 해시 충돌이 자주 일어나게 된다.
      - 31이라는 숫자를 사용하면 시프트 연산 및 뺄셈으로 대체해 최적화 할 수 있다.
        - 31 * i 는 (i << 5) - i와 같다.
3. result를 반환한다.

**hashCode를 다 구현했다면, 이 메소드가 동치인 인스턴스에 대해 똑같은 해시코드를 반환하는 지 확인해보자**

- 검증할 단위 테스트를 작성하자
- 파생 필드는 해시코드 계산에서 제외해도 된다.
- equals 비교에 사용되지 않은 필드는 반드시 제외해야 한다. 그렇지 않으면 hashCode 규약의 두번째를 어기게 될 위험이 있다.



**이제 해당 hashCode 구현 요령을 사용하여 PhoneNumber 클래스를 수정해보자**

~~~java
//PhoneNumber 클래스
@Override
public int hashCode() {
  int result = Short.hashCode(areaCode);
  result = 31 * result + Short.hashCode(prefix);
  result = 31 * result + Short.hashCode(lineNum);
  return result;
}
~~~

- 이 메소드는 PhoneNumber 인스턴스의 핵심 필드 3개(areaCode, prefix, lineNum)만을 사용해 간단한 계산만 수행한 것이다.



**해시 함수를 사용하여 해시코드값을 뽑아내는 PhoneNumber 클래스**

~~~java
//PhoneNumber 클래스
@Override
public int hashCode() {
  return Objects.hash(lineNum, prefix, areaCode);
}
~~~

- 한줄이라는 장점을 보여주고 있지만, 아쉽게도 속도가 느리다.

- 이유는, 입력 인수를 담기위한 배열이 만들어지며, 입력 중 기본 타입이 있다면 박싱과 언박싱도 거쳐야 한다.

  ~~~java
  //Objects.hash()
  public static int hash(Object... values) {
    return Arrays.hashCode(values);
  }
  
  public static int hashCode(Object[] a) {
    if (a == null) {
      return 0;
    } else {
      int result = 1;
      Object[] var2 = a;
      int var3 = a.length;
  
      for(int var4 = 0; var4 < var3; ++var4) {
        Object element = var2[var4];
        result = 31 * result + (element == null ? 0 : element.hashCode());
      }
  
      return result;
    }
  }
  ~~~



**클래스가 불변이고 해시코드를 계산하는 비용이 크다면, 매번 새로 계산하기 보다는 캐싱하는 방식을 고려해라.**

- 이 타입의 객체가 주로 해시의 키로 사용될 것 같다면 인스턴가 만들어질 때 해시코드를 계산해둬야 한다.



**해시의 키로 사용되지 않는 경우라면 hashCode가 처음 불릴 때 계산하는 지연 초기화 전략을 사용해보자**

- 필드를 지연초기화 하려면 그 클래스를 스레드에 안전하게 만들도록 신경써야 한다.

- PhoneNumber 클래스를 수정해보자

  ~~~java
  private int hashCode;	//자동으로 0으로 초기화
  
  @Override
  public int hashCode() {
    int result = hashCode;
    if(result == 0) {	//스레드 안정성 고려
      result = Short.hashCode(areaCode);
      result = 31 * result + Short.hashCode(prefix);
    	result = 31 * result + Short.hashCode(lineNum);
      hashCode = result;
    }
    return result;
  }
  ~~~



**주의**

**성능을 높인답시고 해시코드를 계산할 때 핵심 필드를 생략해서는 안된다.**

- 속도는 빨라지나, 해시 품질이 나빠져 해시 테이블 성능을 심각하게 떨어뜨릴 수 있다.
- 만약 빼먹은 필드 중 어떤 필드는 특정 영역에 몰리는 인스턴스들의 해시코드를 넓은 범위로 퍼트려주는 효과가 있을 수도 있다.

**hashCode가 반환하는 값의 생성 규칙을 API 사용자에게 자세히 공표하지 말자**

- 그래야 클라이언트가 이 값에 의지하지 않게 되고, 추후에 계산 방식을 바꿀 수 있다.



**핵심 정리**

- equals를 재정의할 때는 hashCode도 반드시 재정의해야 한다.
  - 그렇지 않으면 프로그램이 정상 동작 하지 않을 것이다.
- 재정의한 hashCode는 Object 문서에 기술된 일반 규약을 따라야 하며, **서로 다른 인스턴스라면 되도록 hashCode도 서로 다르게 구현해야 한다.**



**간략 정리**

1. equals만 재정의한 Test 클래스가 있다.

2. Test 클래스는 HashMap의 key값을 사용되고, put과 get을 실행한다.

   ~~~java
   HashMap<Test, String> hs = new HashMap<>();
   hs.put(new Test(code, num1, num2), "string");
   hs.get(new Test(code, num1, num2));
   ~~~

3. key값이 서로 다른 객체로 put과 get을 진행하므로 hashCode 값은 서로 다르게 되어진다.

   - hashCode를 재정의 하지 않았으므로!

4. 동일한 hashCode를 선별하기 위해 Test 클래스의 핵심 필드(code, num1, num2)를 활용하여 hashCode를 만들 수 있게 재정의한다.

   ~~~java
   @Overrdie
   public int hashCode() {
     result = Short.hashCode(code);
     result = 31 * result + Short.hashCode(num1);
     result = 31 * result + Short.hashCode(num2);
   }
   ~~~

   - 31(버킷 사이즈)이라는 숫자는 소수값으로 임의로 정한 것이다.




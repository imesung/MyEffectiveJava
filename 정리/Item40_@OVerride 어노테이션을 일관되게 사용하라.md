## @Override 어노테이션을 일관되게 사용하라

해당 어노테이션을 일관되게 사용하면 여러가지 악명 높은 버그들을 예방해준다.

다음 Bigram 프로그램을 살펴보자. Bigram은 영어 알파벳 2개로 구성된 문자열을 표현하는 소스다.

~~~java
pubcli class Bigram {
  private final char first;
  private final char second;
  
  public Bigram(char firtst, char second) {
    this.first = first;
    this.second = second;
  }
  public boolean equals(Bigram b) {
    return b.first == first && b.second = second;
  }
  public int hashCode() {
    return 31 * first + second;
  }
  public static void main(String [] args) {
    Set<Bigram> s = new HashSet<>();
    for(int i = 0; i < 10; i++) {
      for(char ch = 'a'; ch <= 'z'; ch++) {
      	s.add(new Bigram(ch, ch));  
      }
      System.out.println(s.size());
    }
  }
}
~~~

main 메소드에서 보듯이 똑같은 소문자 2개로 구성된 Bigram 26개를 10번 반복 후 집합에 추가하여, 그 집합의 크기를 출력한다.

 Set은 중복이 허용되지 않으므로, 26이 출력될 거 같지만 실제로는 **260**이 출력된다...?!



**확실히 Bigram 작성자는 equals 메소드를 재정의 한 것으로 보이고, hashCode도 함께 재정의했다.**(아이템10, 11)

그런데 자세히 살펴보면, **equals를 overriding한 게 아니라 overloading을 해버린 것이다.** 

Object의 equals를 재정의하려면 매개변수 타입을 Object로 해야만 하는데 그렇게 하지 않았다. 그로인해, Object에서 상속한 equals와는 별개로 새로운 equals 메소드를 정의한 꼴이 되버린 것이다.

Object의 equals는 **== 연산자와 똑같이 객체 식별성만을 확인한다.** 따라서 같은 소문자를 소유한 Bigram 10개 각각이 서로 다른 객체로 인식되고 결과적으로 260이라는 결과가 출력된 것이다.



그럼 equals()에 @Override를 달고 다시 컴파일 해보자

~~~java
@Override 
public boolean equals(Bigram b) {
  ...
}
//결과 : method does not override or implement a method from a supertype.

//수정해보자
@Override 
public boolean equals(Object o) {
  if(!(o instanceof Bigram)) {
    return false;
  }
  Bigram b = (Bigram) o;
  return b.first == first && b.second == second;
}
~~~

**그러니 상위 클래스의 메서드를 재정의하려는 모든 메서드에는 @Override를 달자**



**@Override를 안달아도 되는 예외 케이스**

@Override를 안달아도 되는 예외 케이스가 있다.

**구현체 클래스에서 상위 클래스의 추상 메소드를 재정의할 때는 굳이 @Override를 달지 않아도 된다.**

- 이유는, 구현체 클래스에서 추상 클래스의 추상 메소드를 구현하지 않으면 컴파일러가 그 사실을 바로 알려주기 때문이다.



**핵심 정리**

재정의한 모든 메소드에 @Override를 의식적으로 달면 우리가 실수했을 때 컴파일러에 의해 알 수 있다.

예외 케이스는 구현체 클래스에서 상위 클래스의 추상 메소드를 구현할 시 @Override를 안 달아도 되나 다는 것도 해로울 것은 없다.

**즉, 재정의를 할 시에는 @Override를 다는 것이 가독성도 좋고 안전성도 높다.**


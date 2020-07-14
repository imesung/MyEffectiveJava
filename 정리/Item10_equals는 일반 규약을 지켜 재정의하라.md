## equals는 일반 규약을 지켜 재정의하라

Equals 메소드는 재정의하기 쉬워 보이지만 곳곳에 함정이 도사리고 있어 자칫하면 끔찍한 결과를 초래할 수 있다.

이런 이유로 인해 아예 재정의 안하는 것이 옳은 선택일 수 있다. 



#### **다음 상황에 맞닥드릴 시에는 재정의를 하지 않는 것이 좋다.**

- **각 인스턴스가 본질적으로 고유하다.** 

  - 값을 표현하는 게 아니라 동작하는 개체를 표현하는 클래스를 말한다.
  - Thread에서 Object의 equals 메소드는 이러한 클래스에 딱 맞게 구현되어있다.

- **인스턴스의 논리적 동치성을 검사할 일이 없다.**

  - 인스턴스의 동치성을 검사할 일이 없다면 Object의 기본 equals만으로 해결이 된다.

- **상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다.**

  - Set구현체는 AbstractSet이 구현한 equals를 상속받앙 사용하고, List 구현체들은 AbstractList로 부터, Map 구현체는 AbstractMap으로 부터 그대로 상속받아 사용한다.

- **클래스가 private이거나 package-private(protected)이고, equals 메소드를 호출할 일이 없다.**

  - 만약 위 같은 상황에서 혹시라도 equals가 호출되는 것을 막고 싶다면 다음처럼 구현해라

    ~~~java
    @Override
    public boolean equals(Object o) {
      throw new AssertionError();	//호출 금지
    }
    ~~~

    

#### **그렇다면 우리는 언제 equals를 재정의해야 하는 것일까?**

- 객체 식별성(두 객체가 물리적으로 같은지 == 주소가 같은지)이 아니라 논리적 동치성(값이 같은지)을 확인해봐야 하는데, **상위 클래스의 equals가 논리적 동치성을 비교하도록 재정의가 되지 않을 때다.**
  - 주로 String, Integer 같은 Wrapper Class(값 클래스)가 여기에 해당한다. 
  - Wrapper Class를 equals()로 사용시 주로 값이 같은지를 확인할 것이다. 그러므로, 이 때는 **재정의가 필요하며, 이로 인해 Map키와 Set원소로 사용할 수 있게 된다.**



- 하지만 만약, **값 클래스더라도 같은 인스턴스가 둘 이상 만들어지지 않음을 보장하는 인스턴스(Ex. 리터럴 String)라면 equals를 재정의 하지 않아도 된다.**
  - 이 경우에서는 같은 인스턴스가 2개 이상 만들어지는 것이 아니기 때문에 **논리적 동치성과 객체 식별성이 사실상 똑같은 의미가 된다. 즉, Object의 equals가 논리적 동치성까지 확인해준다고 볼수 있다.**



#### **equals() 재정의 시 따라야할 일반 규약**

- **반사성(reflexivity)** : null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true이다.
- **대칭성(symmetry)** : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)가 true이면 y.equals(x)도 true이다.
- **추이성(transitivity)** : null이 아닌 모든 참조 값 x, y, z에 대해, x.equals(y)가 true이면 y.equals(z)도 true면, x.equals(z)도 true이다.
- **일관성(consistency)** : null이 아닌 모든 참조 값 x,y에 대해, x.equals(y)를 반복해서 호출하면 항상 true를 반환하거나 항상 false를 반환한다.
- **null - 아님** : null이 아닌 모든 참조 값 x에 대해, x.equals(null)은 **false**이다.

~~~java
//Integer equals
public boolean equals(Object obj) {
  if (obj instanceof Integer) {
    return this.value == (Integer)obj;
  } else {
    return false;
  }
}

//String equals
public boolean equals(Object anObject) {
  if (this == anObject) {
    return true;
  } else {
    if (anObject instanceof String) {
      String aString = (String)anObject;
      if (this.coder() == aString.coder()) {
        return this.isLatin1() ? StringLatin1.equals(this.value, aString.value) : StringUTF16.equals(this.value, aString.value);
      }
    }

    return false;
  }
}
~~~

**equals()를 재정의 할 때는 위의 규약을 반드시 따라야 하며, 이 규 약을 어길 시 프로그램이 이상하게 동작하거나 종료될 것이고, 원인이 되는 코드를 찾기도 굉장히 어려울 것이다.**

equals()는 true 혹은 false만 return 한다.



## **Object에서 말하는 동치관계란?**

Object에서 말하는 동치관계는 쉽게 말해, **서로 같은 원소들로 이뤄진 부분집합으로 나누는 연산**을 말한다. 이 부분집합을 **동치 클래스**라 한다. equals()가 쓸모 있으려면 모든 원소가 같은 동치류에 속하여 어떤 원소와도 서로 교환할 수 있어야 한다.



## 동치 관계를 만족시키기 위한 요건

1. #### **반사성**은 단순히 말해 객체는 자기 자신과 같아야 한다는 뜻이다.

   - 이 요건을 어긴 클래스를 확인하려면, 해당 클래스의 인스턴스를 컬렉션에 넣고 contains()를 호출하면 인스턴스가 없다고 답할 것이다.

   

2. #### **대칭성**은 두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다는 뜻이다.

   - 아래 클래스에서 toString 메소드는 원본 문자열의 대소문자를 그대로 돌려주지만, equals에서는 대소문자를 무시하여 확인하다.

   ~~~java
   public final class CaseInsensitiveString {
       private final String s;
   
       public CaseInsensitiveString(String s) {
          this.s = Objects.requireNonNull(s);
       }
   
     	//대소문자 무시후 비교
       public boolean equals(Object o) {
           if(o instanceof CaseInsensitiveString) {
               return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
           }
           
           if(o instanceof String) {
               return s.equalsIgnoreCase((String) o);
           }
           return false;
       }
   }
   
   //String equalsIgnoreCase
   public boolean equalsIgnoreCase(String anotherString) {
     return this == anotherString ? true : anotherString != null &&
       anotherString.length() == this.length() && 
       this.regionMatches(true, 0, anotherString, 0, this.length());
   }
   
   //String 대소문자 무시 로직
   public static boolean regionMatchesCI(byte[] value, int toffset, byte[] other, int ooffset, int len) {
     int last = toffset + len;
   
     while(toffset < last) {
       char c1 = (char)(value[toffset++] & 255);	//char형으롭 변경 
       char c2 = (char)(other[ooffset++] & 255);
       if (c1 != c2) {
         char u1 = Character.toUpperCase(c1);	//대문자로 변경
         char u2 = Character.toUpperCase(c2);
         if (u1 != u2 && Character.toLowerCase(u1) != Character.toLowerCase(u2)) {
           return false;
         }
       }
     }
   
     return true;
   }
   ~~~

   

   - CaseInsensitiveString의 equals는 순진하게 일반 문자열과도 비교를 시도한다. 

     ~~~java
     CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
     String s = "polish";
     ~~~

     - 위를 참고로 cis.equals(s)를 실행해봤을 때 결과는 true를 반환한다.
     - 문제는 CaseInsensitiveString의 equals는 일반 String을 알고 있지만, String의 equals는 CaseInsensitiveString의 존재여부를 모른다. **따라서 s.equals(cis)는 false라는 결과를 나타낸다.**
     - 즉, **명백한 대칭성 위반이다.**

     

   - 이번에는 CaseInsensitiveString을 컬렉션에 넣어보자

     ~~~java
     List<CaseInsensitiveString> List = new ArrayList<>();
     list.add(cis);
     ~~~

     - list.contains(s);를 호출하면 어떤 결과가 나올까?
       - JDK 버전에 따라 다르므로 true 혹은 false를 반환하거나 런타임 예외가 발생할 수도 있다.

   

   - 여기에서 요점은 **equals 규약을 어기면 그 객체를 사용하는 다른 객체들이 어떻게 반응할지 모른다는 것이다.**

   - 위의 문제를 해결하려면 **CaseInsensitiveString의 equals를 String과도 연동하겠다는 꿈을 버려야 한다.**

     ~~~java
     @Override
     public boolean equals(Object o) {
       return o instanceof CaseInsensitiveString && ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
     }
     ~~~

     

   

   - **equalsIgnoreCase에 속한 regionMatchesCI의 로직흐름을 직접 테스트 해봤다.**

   ~~~java
   public static void main(String [] args) {
   
     String str2 = new String("hHllo");
   
     //1. 받아온 String을 byte배열에 담는다.
     byte [] bt = str2.getBytes();
     System.out.println("비트 값 : " + bt[0] + ", " + bt[1]);
   
     //2. 255로 비트연산을 진행 후 char형으로 변환한다.
     char a = (char) (bt[0] & 255);
     char b = (char) (bt[1] & 255);
     System.out.println("char 변환 값 : " + a +","+ b);
   
     //3. Character의 toUpperCase()를 활용하여 모두 대문자로 변경한다.
     char u1 = Character.toUpperCase(a);
     char u2 = Character.toUpperCase(b);
     System.out.println("대문자 변환 값 : " + u1 +","+ u2);
   }
   ~~~

   - **실행 결과**

   ![image](https://user-images.githubusercontent.com/40616436/74012433-954a0180-49cd-11ea-8f04-c689bbfe33d1.png)

   

   

3. #### 추이성은 첫번째 객체와 두번째 객체가 같고, 두번째 객체와 세번째 객체가 같다면, 첫번째 객체와 세번째 객체도 같아야 한다는 뜻이다.

   - 만약, 상위 클래스에는 없는 필드를 하위 클래스에 추가하는 상황을 생각해보자(equals 비교에 영향을 주는 정보를 추가한 것이다.)

     ~~~java
     //Point
     public class Point {
         private final int x;
         private final int y;
     
         public Point(int x, int y) {
             this.x = x;
             this.y = y;
         }
     
         @Override
         public 	boolean equals(Object o) {
             if(!(o instanceof Point)) {
                 return false;
             }
             Point p = (Point)o;
             return p.x == x && p.y == y;
         }
     }
     
     //ColorPoint
     public class ColorPoint extends Point {
         private final Color color;
     
         public ColorPoint(int x, int y, Color color) {
             super(x, y);
             this.color = color;
         }
     }
     ~~~

   - equals()를 그대로 둔다면 Point의 구현이 상속되어 색상(Color)정보를 무시한채 비교를 수행할 것이다.

     - equals의 규약을 어긴것은 아니지만 중요한 정보를 놓치게 된다.

     

   - 다음 코드처럼 비교 대상이 또 다른 ColorPoint이고 **위치와 색상이 같을 때만 true를 반환하는 equals를 생각해보자**

     ~~~java
     //ColorPoint
     @Override
     public boolean equals(Object o) {
       if(!(o instanceof ColorPoint)) {
         return false;
       }
       return super.equals(o) && ((ColoPoint) o).color == color;
     }
     ~~~

     -  이 메소드는 일반 Point를 ColorPoint에 비교한 결과와 그 둘을 바꿔 비교한 결과가 다를 수 있다.

       - **Point의 equals는 색상을 무시하고, ColorPoint의 equals는 입력 매개변수의 클래스 종류가 다르다며 매번 false를 return할 것이다.**

     - 실행 동작을 확인해보자.

       ~~~java
       public static void main(String [] args) {
         Point p = new Point(1, 2);
         ColorPoint cp = new ColorPoint(1, 2, Color.RED);
       }
       ~~~

       - 실행결과는 **p.equals(cp)는 true이고, cp.equals(p)는 false**이다.

     

   - **ColorPoint.equals가 Point와 비교할 때는 색상을 무시하도록 하면 어떨까?**

     ~~~java
     //ColorPoint 
     public boolean equals(Object o) {
        if(!(o instanceof ColorPoint)) {
          return false;
        }
     
        //o가 일반 Point이면 색상을 무시하고 비교한다.
        if(!(o instanceof ColorPoint)) {
          return o.equals(this);
        }
     
        return super.equals(o) && ((ColorPoint) o).color == color;
      }
     ~~~

     - **이 방식을 사용하면 대칭성은 지켜주지만, 추이성을 깨뜨린다.**

       ~~~java
       public static void main(String [] args) {
         //2. ColorPoint equals 변경
         ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
         Point p2 = new Point(1, 2);
         ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
       }
       ~~~

       - 이제 p1.equals(p2)와 p2.equals(p3)는 true를 반환하는데, p1.equals(p3)가 false를 반환한다.
         - 추이성이 명백히 위반된다.
         - **p1과 p2, p2와 p3 비교에서는 색상을 무시했지만, p1과 p3는 색상까지 고려했기 때문이다.**
       - 또한, 이 방식은 무한 재귀에 빠질 위험도 있다.
         - 만약 Point의 또다른 하위 클래스인 SmallPoint를 만들고, equals는 같은 방식으로 구현했다고 치자.
         - 그런 후 ColorPoint.equals(SmallPoint);를 할 시 ColorPoint의 두번째 if문을 통해 SmallPoint의 equals()로 진입할 것이다.
         - 하지만, SmallPoint.equals()에서도 두번째 if문을 타게 되어, 다시 ColorPoint.equals()로 갈 것이다.
         - 이로 인해 무한 재귀 상태에 빠지게 되는 것이다.

     

     - 사실 **이 현상(대칭성은 지켜주되 추이성은 깨뜨림)은 모든 객체 지향 언어의 도치관계에서 나타나는 근본적인 원인**이다.

       - **구현 클래스를 확장해 새로운 값을 추가하면서 equals 규약을 만조시킬 방법은 존재하지 않는다.**

         - 객체 지향적 추상화의 이점을 포기하지 않는 한 말이다.

       - 이 말은 얼핏, **equals 안에 intanceof 검사를 getClass 검사로 바꾸면 규약도 지키고 값도 추가하면서 구현 클래스를 상속할 수 있다는 뜻으로 보이는데..**

         ~~~java
         //Point
         @Override
         public boolean equals(Object o) {
           if(o == null || o.getClass() != getClass()) {
             return false;
           }
           Point p = (Point)o;
           return p.x == x && p.y == y;
         }
         ~~~

         - **이번 equals는 같은 구현 클래스의 객체와 비교할 때만 true를 반환한다.**
         -  **Point의 하위 클래스는 정의상 여전히  Point이므로 어디서든 Point로써 활용될 수 있어야 하는데, 이 방식에서는 그렇지 못한다.**
           - 예를들어 Point를 확장한 CounterPoint를 선언하고 Set 컬렉션에 Point 객체와 CounterPoint 객체를 add 한 후 contains(Point p)에 Point 대신 CounterfPoint가 매개변수로 선언되면, getClass()로 인해 CounterPoint에 해당하는 객체만 true로 반환 될 것이다.
           - 이유는, CoutnerPoint 인스턴스는 어떤 Point와도 같을 수 없기 때문이다.

     

   - **구체 클래스의 하위 클래스에서 값을 추가할 방법은 없지만, 괜찮은 우회 방법이 있다.**

     - 바로 **'상속 대신 컴포지션을 사용'**하는 것이다.

       ~~~java
       public class InheritanceColorPoint {
           private final Point point;
           private final Color color;
       
           public InheritanceColorPoint(int x, int y, Color color) {
               point = new Point(x, y);
               this.color = Objects.requireNonNull(color);
           }
       
         	//이 InheritanceColorPoint에서 Point 뷰를 반환한다.
           public Point asPoint() {
               return point;
           }
           
           public boolean equals(Object o) {
               if(!(o instanceof InheritanceColorPoint)) {
                   return false;
               }
       
               InheritanceColorPoint cp = (InheritanceColorPoint) o;
               return cp.point.equals(point) && cp.color.equals(color);
           }
       }
       ~~~

       

     

     

4. #### **일관성**은 두 객체가 같다면 앞으로도 영원히 같아야 한다는 뜻이다.

   - 가변 객체의 경우 비교 시점에 따라 서로 다를 수도 있고, 같을 수도 있는 반면,
   - 불변 객체는 한번 다르면 끝까지 달라야 한다.
     - 만약 불변 객체로 만든다고 하면 equals가 한번 같다고 한 객체와는 영원히 같아야 하고, 다르다고 한 객체와는 영원히 다르다고 해야한다.

   

   - 클래스가 불변이든 가변이든 **equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안된다.(일관성 조건에 중요)**
     - 예컨대, java.net.URL의 equals는 주어진 URL과 매핑된 호스트의 IP 주소를 이용해 비교한다.
     - 호스트 이름을 IP 주소로 바꾸려면 네트워크를 통해야 하는데, 그 결과가 항상 같다고 보장할 수 없다.
       - 이는 URL의 equals가 일반 규약을 어기게 하고, 실무에서도 종종 발생되는 문제이다.
     - **URL의 equals를 이렇게 구현한 것은 커다란 실수였으니 절대 따라 해서는 안된다.**

   

   - **equals는 항시 메모리에 존재하는 객체만을 사용한 결정적 계산만을 수행해야 한다.**



5. #### **Null-아님**은 이름처럼 모든 객체가 null과 같지 않아야 한다는 뜻이다.

   - o.equals(null)이 true를 반환하는 상황은 상상하기 어렵지만, **실수로 NullPointerException을 던지는 코드는 흔할 것이다.**

   - 이 일반 규약은 이런 경우(NullPointerException)도 허용하지 않는다.

   - 다음 코드처럼 입력이 null인지 확인해 자신을 보호한다.

     ~~~java
     //명시적 null 검사 - 얘보다는 묵시적으로 검사하자.
     @Override
     public boolean equals(Object o) {
       if(o == null) { 
       	return false;
       }
     }
     ~~~

   - 동치성 검사를 하려면 equals는 건네받은 객체를 적절히 형변환 한 후 필수 필드들의 값을 알아내야 한다. 그러려면 **형변환을 하기 전에 instanceof 연산자로 입력 매개변수가 올바른 타입인지 검사해야한다.**

     ~~~java
     //묵시적 null 검사 - 이렇게 사용하자.
     @Override
     public boolean equals(Object o) {
       if(!(o instanceof MyType)) { 
       	return false;
       }
       MyType myType = (MyType) o;
     }
     ~~~

     - equals가 타입을 확인하지 않으면 타입이 잘못 주어졌을 때 **ClassCaseException을 던져서 일반 규약을 위배하게 된다.**
     - **instanceof를 사용하게 되면 첫번째 피연산자가 null이면 무조건 false를 반환하게 되므로 명시적으로 null검사를 하지 않아도 된다.**

     

   - **지금까지의 내용을 종합해서 양질의 equals 메서드 구현 방법을 단계별로 정리해보자**

     1. == 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.
        - 자기 자신이면 true, 이는 단순한 성능 최적화용으로 비교 작업이 복잡한 상황일 때 값어치를 한다.
     2. instanceof 연산자로 입력이 올바른 타입인지 확인한다.
        - 가끔 해당 클래스가 구현한 특정 인터페이스를 비교할 수도 있다.
        - 만약 어떤 인터페이스를 구현한 서로 다른 클래스를 같은 타입으로 주기 위해 equals 규약을 수정할 수도 있다.
        - 이런 인터페이스를 구현한 클래스라면 equals에서 해당 인터페이스를 사용해야한다.
          - Ex. Set, List, Map, Map.Entry 등의 컬렉션 인터페이스들이 여기에 해당한다.
     3. 입력을 올바른 타입으로 형변환한다.
        - instanceof로 인해 이 단계는 100% 성공한다.
     4. 입력 객체와 자기 자신의 대응되는 핵심 필드들이 모두 일치하는지 하나씩 검사한다.
        - 모두 일치해야 true를 반환한다.

   

## **equals를 다 구현했다면 세 가지만 자문해보자**

**대칭적인가? 추이성이 있는가? 일관적인가?** 나머지(반사성과 null-아님)가 문제되는 경우는 거의 없다.

~~~java
//가장 전형적인 equals 메서드의 예
public class PhoneNumber {
    private final short areaCode, prefix, lineNum;
    
    public PhoneNumber(int areaCode, int prefix, int lineNum) {
        this.areaCode = rangeCheck(areaCode, 999, "지역코드");
        this.prefix = rangeCheck(prefix, 999, "프리픽스");
        this.lineNum = rangeCheck(lineNum, 9999, "가입자 번호");
    }
    
    private static short rangeCheck(int val, int max, String arg) {
        if(val < 0 || val > max) {
            throw new IllegalArgumentException(arg + ": " + val);
        }
        return (short) val;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        
        if(!(o instanceof PhoneNumber)) {
            return false;
        }
        
        PhoneNumber pn = (PhoneNumber) o;
        return pn.lineNum == lineNum && pn.prefix == prefix
                && pn.areaCode == areaCode;
    }
}
~~~

- 가장 전형적인 equals()를 보여준다.



## 마지막으로 equals의 주의사항을 살펴보자

- **equals를 재정의할 때는 hashCode도 반드시 재정의하자(아이템 11)**

- 너무 복잡하게 해결하려 들지 말자.

  - 필드들의 동치성 검사만 해도 equals 규약을 어렵지 않게 지킬 수 있다.
    - 예컨대, *를 비교하지 않는게 좋다.

- **Obejct 외의 타입을 매개변수로 받는 equals 메소드는 선언하지 말자**

  - 많은 프로그래머가  equals를 다음과 같이 코딩하고 문제의 원인을 찾기 위해 헤맨다.

    ~~~java
    public boolean equals(MyClass o) {
      ...
    }
    ~~~

  - 이 메소드는 Object.equals를 재정의한 게 아니다. **입력 타입이  Object가 아니므로 재정의가 아니라 다중정의(Overloading)를 한 것이다.**

    - 이 처럼 타입을 구체적으로 명시한 equals는 오히려 해가 된다.

    - **이는 @Override를 명시해주면 이러한 실수를 예방할 수 있다.**

      ~~~java
      //컴파일 에러
      @Override
      public boolean equals(MyClass o) {
        ...
      }
      ~~~

- equals를 작성하고 테스트하는 일은 지루하고 이를 테스트하는 코드는 항상 뻔하다. 이 작업은 **구글에서 만든 AutoValue 프레임워크** 오픈소스를 활용하여 테스트하면 좋다.

  - 클래스에 어노테이션 하나만 추가하면 AutoValue가 이 메소드들을 알아서 작성해주며, 우리가 직접 작성하는 것과 근본적으로 똑같은 코드를 만들어준다.



## 핵심 정리

- 꼭 필요한 경우가 아니면 equals를 재정의 하지 말자
- 많은 경우에 Object의 equals가 우리가 원하는 비교를 수행해준다.
- 재정의 해야 할 때는 그 클래스의 핵심 필드 모두를 빠짐없이, 다섯가지(**대칭적, 추이성, 일관성,** 반사성, null-아님) 규약을 확실히 지켜가며 비교해야 한다.

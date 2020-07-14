## 생성자에 매개변수가 많다면 빌더를 고려하라

### 점층적 생성자 패턴

- 생성자를 매개변수 개수에 따라 계속적으로 추가하는 패턴을 말함
- 이 클래스의 인스턴스를 만드려면 원하는 매개변수를 모두 포함하는 생성자 중 가장 짧은 것을 골라 호출하면 되는데, 클라이언트 입장에서 매개 변수로 불필요한 변수를 삽입할 때 헷갈릴 수 있다,
- 결국, 클라이언트가 실수로 매개변수의 순서를 바꾸어 값이 들어오면 런타임 에러가 발생할 수 있다.
- 이런 단점을 해결하기 위해 자바빈즈 패턴을 활용한다.



### 자바빈즈 패턴

- 기본 생성자만을 만들고 매개변수로 필요한 값들은 set메서드를 활용해서 객체를 완성하는 특징이다.
- 하지만 자바빈즈 패턴에서는 객체 하나를 만들려면 메서드를 여러 개 호출해야하고, 객체가 완전히 생성되기 전까지는 일관성이 무너진 상태에 놓이게 된다.
- 점층적 생성자 패턴에서는 매개변수들이 유효한지를 생성자에서만 확인하면 일관성을 유지할 수 있었는데, 그 장치가 완전히 사라진 것이다.
- 이런 단점을 해결하기 위해 빌더 패턴을 활용한다.



### 빌드 패턴

- 점층적 생성자 패턴의 안전성과 자바 빈즈 패턴의 가독성을 가져온 패턴이다.
- 클라이언트는 필요한 객체를 직접 만드는 대신, 필수 매개변수만으로 생성자를 호출해 빌더 객체를 얻는다.
- 그런 다음 객체가 제공하는 일종의 세터 메서드들로 원하는 선택 매개변수들을 설정한다.
- 마지막으로 매개변수가 없는 build 메서드를 호출해 우리에게 필요한 객체를 얻는다.
- 소스로 살펴보자

```java
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    ....
    
    //Builder 객체
    public static class Bulider {
        //필수 매개변수
        private final int servingSize;
        pirvate final int servings;
        
        //선택 매개변수
        private final int calories = 0;
        private final int fat = 0;
        
        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }
        
        //일종의 Setter 메서드들
        public Builder calories(int val) {
            calories = val;
            return this;	//.으로 체인화 가능
        }
        public Builder fat(int val) {
            fat = val;
            return this;
        }
        
        //마지막으로 매개변수가 없는 build 메서드 호출로 객체를 얻음
        public NutritionFacts build() {
        	return new NutritoinFacts(this);
        }
    }
        
     private NutritionFacts(Builder builder) {
            servingSize = builder.servingSize;
            servings = builder.servings;
            calories = builder.calories;
            fat = builder.fat;
        }   
    }
}

//Main
public class Main {
    public static void main(String[] args) {
	    //Builder Pattern
        NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8).calories(100).builder();
    }
}
```

- 이런 방식을 메서드 호출이 물 흐르듯 연결된다는 뜻으로 플루언트 API 혹은 메서드 연쇄라고 한다.



### 빌더 패턴과 계층적으로 설계된 클래스

- 빌더 패턴은 계층적으로 설계된 클래스와 함께 쓰기 좋다.

- ```java
  public abstract class Pizza{
      public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
      final Set<Topping> toppings;
  
      abstract static class Builder<T extends Builder<T>>{
          EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);
          public T addTopping(Topping topping){
              toppings.add(Objects.requireNonNull(topping));
              return self();
          }
  
          abstract Pizza build();
  
          //하위 클래스는 이 메서드를 재정의하여 this를 반환해야 함
          protected abstract T self();
      }
  
      Pizza(Builder<?> builder){
          toppings = builder.toppings.clone();
      }
  }
  ```

  - Pizza.Builder 클래스는 재귀적 타입 한정(아이템 30)을 이용한 제네릭 타입이다.
  - 여기에 self()를 더해 하위 클래스에서는 형변환을 하지 않고 메서드 연쇄를 지원할 수 있다.(하위 클래스에서 this를 반환)

- ```java
  //뉴욕 피자
  public class NyPizza extends Pizza{
      public enum Size {SMALL, MEDIUM, LARGE }
      private final Size size;
  
      public static class Builder extends Pizza.Builder<Builder>{
          private final Size size;
  
          public Builder(Size size){
              this.size = Objects.requireNonNull(size);
          }
  
          @Override
          public NyPizza build() {
              return new NyPizza(this);
          }
  
          @Override
          protected Builder self() {
              return this;
          }
      }
  
      private NyPizza(Builder builder) {
          super(builder);
          size = builder.size;
      }
  }
  
  //칼초네 피자
  public class Calzone extends Pizza{
      private final boolean sauceInside;
  
      public static class Builder extends Pizza.Builder<Builder>{
          private boolean sauceInsize = false;
  
          public Builder sauceInsize(){
              sauceInsize = true;
              return this;
          }
  
          @Override
          public Calzone build() {
              return new Calzone(this);
          }
  
          @Override
          protected Builder self() {
              return this;
          }
      }
  
      private Calzone(Builder builder) {
          super(builder);
          sauceInside = builder.sauceInsize;
      }
  }
  
  //Main
  public class main{
      public static void main(String[] args) {
      	NyPizza nyPizza = new NyPizza.Builder(NyPizza.Size.SMALL).addTopping(Pizza.Topping.SAUSAGE).addTopping(Pizza.Topping.ONION).build();
  		Calzone calzone = new Calzone.Builder().addTopping(Pizza.Topping.HAM).sauceInsize().build();
  	}
  }
  
  ```

  - 각 하위 클래스의 빌더가 재정의한 build 메서드는 하위 클래스의 인스턴스를 반환하도록 선언한다.
  - NyPizza.Builder는 NyPizza를 반환, Calzone.Builder는 Calzone을 반환한다는 뜻이다.
  - 즉, 하위 클래스가 하위 타입의 클래스를 반환하므로  형변환이 따로 필요로 하지 않는다.
  - 메서드를 여러번 호출하도록 하고, 각각에서 호출할 때 넘겨진 매개변수들을 하나의 필드로 모을 수도 있는데, 이를 활용한 것이 addTopping이다.
    - Builder.addTopping의 반환값은 self() 즉, 하위 클래스 자신이므로 하위클래스는 상속받은 Builder의 addTopping에 접근이 가능하여 각 호출때 넘겨진 매개변수들을 하나의 필드로 모으는 것이다.

- 빌더 패턴은 상당히 유연하다. 빌더 하나로 여러 객체를 순회하면서 만들 수 있고, 빌더에 넘기는 매개변수에 따라 다른 객체를 만들 수도 있다.

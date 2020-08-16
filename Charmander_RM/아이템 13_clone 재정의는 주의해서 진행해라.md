**아이템13_clone 재정의는 주의해서 진행해라**

Cloneable은 어떤 클래스를 복제해도 된다는 사실을 알리기 위해서 만들어진 믹스인 인터페이스(아이템20)이다. Java의 Cloneable 인터페이스를 보면 아무런 메소드가 보이지 않지만 사실은 Object의 clone() 메소드의 동작방식을 결정하고 있다.

**그럼 Cloneable의 목적을 자세히 살펴보자.**

> 다시 말해, Cloneable의 인터페이스를 구현한 클래스는 Object의 메소드인 clone()을 어떤 식으로 사용할 것인지를 결정할 수 있게 한다.
>
> 즉, Cloneable을 구현한 인스턴스에서 clone() 메소드를 호출하게 되면, ***해당 객체를 필드 단위로 복사한 객체를 반환하게 된다.*** 
>
> 하지만 Cloneable을 구현하지 않은 인스턴스에서 clone() 메소드를 호출하게 되면 *CloneNotSupportedException* 예외를 던진다.



**그렇다면 Cloneable을 어떻게 사용하면 되는 것일까?**

~~~java
public class MyCloneable implements Cloneable{
  private String name;

  public MyCloneable(String name) {
    this.name = name;
  }

  public void chgName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public MyCloneable clone() throws CloneNotSupportedException {
    return (MyCloneable)super.clone();
  }

  @Override
  public String toString() {
    return "MyCloneable{" +
      "name='" + name + '\'' +
      '}';
  }
}


public static void main(String[] args) throws CloneNotSupportedException {
  MyCloneable myCloneable = new MyCloneable("mesung");
  MyCloneable cloneMyCloneable = myCloneable.clone();	//복제
  
  //복제한 인스턴스와 비교
  System.out.println(cloneMyCloneable.getName().equals(myCloneable.getName()));

  //이름 변경
  System.out.println(cloneMyCloneable);
  cloneMyCloneable.chgName("ime_sung");
  System.out.println(cloneMyCloneable);
}
~~~

<img src="https://user-images.githubusercontent.com/40616436/89778820-1e8eec00-db49-11ea-98df-85343ee1992d.png" alt="image" style="zoom:50%;" />

소스에 대해서 간략히 살펴보면, 재정의한 clone() 메소드는 다른 패키지에서 접근이 가능하기 위해 접근 제한자를 protected가 아닌 public으로 구현한 것을 볼 수 있다. 또한, super.clone() 메소드에 의해서 MyCloneable 인스턴스에 대한 복제가 완벽히 이루어지고 있고, 공변 반환 타입으로 인해 상위 클래스의 메소드가 반환하는 타입(Object)의 하위 타입이 가능한 것이다.



**가변 객체가 포함된 객체 복사는 조심해야 한다.**

위 소스처럼 고정 객체(String)만 있을 때에는 아무 이슈가 없지만, 만약 리스트와 같은 가변 객체가 포함되어 있으면 super.clone() 메소드의 사용을 조심해야 한다.

*그 이유는, 복제된 인스턴스의 가변 객체를 변경하게 되면 원본 객체도 동일하게 변경되는 현상이 나타나게 된다.*

~~~java
public class MyCloneableList implements Cloneable{
  private String name;
  private List<String> nameList = new ArrayList<>();

  public MyCloneableList(String name) {
    addName(name);
  }

  public void chgName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void addName(String name) {
    nameList.add(name);
    this.name = name;
  }

  public List<String> getNameList() {
    return nameList;
  }

  @Override
  protected MyCloneableList clone() throws CloneNotSupportedException {
    return (MyCloneableList)super.clone();
  }
}

public static void main(String[] args) throws CloneNotSupportedException {
  MyCloneableList myCloneableList = new MyCloneableList("richard");
  myCloneableList.addName("jun");

  MyCloneableList cloneMyCloneableList = (MyCloneableList) myCloneableList.clone();
  cloneMyCloneableList.addName("won");

  int myCloneableListSize = myCloneableList.getNameList().size();
  int cloneMyCloneableListSize = cloneMyCloneableList.getNameList().size();

  System.out.println(myCloneableListSize);
  System.out.println(myCloneableList.getNameList().get(myCloneableListSize-1));
  
  System.out.println(cloneMyCloneableListSize);
  System.out.println(cloneMyCloneableList.getNameList().get(cloneMyCloneableListSize-1));
}
~~~

<img src="https://user-images.githubusercontent.com/40616436/89784646-f9ec4180-db53-11ea-8815-10349803296a.png" alt="image" style="zoom:50%;" />

즉, 가변 객체를 포함한 객체를 복사할 때는 각별히 조심해야한다는 것이다.

clone() 메소드는 다른 종류의 생성자라고 볼 수 있다. 생성자를 통해 생성되는 객체는 힙 영역에 새로운 메모리를 차지하므로 clone() 메소드를 사용하여 복제된 객체 또한 고유한 클래스여야 한다. 다시 말해 원본 객체에게 피해를 주면 안 되는 것이다.

그럼 가변 객체를 포함했을 때 Cloneable을 사용한 소스를 재구현해보겠다.

~~~java
public class MyCloneableList implements Cloneable{
  private String name;
  private List<String> nameList = new ArrayList<>();

  ...

    @Override
    protected MyCloneableList clone() throws CloneNotSupportedException {
    //얕은 복사
    //return (MyCloneableList)super.clone();

    //깊은 복사
    //Object clone() 사용
    MyCloneableList myCloneableList = (MyCloneableList)super.clone();

    //깊은 복사를 위한 가변 객체 복제
    List<String> copyList = new ArrayList<>();
    for(int i = 0; i < this.nameList.size(); i++) {
      copyList.add(this.nameList.get(i));
    }
    myCloneableList.nameList = copyList;
    return myCloneableList;
  }

  ...
}

public static void main(String[] args) throws CloneNotSupportedException {
  MyCloneableList myCloneableList = new MyCloneableList("richard");
  myCloneableList.addName("jun");

  MyCloneableList cloneMyCloneableList = (MyCloneableList) myCloneableList.clone();
  cloneMyCloneableList.addName("won");

  int myCloneableListSize = myCloneableList.getNameList().size();
  int cloneMyCloneableListSize = cloneMyCloneableList.getNameList().size();


  System.out.println(cloneMyCloneableListSize);
  System.out.println(cloneMyCloneableList.getNameList().get(cloneMyCloneableListSize-1));

  System.out.println(myCloneableListSize);
  System.out.println(myCloneableList.getNameList().get(myCloneableListSize-1));
}
~~~

<img src="https://user-images.githubusercontent.com/40616436/89784579-dde8a000-db53-11ea-8044-55231f674130.png" alt="image" style="zoom:50%;" />

위 결과를 보듯이 정상적으로 복제가 된 것을 확인할 수 있다. 가변 객체가 포함된 클래스에서 Cloneable을 사용하여 복제하기 위해서는 super.clone() 메소드를 호출하고 나서 **가변 객체도 복제될 수 있는 로직을 추가해야 한다.**

**요약하자면,** Cloneable을 구현하는 모든 클래스는 clone() 메소드를 재정의해야하고, 접근 제한자는 public으로 구현하여 모든 패키지에서 접근이 가능하게 해야 한다. 또한 반환 타입은 클래스 자신으로 변경하고 가변 객체가 존재할 시 가변 객체도 복제될 수 있는 로직을 구성하여 재정의해야 한다.



**clone() 메소드 방식을 가장 깔끔하게 사용한 배열..**

만약 위 같은 동적 배열 말고 정적 배열을 사용하게 된다면 위 소스 처럼 로직을 복잡하게 구현할 필요는 없다. **정적 배열의 경우 clone() 메소드를 가장 깔끔하게 사용한 것이라는 사실을 알기 바란다.**

~~~java
public class MyCloneableList implements Cloneable{
    private String name;
    private List<String> nameList = new ArrayList<>();
    private Object[] elements = new Object[10];
    private int arrSize = 0;

    ...

    public void printArr() {
        for(int i = 0; i < this.elements.length; i++) {
            if (elements[i] == null) {
                continue;
            }
            System.out.print(elements[i] + " ");
        }
        System.out.println();
    }

    @Override
    protected MyCloneableList clone() throws CloneNotSupportedException {
        MyCloneableList myCloneableList = (MyCloneableList)super.clone();
      
      	//Lsit 복제
        List<String> copyList = new ArrayList<>();
        for(int i = 0; i < this.nameList.size(); i++) {
            copyList.add(this.nameList.get(i));
        }
        myCloneableList.nameList = copyList;
      
      	//배열 복제(clone() 사용)
        myCloneableList.elements = elements.clone();
        return myCloneableList;
    }
}

public static void main(String[] args) throws CloneNotSupportedException {
  MyCloneableList myCloneableList = new MyCloneableList("richard");
  myCloneableList.addName("jun");

  MyCloneableList cloneMyCloneableList = (MyCloneableList) myCloneableList.clone();
  cloneMyCloneableList.addName("won");

	...

  //배열
  myCloneableList.printArr();
  cloneMyCloneableList.printArr();
}
~~~

<img src="https://user-images.githubusercontent.com/40616436/89787300-2c983900-db58-11ea-971e-5dcbe4c413a8.png" alt="image" style="zoom:50%;" />

소스에서 살펴본 바와 같이 Object의 정적 배열을 선언하게 되면 배열의 clone() 메소드를 재귀적으로 호출하여 복제를 해주는 것이다.

---

**복사 생성자와 복사 팩토리**

객체를 복제하는 또 다른 방법이 있는데, 그것은 바로 복사 생성자와 복사 팩토리를 사용하는 것이다.

> 복사 생성자는 자신과 같은 클래스의 인스턴스를 매개 변수로 받는 생성자를 말하는 것이다.
>
> - Ex. public Yum(Yum yum) {...}
>
> 복사 팩토리는 복사 생성자를 정적 팩토리 형식으로 정의한 것이다.
>
> - Ex. public static Yum newInstance(Yum yum) {...}

복사 생성자와 복사 팩토리를 사용하면 Cloneable 방식처럼 불필요한 check exception 처리가 필요 없고, 형변환도 필요 없다. 또한, 직접적인 인스턴스가 아닌 인터페이스 타입의 인스턴스를 매개 변수로 받을 수 있어 유연성 또한 향상 될 수 있는 장점이 있다.

**결과적으로 객체의 복제 기능은 Cloneable보다 복사 생성자와 복사 팩토리를 이용하는 것이 가장 좋다라는 것이다. 하지만 배열 같은 경우는 clone() 메소드를 제대로 사용한 것이니 배열의 경우는 예외이다.**

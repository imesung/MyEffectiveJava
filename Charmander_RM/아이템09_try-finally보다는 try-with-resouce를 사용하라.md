## try-finally보다는 try-with-resouce를 사용하라

자바에서는 전통적으로 자원을 닫고자 할 때 close() 메소드를 사용하여 닫는 경우가 다반사이다. 하지만 개발자들은 누구나 다 실수를 하기 마련이므로 close를 하지 않는 경우가 발생할 수 있다. 이런 경우 심각하게는 성능상 이슈가 발생할 수 있다는 사실을 알 것이다.

이런 경우를 대비하여 Java에서는 2 가지 방법을 제시하고 있다.

1. try-finally
2. try-with-resources(Java 7 이후)

먼저 try-finally 부터 살펴보자.

---

**try-finally**

~~~java
static String firstLineOfFile(String path) throws IOException {
  BufferedReader br = new BufferedReader(new FileReader(path));
  try {
    return br.readLine();
  } finally {
    br.close();
  }
}
~~~

위 같은 방식으로 finally에서 close() 메소드를 호출한다. 그렇다면 자원을 하나 더 사용해보자.

~~~java
static void copy(String src, String dst) throws IOException {
  InputStream in = new FileInputStream(src);
  try {
    OutputStream out = new FileOutputStream(dst);
    try {
      byte[] buf = new byte[BUFFER_SIZE];
      int n;
      while ((n = in.read(buf)) >= 0)
        out.write(buf, 0, n);
    } finally {
      out.close();
    }
  } finally {
    in.close();
  }
}
~~~

위 두 가지 소스에서는 두 가지 단점이 나타나게 되는 데,

1. try 두 번, finally 두 번, 그 이상이 되면..? 코드가 너무 지저분해진다. 즉, **가독성 문제가 발생한다.**

2. 만약 시스템 문제가 발생하면 **스택 추적**이 어렵다.

   - firstLineOfFile() 메소드를 다시 한 번 살펴보면, 시스템 문제로 인해 예외가 try와 finally 모두 발생할 수가 있다. 이 때, try-finally는 두번째 예외(finally - close())가 첫번째 예외(try - readLine())를 덮어버리게 된다.
   - 실제로 스택 내역에 close()의 예외는 등장하지 않고, 두번째 예외만 등장하게 된다.

   ~~~java
   static String firstLineOfFile(String path) throws IOException {
     try {
       //readLine();
       throw new IllegalArgumentException();
     } finally {
       //close();
       throw new NullPointerException();
     }
   }
   ~~~

   <img src="https://user-images.githubusercontent.com/40616436/89183201-d5cbb600-d5d1-11ea-9e4b-88e561164a64.png" alt="image" style="zoom:50%;" />

---

**try-with-resources**

일단 결론부터 말하면, try-with-resources로 인해 try-finally 문제를 해결할 수 있다.

try-with-resources를 사용 하기 전 알아둬야 할 것은, try-with-resources를 사용하는 자원은 AutoCloseable 인터페이스를 구현해야 한다.

~~~java
public interface AutoCloseable {
  void close() throws Exception;
}

class Close implements AutoCloseable {
    @Override
    public void close() throws Exception {
        
    }
}
~~~

위에서 보는 바와 같이 AutoClosable 클래스는 close() 메소드 단 하나만 가지고 있는 메소드이다.

자원이 해당 인터페이스를 구현해야지만 자동적으로 close() 메소드를 호출할 수 있다는 사실을 기억해야 한다. 다행히 자바 라이브러리에서 제공하는 많은 클래스는 이미 AutoCloseable은 구현하거나 확장하고 있다.

~~~java
//자원 1개
static String firstLineOfFile(String path) throws IOException {
  try (BufferedReader br = new BufferedReader(new FileReader(path))) {
    return br.readLine();
  }
}

//자원 2개
static void copy(String src, String dst) throws IOException {
  try (InputStream   in = new FileInputStream(src);
       OutputStream out = new FileOutputStream(dst)) {
    byte[] buf = new byte[BUFFER_SIZE];
    int n;
    while ((n = in.read(buf)) >= 0)
      out.write(buf, 0, n);
  }
}
~~~

자원이 1개든 2개든 매우 깔끔한 코드로 구성되어 있는 것을 볼 수 있다. 그렇다면 close도 잘 되고 있을까? 테스트 해보자.

~~~java
static String firstLineOfFile() throws Exception {
  try (Close c = new Close();
       Open o = new Open()) {
    return "";
  }
}

public static void main(String[] args) throws Exception {
  System.out.println(firstLineOfFile());
}

//AutoCloseable 구현
class Close implements AutoCloseable {
    @Override
    public void close() throws Exception {
        System.out.println("print close");
    }
}
class Open implements AutoCloseable {
    @Override
    public void close() throws Exception {
        System.out.println("print open");
    }
}
~~~

<img src="https://user-images.githubusercontent.com/40616436/89185517-517b3200-d5d5-11ea-914c-c60a534c60ba.png" alt="image" style="zoom:50%;" />

보는 바와 같이 Close와 Open 클래스가 Autocloseable 인터페이스를 구현한 후 try-with-resources를 활용하니 자동으로 close() 메소드가 호출되는 것을 볼 수 있다.

이 뿐만 아니라 try-finally의 단점이었던 예외를 덮어씌워지는 문제도 해결할 수 있다.

~~~java
static String firstLineOfFile() throws Exception {
  try(Open o = new Open()) {
    throw new IllegalArgumentException();
  }
}

class Open implements AutoCloseable {
  @Override
  public void close() throws Exception {
    throw new NullPointerException();
  }
}
~~~

<img src="https://user-images.githubusercontent.com/40616436/89189423-ea607c00-d5da-11ea-988a-28304daf511b.png" alt="image" style="zoom:50%;" />

위 firstLineOfFile() 메소드를 보면 Open 클래스를 생성 후 바로 IllegalArgumentException 발생시키고 close()에서는 NullPointerException을 발생시키는 것을 볼 수 있다. try-finally에서는 close() 메소드의 NullPointerException은 발생되지 않고 IllegalArgumentException만 발생되었는데, try-with-resources의 경우는 Suppressed를 통해 단계별로 발생된 Exception을 모두 보여주고 있다.

**즉,** try-finally와 try-with-resources를 비교해볼 때,

1. 소스의 가독성이 매우 높아졌다.
2. close() 발생한 예외를 보여준다.

또한, try-with-resources에 catch문을 더하면 다수의 예외 처리도 가능하다.

~~~java
static boolean firstLine(String defaultVal) {
  try (Open o = new Open()) {
    Integer i = null;
    return i == 3;	//NullPointerException
  } catch (Exception e) {
    e.printStackTrace();
    System.out.println(defaultVal);
    return false;
  }
}

public static void main(String[] args) throws Exception {
  firstLine("Toppy McTopFace");
}

class Open implements AutoCloseable {
    @Override
    public void close() throws Exception {
        System.out.println("print close");
    }
}
~~~

<img src="https://user-images.githubusercontent.com/40616436/89191592-1cbfa880-d5de-11ea-83c7-b9389331922c.png" alt="image" style="zoom:50%;" />

try-with-resources로 인해 try문을 중첩으로 사용하지 않아도 여러 예외 처리가 가능하다.

1. try-with-resources를 활용하여 Open 객체 생성 시 예외 체크
2. try 문 안에서의 예외 체크(Integer값 비교) - catch에서 예외 처리 진행

*try 중첩을 사용했으면?*

~~~java
static boolean firstLine2(String defaultVal) throws Exception {
  try (Open o = new Open()) {
    try {
      Integer i = null;
      return i == 3;
    } finally {
      System.out.println(defaultVal);
    }
  }
}
~~~

이 처럼 try 중첩을 사용하여 구현하면 매우 복잡한 코드가 나타는 것을 확인할 수 있다.

---

**결과적으로** 꼭 회수해야하는 자원들이 존재할 때는 try-finally 대신에 **try-with-resources** 를 사용해야 코드의 가독성이 높아지고 개발자의 실수 없이 자원을 정상적으로 회수할 수 있다.


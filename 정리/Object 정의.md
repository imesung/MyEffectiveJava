## Object

Object는 객체를 만들 수 있는 구체 클래스지만 기본적으로는 상속해서 사용하도록 설계되었다.

Obejct에서 final이 아닌 **메소드(equals, hashCode, toString, clone, finalize)** 는 모두 재정의를 염두에 두고 설계된 것이라 **재정의 시 지켜야하는 일반 규약이 명확히 정의되어 있다.**

- 그러므로 Object를 상속하는 클래스, 즉 모든 클래스는 이 메소드들을 일반 규약에 맞게 재정의를 해야한다.
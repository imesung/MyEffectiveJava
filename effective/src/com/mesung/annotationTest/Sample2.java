package com.mesung.annotationTest;

import java.util.ArrayList;
import java.util.List;

public class Sample2 {
    @ExceptionTest(ArithmeticException.class)
    public static void m1() {   //성공해야 한다.
        int i = 0;
        i = i/i;
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m2() {   //실패해야 한다. (다른 예외 발생)
        int [] a = new int[0];
        int i = a[1];
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m3() {   //실퍃해야 한다. (예외가 발생하지 않음)

    }

    /*//예외 두개중 하나만 걸려도 true
    @ExceptionTest({IndexOutOfBoundsException.class, NullPointerException.class})
    public static void doublyBad() {
        List<String> list = new ArrayList<>();
        list.addAll(5, null);
    }*/

    //@Repeatable 사용
    @ExceptionTest(IndexOutOfBoundsException.class)
    @ExceptionTest(NullPointerException.class)
    public static void doublyBad() {
        List<String> list = new ArrayList<>();
        list.addAll(5, null);
    }
}

package com.mesung;

import java.util.Comparator;

public class WeaknessOfBoxing {

    //2.
    static Integer i;

    public static void main(String [] args) {
        //1. 박싱된 기본 타입은 값 + 식별성이라는 속성을 갖는다.**
        /*Comparator<Integer> naturalOrder = (i, j) -> (i < j) ? -1 : (i == j ? 0 : 1);
        int num = naturalOrder.compare(new Integer(42), new Integer(42));
        System.out.println(num);*/

        //1. 해결
        /*Comparator<Integer> naturalOrder = (iBoxed, jBoxed) -> {
            int i = iBoxed, j = jBoxed; //오토박싱
            return i < j ? -1 : (i == j ? 0 : 1);
        };
        int result = naturalOrder.compare(new Integer(43), new Integer(43));
        System.out.println(result);*/

        //2. 박싱된 기본 타입은 null을 가질 수 없다.
        /*if(i == 43) {
            System.out.println("믿을 수가 없네..!");
        }*/

        //3. 기본타입이 박싱된 기본타입보다 시간과 메모리 사용면에 더 효율적이다.
        Long sum = 0L;
        for(long i = 0; i <= Integer.MAX_VALUE; i++) {
            sum += i;
        }
        System.out.println(sum);
    }
}


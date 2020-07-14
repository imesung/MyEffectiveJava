package com.mesung.objectEquals.transitivity;

public class EqualsInObj {
    Object obj;

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

}

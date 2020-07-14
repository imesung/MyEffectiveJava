package com.mesung.annotationTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RunTests {
    public static void main(String args[]) throws Exception {
        int tests = 0;
        int passed = 0;
        Class testClass = Class.forName("com.mesung.annotationTest.Sample2");
        for(Method m : testClass.getDeclaredMethods()) {
            if(m.isAnnotationPresent(ExceptionTest.class) || m.isAnnotationPresent(ExceptionTestContainer.class)) {
                tests++;
                try {
                    m.invoke(null);
                    System.out.printf("테스트 %s 실패 : 예외를 던지지 않음 %n", m);
                } catch (InvocationTargetException wrappeedExc) {
                    Throwable exc = wrappeedExc.getCause();
                    int oldPassed = passed;
                    //Class<? extends Throwable> excType = m.getAnnotation(ExceptionTest.class).value();
                    //Class<? extends Throwable>[] excTypes = m.getAnnotation(ExceptionTest.class).value();
                    ExceptionTest[] excTests = m.getAnnotationsByType(ExceptionTest.class);
                    for(ExceptionTest excTest : excTests) {
                        if(excTest.value().isInstance(exc)) {
                            passed++;
                            break;
                        }
                    }
                    if(passed == oldPassed) {
                        System.out.printf("테스트 %s 실패 : %s \n", m, exc);
                    }
                } catch (Exception exc) {
                    System.out.println("잘못 사용한 @ExceptionTest : " + m);
                }
            }
        }
        System.out.println("성공 : " + passed + ", 실패 : " + (tests - passed));
    }
}

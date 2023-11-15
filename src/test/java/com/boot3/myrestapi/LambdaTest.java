package com.boot3.myrestapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

/*
    Functional Interface(함수형 인터페이스)
    : 추상메서드를 1개만 가진 인터페이스
    : 함수형인터페이스의 추상메서드를 오버라이딩 하는 구문을 람다식이나 메서드레퍼런스로 표현할 수 있다
 */
public class LambdaTest {
    @Test @Disabled
    public void runnable() {

        //1. Anonymous Inner class
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous Inner class");
            }
        });
        t1.start();

        //2. Lambda Expression
        Thread t2 = new Thread(() -> System.out.println("Lambda Expression"));
        t2.start();
    }
    
    @Test //@Disabled
    public void consumer() {
        //Immutable List
        List<String> list = List.of("aa", "bb", "cc");

        //Consumer가 함수형 인터페이스   void accept(T t)
        //1. Anonymous Inner class
        list.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("s = " + s);
            }
        });

        //2.Lambda Expression
        //Consumer의 추상 메서드 void accept(T t)
        list.forEach(val -> System.out.println("Value " + val));

        //3.Method Reference
        list.forEach(System.out::println);
    }

}
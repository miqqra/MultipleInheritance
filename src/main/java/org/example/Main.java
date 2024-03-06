package org.example;

public class Main {
    public static void main(String[] args) {
        ResultClass resultClass = new ResultClassImplExpected();

        resultClass.whatever();
        resultClass.other();
        resultClass.nan();

        System.out.println("Hello world!");
    }
}
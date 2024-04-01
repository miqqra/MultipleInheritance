package org.example.usage;

public class Main {
    public static void main(String[] args) {
        ResultClass resultClass = new ResultClass();
        System.out.println();
        System.out.println("whatever result is " + resultClass.whatever(0));
        System.out.println();
        resultClass.other();
        System.out.println();
        System.out.println(resultClass.everyClass());
    }
}

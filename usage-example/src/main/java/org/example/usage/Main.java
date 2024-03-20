package org.example.usage;

public class Main {
    public static void main(String[] args) {
        ResultClass resultClass = new ResultClass();

        resultClass.whatever();
        System.out.println();
        resultClass.other();
        System.out.println();
        System.out.println("\n\n\nParent2 doesn't call super for method other");
    }
}
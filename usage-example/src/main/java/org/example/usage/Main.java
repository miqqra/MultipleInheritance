package org.example.usage;

import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args)
        throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ResultClass resultClass = new ResultClass();

        resultClass.whatever();
//        resultClass.other();
//        resultClass.nan();

//        System.out.println("Hello world!");
    }
}
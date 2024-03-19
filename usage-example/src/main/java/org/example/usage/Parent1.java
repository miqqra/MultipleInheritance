package org.example.usage;

import java.util.Set;

public class Parent1 extends CommonParent {

    public int parent1Field = 1;

    private final int parent1Private = 11;

    public void whatever() {
        System.out.println("Hello from Parent1");
    }


    public void whatever(int n, Set<String> ss) {
        System.out.printf("Hello %d from Parent1", n);
    }

    public void other(){
        System.out.println("Bye from Parent1");
    }
}

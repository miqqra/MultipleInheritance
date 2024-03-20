package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance
public class Parent1 extends Parent1Intermediary {

    public void whatever() {
        System.out.println("Hello from Parent1");
//        super
    }

    public void other() {
        System.out.println("Bye from Parent1");
//        super
    }

//    public void whatever(int n, Set<String> ss) {
//        System.out.printf("Hello %d from Parent1", n);
////        super

//    }
}

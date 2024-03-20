package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

//@MultipleInheritance
@MultipleInheritance(classes = {Parent2.class})
public class Parent1 extends Parent1Intermediary {

    public void whatever() {
        System.out.println("Hello from Parent1");
        super.whatever();
    }

    public void other() {
        super.other();
        System.out.println("Bye from Parent1");
    }

//    public void whatever(int n, Set<String> ss) {
//        System.out.printf("Hello %d from Parent1", n);
////        super

//    }
}

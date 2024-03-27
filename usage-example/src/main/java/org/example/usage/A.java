package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance
//@MultipleInheritance(classes = {Parent2.class})
public class A extends AIntermediary {
    public A() {
        System.out.println("A created");
    }

    public void whatever(int number) {
        System.out.println("A (root) says " + number);
        super.whatever(number + 10);
    }

    public void other() {
        super.other();
        System.out.println("Bye from A");
    }

//    public void whatever(int n, Set<String> ss) {
//        System.out.printf("Hello %d from Parent1", n);
////        super

//    }
}

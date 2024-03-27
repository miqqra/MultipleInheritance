package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {B.class})
public class D extends DIntermediary {
    public D() {
        System.out.println("D created");
    }

    public void whatever(int a) {
        System.out.println("D says " + a);
        super.whatever(a + 1);
    }

//    public void other() {
//        super.other();
//        System.out.println("Bye from D");
//    }

//    public void whatever(int n, Set<String> ss) {
//        System.out.printf("Hello %d from Parent1", n);
////        super

//    }
}

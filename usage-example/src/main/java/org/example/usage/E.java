package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

//@MultipleInheritance
@MultipleInheritance(classes = {D.class})
public class E extends EIntermediary {
    public E() {
        System.out.println("E created");
    }

    public void whatever(int a) {
        System.out.println("E says " + a);
        super.whatever(a + 1);
    }

    public void other() {
        super.other();
        System.out.println("Bye from E");
    }

//    public void whatever(int n, Set<String> ss) {
//        System.out.printf("Hello %d from Parent1", n);
////        super

//    }
}

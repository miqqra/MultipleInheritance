package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

//@MultipleInheritance
@MultipleInheritance(classes = {A.class, B.class})
public class C extends CIntermediary {
    public C() {
        System.out.println("C created");
    }

//    public void whatever(int a) {
//        System.out.println("C says" + a);
//        super.whatever(a + 1);
//    }

    public void other() {
        super.other();
        System.out.println("Bye from C");
    }

//    public void whatever(int n, Set<String> ss) {
//        System.out.printf("Hello %d from Parent1", n);
////        super

//    }
}

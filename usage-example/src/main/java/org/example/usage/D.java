package org.example.usage;

import java.util.Set;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {B.class})
public class D extends DIntermediary {
    public D() {
        System.out.println("D created");
    }

    public int whatever(int a) {
        System.out.println("D says " + a);
        return super.whatever(a + 1);
    }

    public Set<String> everyClass() {
        Set<String> fromParent = super.everyClass();
        fromParent.add("D");
        return fromParent;
    }

//    public void whatever(int n, Set<String> ss) {
//        System.out.printf("Hello %d from Parent1", n);
////        super

//    }
}

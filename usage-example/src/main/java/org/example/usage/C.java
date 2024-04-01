package org.example.usage;

import java.util.Set;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {A.class, B.class})
public class C extends CIntermediary {
    public C() {
        System.out.println("C created");
    }

    public int whatever(int a) {
        System.out.println("C says" + a);
        return super.whatever(a + 1);
    }

    public void other() {
        super.other();
        System.out.println("Bye from C");
    }

    public Set<String> everyClass() {
        Set<String> fromParent = super.everyClass();
        fromParent.add("C");
        return fromParent;
    }

//    public void whatever(int n, Set<String> ss) {
//        System.out.printf("Hello %d from Parent1", n);
////        super

//    }
}

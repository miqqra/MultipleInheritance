package org.example.usage;

import java.util.Set;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance
//@MultipleInheritance(classes = {Parent1.class})
public class B extends BIntermediary {
    public B() {
        System.out.println("B created");
    }

    public int whatever(int a) {
        System.out.println("B (root) says " + a);
        return super.whatever(a + 10);
    }

    public void other() {
        super.other();
        System.out.println("Bye from B");
    }

    public Set<String> everyClass() {
        Set<String> fromParent = super.everyClass();
        fromParent.add("B");
        return fromParent;
    }

//    int onlyParent2() {
//        return 2;
//    }
}

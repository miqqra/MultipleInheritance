package org.example.usage;

import java.util.HashSet;
import java.util.Set;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance
public class B extends BIntermediary {
    public B() {
        System.out.println("B created");
    }

    public int whatever(int a) {
        System.out.println("B (root) says " + a);
        int fromParents = super.whatever(a + 10);
        if (fromParents == 0) {
            return 10;
        } else {
            return fromParents;
        }
    }

    public void other() {
        super.other();
        System.out.println("Bye from B");
    }

    public Set<String> everyClass() {
        Set<String> fromParent = super.everyClass();
        if (fromParent == null) {
            fromParent = new HashSet<>();
        }
        fromParent.add("B");
        return fromParent;
    }

//    int onlyParent2() {
//        return 2;
//    }
}

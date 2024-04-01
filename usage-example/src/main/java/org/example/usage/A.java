package org.example.usage;

import java.util.HashSet;
import java.util.Set;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance
public class A extends AIntermediary {
    public A() {
        System.out.println("A created");
    }

    public int whatever(int number) {
        System.out.println("A (root) says " + number);
        int fromParents = super.whatever(number + 10);
        if (fromParents == 0) {
            return 10;
        } else {
            return fromParents;
        }
    }

    public void other() {
        super.other();
        System.out.println("Bye from A");
    }

    public Set<String> everyClass() {
        Set<String> fromParent = super.everyClass();
        if (fromParent == null) {
            fromParent = new HashSet<>();
        }
        fromParent.add("A");
        return fromParent;
    }
}

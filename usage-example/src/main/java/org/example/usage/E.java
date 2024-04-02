package org.example.usage;

import java.util.Set;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {D.class})
public class E extends EIntermediary {
    public E() {
        System.out.println("E created");
    }

    public int whatever(int a) {
        System.out.println("E says " + a);
        return super.whatever(a + 1);
    }

    public void other() {
        super.other();
        System.out.println("Bye from E");
    }

    public Set<String> everyClass() {
        Set<String> fromParent = super.everyClass();
        fromParent.add("E");
        return fromParent;
    }

}

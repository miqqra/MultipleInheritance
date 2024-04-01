package org.example.usage;

import java.util.Set;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {C.class, E.class})
public class ResultClass extends ResultClassIntermediary {

    public int whatever(int number) {
        System.out.println("ResultClass says" + number);
        return super.whatever(number + 1);
    }

    public void other() {
        super.other();
        System.out.println("Bye from ResultClass");
    }

    public Set<String> everyClass() {
        Set<String> fromParent = super.everyClass();
        fromParent.add("ResultClass");
        return fromParent;
    }
}

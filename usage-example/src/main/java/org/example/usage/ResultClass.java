package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {C.class, E.class})
public class ResultClass extends ResultClassIntermediary {

    public void whatever() {
        System.out.println("Hello from ResultClass");
        super.whatever();
    }

    public void other() {
        super.other();
        System.out.println("Bye from ResultClass");
    }
}

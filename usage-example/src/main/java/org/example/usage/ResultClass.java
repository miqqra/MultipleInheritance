package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {C.class, E.class})
public class ResultClass extends ResultClassIntermediary {

    public void whatever(int number) {
        System.out.println("ResultClass says" + number);
        super.whatever(number + 1);
    }

    public void other() {
        super.other();
        System.out.println("Bye from ResultClass");
    }
}

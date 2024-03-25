package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance
//@MultipleInheritance(classes = {Parent1.class})
public class B extends BIntermediary {

    public void whatever() {
        System.out.println("Hello from B");
        super.whatever();
    }

    public void other() {
        super.other();
        System.out.println("Bye from B");
    }

//    int onlyParent2() {
//        return 2;
//    }
}

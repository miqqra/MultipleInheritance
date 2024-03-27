package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance
//@MultipleInheritance(classes = {Parent1.class})
public class B extends BIntermediary {
    public B() {
        System.out.println("B created");
    }

    public void whatever(int a) {
        System.out.println("B (root) says " + a);
        super.whatever(a + 10);
    }

    public void other() {
        super.other();
        System.out.println("Bye from B");
    }

//    int onlyParent2() {
//        return 2;
//    }
}

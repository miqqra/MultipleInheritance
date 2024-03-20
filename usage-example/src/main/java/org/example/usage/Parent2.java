package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {Parent1.class})
public class Parent2 extends Parent2Intermediary {

    public void whatever() {
        System.out.println("Hello from Parent2");
    }

    public void other() {
        System.out.println("Bye from Parent2");
    }

//    int onlyParent2() {
//        return 2;
//    }
}

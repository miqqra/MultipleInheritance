package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {Parent1.class, Parent2.class})
//public abstract class ResultClass implements CommonParent {
public class ResultClass extends ResultClassIntermediaryExpected {

    public void whatever() {
        System.out.println("Hello from ResultClass");
        //Super
    }
}

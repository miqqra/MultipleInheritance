package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritanceObject;

/**
 * Parent classes: org.example.usage.Parent1, org.example.usage.Parent2
 */
public class ResultIntermediaryExpected extends MultipleInheritanceObject {
    private final B parent2 = new B();

    private final A parent1 = new A();

    public void other() {
        if (actualObject != null) {
            var actual = actualObject;
            actualObject = null;
            try {
                actual.getClass().getMethod("callNextOther").invoke(actual);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            currentNextMethod = 0;
            callNextother();
        }
    }

    public void callNextother() {
        currentNextMethod++;
        if (currentNextMethod == 1) {
            parent2.actualObject = this;
            parent2.other();
        } else if (currentNextMethod == 2) {
            parent1.actualObject = this;
            parent1.other();
        }
    }

//    public void whatever() {
//        parent2.whatever();
//        parent1.whatever();
//        parent2.callNextwhatever();
//        parent1.callNextwhatever();
//    }
//
//    public void callNextwhatever() {
//    }
}

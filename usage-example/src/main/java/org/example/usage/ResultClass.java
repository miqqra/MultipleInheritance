package org.example.usage;

import java.lang.reflect.InvocationTargetException;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {Parent1.class, Parent2.class})
//public abstract class ResultClass implements CommonParent {
public class ResultClass extends ResultClassIntermediary {

    public void whatever()
        throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        System.out.println("Hello from ResultClass");
        super.whatever();
    }

    public void other() {
        System.out.println("Called other from resultclass");
        //Super
    }
}

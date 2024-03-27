package org.example.usage.package2;

import org.example.usage.A;
import org.example.usage.D;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@MultipleInheritance(classes = {A.class, D.class})
public class Z extends ZIntermediary {
    public Z() {
        System.out.println("Z created");
    }
}

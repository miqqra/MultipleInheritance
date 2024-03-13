package org.example.usage;

import ru.miqqra.multipleinheritance.annotations.Inherit;
import ru.miqqra.multipleinheritance.annotations.Inheritance;

@Inheritance(classes = {Parent1.class, Parent2.class})
//public abstract class ResultClass implements CommonParent {
public abstract class ResultClass extends CommonParent {

    @Inherit(from = Parent1.class)
    public abstract void whatever();

    @Inherit(from = Parent2.class)
    public abstract void other();

    @Inherit(from = Parent1.class)
    public abstract void nan();
}

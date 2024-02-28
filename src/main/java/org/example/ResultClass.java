package org.example;

import ru.miqqra.annotations.Inherit;
import ru.miqqra.annotations.Inheritance;

@Inheritance(classes = {Parent1.class, Parent2.class})
public abstract class ResultClass implements CommonParent {

    @Inherit(from = Parent1.class)
    public abstract void whatever();

    @Inherit(from = Parent2.class)
    public abstract void other();
}

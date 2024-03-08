package org.example.usage;

public class ResultClassImplExpected extends ResultClass {

    private Parent1 parent1;
    private Parent2 parent2;

    @Override
    public void whatever() {
        parent1.whatever();
    }

    @Override
    public void other() {
        parent2.other();
    }

    @Override
    public void nan() {

    }
}

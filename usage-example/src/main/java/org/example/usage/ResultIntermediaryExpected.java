package org.example.usage;

import ru.miqqra.multipleinheritance.MultipleInheritanceObject;

/**
 * Example hierarchy:
 * A     B
 * ├────┘│
 * │     D
 * C     │
 * │     E
 * └─┬───┘
 * Result
 */
@SuppressWarnings("unused")
class ResultIntermediaryExpected extends MultipleInheritanceObject { // Maybe remove extends?
    // These three fields should be defined in generated code, currently in MultipleInheritanceObject
    public static Object[] _initParents = null;
    public Object _actualObject;
    private int _currentNextMethod = 0;

    // Underscore for our service fields
    // Double underscore to avoid collisions from user-defined names
    private final B __b;
    private final D __d;
    private final E __e;
    private final A __a;
    private final C __c;

    ResultIntermediaryExpected() {
        // Reverse order of resolution table
        if (_initParents == null) {
            B._initParents = new Object[] {};
            __b = new B();
            D._initParents = new Object[] {__b};
            __d = new D();
            E._initParents = new Object[] {__b, __d};
            __e = new E();
            A._initParents = new Object[] {};
            __a = new A();
            C._initParents = new Object[] {__b, __a};
            __c = new C();
        } else {
            __b = (B) _initParents[0];
            __d = (D) _initParents[1];
            __e = (E) _initParents[2];
            __a = (A) _initParents[3];
            __c = (C) _initParents[4];
            _initParents = null;
        }
    }

    public void other() {
        if (_actualObject != null) {
            var actual = _actualObject;
            _actualObject = null;
            try {
                actual.getClass().getMethod("_callNextOther").invoke(actual);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            _currentNextMethod = 0;
            _callNextother();
        }
    }

    private void _callNextother() {
        _currentNextMethod++;
        _currentNextMethod++;
        if (_currentNextMethod == 1) {
            __c._actualObject = this;
            __c.other();
        } else if (_currentNextMethod == 2) {
            __a._actualObject = this;
            __a.other();
        } else if (_currentNextMethod == 3) {
            __e._actualObject = this;
            __e.other();
        } else if (_currentNextMethod == 4) {
            __d._actualObject = this;
            __d.other();
        } else if (_currentNextMethod == 5) {
            __b._actualObject = this;
            __b.other();
        }
    }
}

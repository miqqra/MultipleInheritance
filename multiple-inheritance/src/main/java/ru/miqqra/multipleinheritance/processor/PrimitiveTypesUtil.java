package ru.miqqra.multipleinheritance.processor;

import com.squareup.javapoet.TypeName;
import java.util.Map;

public class PrimitiveTypesUtil {

    public static char DEFAULT_CHAR;
    public static boolean DEFAULT_BOOLEAN;
    public static int DEFAULT_INT;
    public static long DEFAULT_LONG;
    public static float DEFAULT_FLOAT;
    public static double DEFAULT_DOUBLE;
    public static byte DEFAULT_BYTE;
    public static short DEFAULT_SHORT;

    public static Map<Object, Object> PRIMITIVE_TYPES_DEFAULTS = Map.of(
            TypeName.get(char.class), DEFAULT_CHAR,
            TypeName.get(boolean.class), DEFAULT_BOOLEAN,
            TypeName.get(int.class), DEFAULT_INT,
            TypeName.get(long.class), DEFAULT_LONG,
            TypeName.get(float.class), DEFAULT_FLOAT,
            TypeName.get(double.class), DEFAULT_DOUBLE,
            TypeName.get(byte.class), DEFAULT_BYTE,
            TypeName.get(short.class), DEFAULT_SHORT
    );
}

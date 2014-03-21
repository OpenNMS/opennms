package org.opennms.netmgt.config.internal.collection;

import java.lang.reflect.Array;
import java.util.Arrays;

public abstract class ArrayUtils {

    public static <T> T[] append(final T[] array, final T newObject) {
        if (array == null) {
            @SuppressWarnings("unchecked")
            final T[] ret = (T[]) Array.newInstance(newObject.getClass(), 1);
            ret[0] = newObject;
            return ret;
        } else {
            final T[] newobjs = Arrays.copyOf(array, array.length + 1);
            newobjs[array.length] = newObject;
            return newobjs;
        }
    }

}

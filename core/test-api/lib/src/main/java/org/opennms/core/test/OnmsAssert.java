package org.opennms.core.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

public abstract class OnmsAssert {

    public static void assertArrayEqualsIgnoreOrder(final Object[] a, final Object[] b) {
        final List<?> aList = Arrays.asList(a);
        final List<?> bList = Arrays.asList(b);
        Assert.assertTrue(aList.containsAll(bList) && bList.containsAll(aList));
    }

}

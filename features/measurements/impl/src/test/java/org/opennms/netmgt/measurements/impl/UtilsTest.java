package org.opennms.netmgt.measurements.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilsTest {

    @Test
    public void canEscapeColons() {
        assertEquals("\\:", Utils.escapeColons(":"));
    }
}

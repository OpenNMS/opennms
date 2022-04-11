package org.opennms.netmgt.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResourceTypeUtilsTest {

    @Test
    public void shouldGetNumPathElementsToNodeLevel() {
        shouldGetNumPathElementsToNodeLevel(4, "snmp/fs/FOREIGN_SOURCE/FOREIGN_ID/node-stats");
        shouldGetNumPathElementsToNodeLevel(4, "snmp/fs/FOREIGN_SOURCE/FOREIGN_ID");
        shouldGetNumPathElementsToNodeLevel(2, "snmp/2/node-stats");
        shouldGetNumPathElementsToNodeLevel(2, "snmp/2");
        shouldGetNumPathElementsToNodeLevel(-1, "snmp");
        shouldGetNumPathElementsToNodeLevel(-1, "");
    }
    private void shouldGetNumPathElementsToNodeLevel(int expectedLevel, String path) {
        assertEquals(expectedLevel, ResourceTypeUtils.getNumPathElementsToNodeLevel(ResourcePath.fromString(path)));
    }
}
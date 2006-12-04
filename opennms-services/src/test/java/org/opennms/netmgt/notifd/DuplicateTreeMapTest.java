package org.opennms.netmgt.notifd;

import java.util.List;

import junit.framework.TestCase;

public class DuplicateTreeMapTest extends TestCase {
    public void testPutItem() {
        DuplicateTreeMap<Long, String> m = new DuplicateTreeMap<Long, String>();
        m.putItem((long) 1, "foo");
        
        assertNotNull("value of key 1 is null", m.get((long) 1));
        assertTrue("class is not an instance of List", m.get((long) 1) instanceof List);
        List<String> list = m.get((long) 1);
        assertEquals("list size", 1, list.size());
        assertEquals("list item zero content", "foo", list.get(0));
    }
}

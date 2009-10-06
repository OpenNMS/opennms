package org.opennms.sms.phonebook;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.sms.phonebook.PropertyPhonebook;

public class PropertyPhonebookTest {

    @Test
    public void testPropertyFile() throws Exception {
        String fileName = getClass().getResource("/phonebook-test.properties").getFile();
        PropertyPhonebook p = new PropertyPhonebook();
        p.setPropertyFile(fileName);
        
        assertEquals("919-533-0160", p.getTargetForAddress("192.168.0.1"));
        assertEquals("877-666-7911", p.getTargetForAddress("192.168.0.2"));
        assertEquals("sms@example.com", p.getTargetForAddress("192.168.0.3"));
    }

    @Test
    public void testResourceFile() throws Exception {
        PropertyPhonebook p = new PropertyPhonebook();
        p.setPropertyFile("phonebook-test.properties");
        
        assertEquals("919-533-0160", p.getTargetForAddress("192.168.0.1"));
        assertEquals("877-666-7911", p.getTargetForAddress("192.168.0.2"));
        assertEquals("sms@example.com", p.getTargetForAddress("192.168.0.3"));
    }

}

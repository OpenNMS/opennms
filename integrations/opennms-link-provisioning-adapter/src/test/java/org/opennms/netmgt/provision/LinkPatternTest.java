package org.opennms.netmgt.provision;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opennms.netmgt.provision.config.linkadapter.LinkPattern;

public class LinkPatternTest {

    @Test
    public void testSimplePattern() {
        LinkPattern p = new LinkPattern("foo", "bar");
        
        assertEquals("bar", p.resolveTemplate("foo"));
        assertNull(p.resolveTemplate("monkey"));
    }
    
    @Test
    public void testTemplatePattern() {
        LinkPattern p = new LinkPattern("([a-z]{2})-([a-z]{3})([0-9]{4})-to-([a-z]{3})([0-9]{4})-dwave", "$1-$4$5-to-$2$3-dwave");
        
        assertEquals("nc-ral0002-to-ral0001-dwave", p.resolveTemplate("nc-ral0001-to-ral0002-dwave"));
        assertEquals("nc-ral0001-to-ral0002-dwave", p.resolveTemplate("nc-ral0002-to-ral0001-dwave"));
        assertNull(p.resolveTemplate("nc-fasdfasdfasdf-dwave"));
    }
}

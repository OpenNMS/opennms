package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class DistributedMapTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Distributed Map");
        waitForPageToLoad();
    }

    @Test
    public void testDistributedMap() throws Exception {
        assertEquals("Applications", selenium.getTable("css=td > table.0.2"));
        assertEquals("off", selenium.getValue("id=gwt-uid-6"));
        assertEquals("on", selenium.getValue("id=gwt-uid-1"));
    }

}

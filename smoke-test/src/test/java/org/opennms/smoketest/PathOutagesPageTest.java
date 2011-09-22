package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class PathOutagesPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Path Outages");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testPathOutagesPage() throws Exception {
        assertTrue(selenium.isTextPresent("All path outages"));
        assertTrue(selenium.isTextPresent("Critical Path IP"));
        assertTrue(selenium.isTextPresent("# of Nodes"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

}

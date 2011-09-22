package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;



public class MapPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Map");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testMapPage() throws Exception {
        assertTrue(selenium.isElementPresent("id=mainSvgDocument"));
        assertTrue(selenium.isTextPresent("Network Topology Maps"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

}

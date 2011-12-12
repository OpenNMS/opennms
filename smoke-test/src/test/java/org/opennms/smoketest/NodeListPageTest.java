package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class NodeListPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Node List");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.getHtmlSource().contains("<h3>Nodes</h3>"));
    }
    
    @Test
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("//a[@href='element/nodeList.htm?listInterfaces=true']"));
    }
    
    @Test
    public void testAllLinks() {
        selenium.click("link=Show interfaces");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("interfaces"));
    }
}

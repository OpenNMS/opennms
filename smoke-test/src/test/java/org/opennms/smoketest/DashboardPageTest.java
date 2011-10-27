package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class DashboardPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Dashboard");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testDashboardPage() throws Exception {
        assertTrue(selenium.isTextPresent("Alarms"));
        assertTrue(selenium.isTextPresent("Notifications"));
        assertTrue(selenium.isTextPresent("Node Status"));
        assertTrue(selenium.isTextPresent("Resource Graphs"));
        assertTrue(selenium.isTextPresent("24 Hour Availability"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

}

package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class ChartsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Charts");
        waitForPageToLoad();
    }

    @Test
    public void testChartsPage() throws Exception {
        assertTrue(selenium.isTextPresent("Charts"));
        assertTrue(selenium.isElementPresent("css=img[alt=sample-bar-chart]"));
        assertTrue(selenium.isElementPresent("css=img[alt=sample-bar-chart2]"));
        assertTrue(selenium.isElementPresent("css=img[alt=sample-bar-chart3]"));
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

}

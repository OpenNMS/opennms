package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;


public class SurveillancePageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        selenium.click("link=Surveillance");
        waitForPageToLoad();
    }

    @Test
    public void testSurveillancePage() throws Exception {
        long endTime = System.currentTimeMillis() + 30000;
        while(System.currentTimeMillis() < endTime){
            if(selenium.isTextPresent("Surveillance View:")){
                break;
            }
            if(endTime - System.currentTimeMillis() < 5000){
                fail ("25 second timeout trying to reach \"Surveillance\" Page");
            }
        }
        assertTrue(selenium.isTextPresent("Routers"));
        assertTrue(selenium.isTextPresent("Nodes Down"));
        assertTrue(selenium.isTextPresent("DEV"));
        selenium.click("link=Log out");
        waitForPageToLoad();
    }
}

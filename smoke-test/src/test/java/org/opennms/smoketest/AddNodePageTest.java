package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class AddNodePageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Add Node");
        selenium.waitForPageToLoad("30000");
    }
    @Test
    public void setupProvisioningGroup() throws Exception {
        selenium.open("/opennms/admin/node/add.htm");
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage Provisioning Groups");
        selenium.waitForPageToLoad("30000");
        selenium.type("css=form[name=takeAction] > input[name=groupName]", "test");
        selenium.click("css=input[type=submit]");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@value='Synchronize']");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }
    @Test
    public void testAddNodePage() throws Exception {

        assertTrue(selenium.isTextPresent("Category:"));
        assertEquals("Provision", selenium.getValue("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("css=input[type=reset]"));
        assertTrue(selenium.isTextPresent("Enable Password:"));
        assertTrue(selenium.isTextPresent("Node Quick-Add"));
        assertTrue(selenium.isTextPresent("CLI Authentication Parameters (optional)"));
        assertTrue(selenium.isTextPresent("SNMP Parameters (optional)"));
        assertTrue(selenium.isTextPresent("Surveillance Category Memberships (optional)"));
        assertTrue(selenium.isTextPresent("Basic Attributes (required)"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

}

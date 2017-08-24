/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminSnmpConfigForIpPageIT extends OpenNMSSeleniumTestCase {

    @Before
    public void setUp() throws Exception {
        gotoPage();
    }

    private void gotoPage() {
        adminPage();
        findElementByLink("Configure SNMP Community Names by IP Address").click();
    }

    @Test
    public void verifyLocationDropdown() throws IOException, InterruptedException {
        boolean created = false;
        try {
            HttpGet httpGet = new HttpGet(buildUrl("/api/v2/monitoringLocations/count"));
            ResponseData response = getRequest(httpGet);
            long locationCount = Long.parseLong(response.getResponseText());

            // Verify location drop downs
            Assert.assertEquals(new Select(findElementById("lookup_location")).getOptions().size(), locationCount);
            Assert.assertEquals(new Select(findElementById("location")).getOptions().size(), locationCount);

            // create new location
            sendPost("/api/v2/monitoringLocations", "<location location-name=\"Test\" monitoring-area=\"test\" priority=\"100\"/>", 201);
            created = true;

            // verify
            gotoPage();
            Assert.assertEquals(new Select(findElementById("lookup_location")).getOptions().size(), locationCount + 1);
            Assert.assertEquals(new Select(findElementById("location")).getOptions().size(), locationCount + 1);
        } finally {
            if (created) {
                sendDelete("/api/v2/monitoringLocations/Test", 204);
            }
        }
    }

    /**
     * Tests if getting the current snmp configuration for a specific ip address works.
     */
    @Test
    public void testGetIpInformation() throws Exception {
        // v2c
        new Select(findElementByName("version")).selectByVisibleText("v2c");
        enterText(By.name("ipAddress"), "1.1.1.1");
        findElementByName("getConfig").click();

        assertEquals("", findElementByName("ipAddress").getAttribute("value"));
        assertEquals("v2c", findElementByName("version").getAttribute("value"));
        assertEquals("1.1.1.1", findElementByName("firstIPAddress").getAttribute("value"));


        //v3
        gotoPage();
        new Select(findElementByName("version")).selectByVisibleText("v3");
        enterText(By.name("firstIPAddress"), "1.2.3.4");

        new Select(findElementByName("securityLevel")).selectByVisibleText("authNoPriv");
        new Select(findElementByName("authProtocol")).selectByVisibleText("MD5");
        new Select(findElementByName("privProtocol")).selectByVisibleText("DES");
        enterText(By.name("authPassPhrase"), "authMe!");
        enterText(By.name("privPassPhrase"), "privMe!");
        findElementByName("saveConfig").click();

        enterText(By.name("ipAddress"), "1.2.3.4");
        findElementByName("getConfig").click();

        // give the page time to rearrange the DOM before checking things
        Thread.sleep(1000);

        assertEquals("", findElementByName("ipAddress").getAttribute("value"));
        assertEquals("v3", findElementByName("version").getAttribute("value"));
        assertEquals("1.2.3.4", findElementByName("firstIPAddress").getAttribute("value"));
        assertEquals("authMe!", findElementByName("authPassPhrase").getAttribute("value"));
        assertEquals("MD5", findElementByName("authProtocol").getAttribute("value"));
        assertEquals("privMe!", findElementByName("privPassPhrase").getAttribute("value"));
        assertEquals("DES", findElementByName("privProtocol").getAttribute("value"));
        assertEquals("2", findElementByName("securityLevel").getAttribute("value")); //authNoPriv
    }

    /**
     * Tests that only one "version specifics" area is visible at the time. 
     */
    @Test
    public void testVersionHandling() {
        new Select(findElementByName("version")).selectByVisibleText("v1");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='v1/v2c specific parameters']")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//h3[text()='v3 specific parameters']")));

        new Select(findElementByName("version")).selectByVisibleText("v2c");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='v1/v2c specific parameters']")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//h3[text()='v3 specific parameters']")));

        // change to v3
        new Select(findElementByName("version")).selectByVisibleText("v3");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//h3[text()='v1/v2c specific parameters']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='v3 specific parameters']")));
    }

    /**
     * Tests if the validation of the integer fields in the "saveConfig" form works fine.
     */
    @Test
    public void testIntegerValidation() {
        final String defaultValidationErrorTemplate = "%s is not a valid %s. Please enter a number greater than 0 or leave it empty.";
        final String geZeroValidationErrorTemplate = "%s is not a valid %s. Please enter a number greater than or equal to 0, or leave it empty.";
        final String maxRequestSizeErrorTemplate = "%s is not a valid %s. Please enter a number greater or equal than 484 or leave it empty.";
        final String[] integerFields = new String[]{
                "timeout", 
                "retryCount", 
                "port", 
                "maxVarsPerPdu", 
                "maxRepetitions",
        "maxRequestSize"};
        final String[] fieldLabels = new String[]{
                "timeout", 
                "Retry Count", 
                "Port",
                "Max Vars Per Pdu",
                "Max Repetitions",
        "Max Request Size"};
        final String[] errorMessages = new String[]{
                defaultValidationErrorTemplate, 
                geZeroValidationErrorTemplate, 
                defaultValidationErrorTemplate, 
                defaultValidationErrorTemplate, 
                defaultValidationErrorTemplate,
                maxRequestSizeErrorTemplate};
        assertTrue("integerFields and fieldDescriptions must have the same length", integerFields.length == fieldLabels.length);
        assertTrue("integerFields and errorMessages must have the same length", integerFields.length == errorMessages.length);

        for (int i=0; i<integerFields.length; i++) {
            if (i>0) {
                gotoPage(); // reset page
            }
            final String fieldName = integerFields[i];
            final String fieldLabel = fieldLabels[i];
            final String errorMessageTemplate = errorMessages[i];

            // we must set first ip to a valid value, otherwise we get an "ip not set" error
            enterText(By.name("firstIPAddress"), "1.2.3.4");
            // now do the validation
            enterText(By.name(fieldName), "abc"); // no integer
            validate(errorMessageTemplate, fieldName, fieldLabel, "abc", false);
            enterText(By.name(fieldName), "-5"); // < 0
            validate(errorMessageTemplate, fieldName, fieldLabel, "-5", false);
            enterText(By.name( fieldName), "0"); // = 0
            if (i != 1) { // A retryCount of zero is legal
                validate(errorMessageTemplate, fieldName, fieldLabel, "0", false);
            }
            enterText(By.name(fieldName), "1000"); // > 0
            validate(errorMessageTemplate, fieldName, fieldLabel, "1000", true);
            // reset to default
            findElementByName(fieldName).clear();
        }

        // now test max request size individually
        final String[] input = new String[]{"483", "484", "65535", "65536"};
        final boolean[] success = new boolean[]{false, true, true, true};
        for (int i=0; i<input.length; i++) {
            gotoPage();
            enterText(By.name("firstIPAddress"), "1.2.3.4");
            enterText(By.name("maxRequestSize"), input[i]);
            validate(maxRequestSizeErrorTemplate, "maxRequestSize", "Max Request Size", input[i], success[i]);
        }
    }

    /**
     * Tests if the ip address validation in the "saveConfig" form works fine.
     * @throws Exception
     */
    @Test
    public void testIpValidation() throws Exception {
        //invalid first and empty last ip
        gotoPage();
        enterText(By.name("firstIPAddress"), "1234");
        enterText(By.name("lastIPAddress"), "");
        findElementByName("saveConfig").click();

        String alertText = handleAlert();
        assertEquals("1234 is not a valid IP address!", alertText);

        // valid first and invalid last ip
        gotoPage();
        enterText(By.name("firstIPAddress"), "1.1.1.1");
        enterText(By.name("lastIPAddress"), "abc");
        findElementByName("saveConfig").click();

        alertText = handleAlert();
        assertEquals("abc is not a valid IP address!", alertText);

        // valid first ip and empty last ip
        gotoPage();
        enterText(By.name("firstIPAddress"), "1.1.1.1");
        enterText(By.name("lastIPAddress"), "");
        findElementByName("saveConfig").click();

        alertText = handleAlert();
        assertNull(alertText);
        assertTrue(wait.until(pageContainsText("Finished configuring SNMP")));

        // valid first ip and valid last ip
        gotoPage();
        enterText(By.name("firstIPAddress"), "1.1.1.1");
        enterText(By.name("lastIPAddress"), "1.1.1.2");
        findElementByName("saveConfig").click();

        alertText = handleAlert();
        assertNull(alertText);
        assertTrue(wait.until(pageContainsText("Finished configuring SNMP")));
    }

    /**
     * Tests that the cancel button works as expected.
     */
    @Test
    public void testCancelButton() {
        findElementByName("cancelButton").click();
        // this takes you to the admin page
        findElementByXpath("//h3[text()='OpenNMS System']");
        assertTrue(m_driver.getCurrentUrl().endsWith("/admin/index.jsp"));
    }

    /**
     * Tests that one or both save options can be selected, but that there must be at least one selection.
     */
    @Test
    public void testSaveOptions() {
        // OK
        enterText(By.name("firstIPAddress"), "1.1.1.1");
        setChecked(By.id("sendEventOption"));
        setUnchecked(By.id("saveLocallyOption"));
        findElementByName("saveConfig").click();
        String alertText = handleAlert();
        assertNull(alertText);
        assertTrue(wait.until(pageContainsText("Finished configuring SNMP")));

        // OK
        gotoPage();
        enterText(By.name("firstIPAddress"), "1.1.1.1");
        setUnchecked(By.id("sendEventOption"));
        setChecked(By.id("saveLocallyOption"));
        findElementByName("saveConfig").click();
        alertText = handleAlert();
        assertNull(alertText);
        assertTrue(wait.until(pageContainsText("Finished configuring SNMP")));

        // OK 
        gotoPage();
        enterText(By.name("firstIPAddress"), "1.1.1.1");
        setChecked(By.id("sendEventOption"));
        setChecked(By.id("saveLocallyOption"));
        findElementByName("saveConfig").click();
        alertText = handleAlert();
        assertNull(alertText);
        assertTrue(wait.until(pageContainsText("Finished configuring SNMP")));

        // Error
        gotoPage();
        enterText(By.name("firstIPAddress"), "1.1.1.1");
        setUnchecked(By.id("sendEventOption"));
        setUnchecked(By.id("saveLocallyOption"));
        findElementByName("saveConfig").click();
        alertText = handleAlert();
        assertNotNull(alertText);
        assertEquals("You must select either 'Send Event' or 'Save Locally'. It is possible to select both options.", alertText);
    }

    private void validate (final String errorMessageTemplate, final String fieldName, final String fieldLabel, final String fieldValue, final Boolean success) {
        findElementByName("saveConfig").click();
        String alertText = handleAlert();
        if (success) {
            // if we expect this page to succeed, we should have no alert text, and we should find the finish text
            assertNull(alertText);
            assertTrue("Expected success on field '" + fieldLabel + "' with value " + fieldValue, wait.until(pageContainsText("Finished configuring SNMP")));
        } else {
            // if we expect a failure, check that the message matches
            assertNotNull(alertText);
            assertEquals("Expected a failure on field '" + fieldLabel + "' with value " + fieldValue, String.format(errorMessageTemplate, fieldValue, fieldLabel), alertText);
        }
    }

}

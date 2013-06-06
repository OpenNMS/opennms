/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class AdminSnmpConfigForIpPageTest extends OpenNMSSeleniumTestCase {

    @Before
    public void setUp() throws Exception {
    	super.setUp();
    	gotoPage();
    }

    private void gotoPage() {
    	selenium.click("link=Admin");
    	waitForPageToLoad();
    	selenium.click("link=Configure SNMP Community Names by IP");
    	waitForPageToLoad();
    }
    
    /**
     * Tests if getting the current snmp configuration for a specific ip address works.
     */
    @Test
    public void testGetIpInformation() {
    	// v2c
    	selenium.type("name=ipAddress",  "1.1.1.1");
    	selenium.click("name=getConfig");
    	waitForPageToLoad();
    	
    	assertEquals("", selenium.getValue("name=ipAddress"));
    	assertEquals("v2c", selenium.getValue("name=version"));
    	assertEquals("1.1.1.1", selenium.getValue("name=firstIPAddress"));
    	assertEquals("", selenium.getValue("name=lastIPAddress"));
    	assertEquals("1800", selenium.getValue("name=timeout"));
    	assertEquals("1", selenium.getValue("name=retryCount"));
    	assertEquals("161", selenium.getValue("name=port"));
    	assertEquals("65535", selenium.getValue("name=maxRequestSize"));
    	assertEquals("10", selenium.getValue("name=maxVarsPerPdu"));
    	assertEquals("2", selenium.getValue("name=maxRepetitions"));
    	assertEquals("public", selenium.getValue("name=readCommunityString"));
    	assertEquals("private", selenium.getValue("name=writeCommunityString"));
    	assertEquals("", selenium.getValue("name=securityName"));
    	assertEquals("", selenium.getValue("name=securityLevel"));
    	assertEquals("", selenium.getValue("name=authPassPhrase"));
    	assertEquals("", selenium.getValue("name=authProtocol"));
    	assertEquals("", selenium.getValue("name=privPassPhrase"));
    	assertEquals("", selenium.getValue("name=privProtocol"));
    	assertEquals("", selenium.getValue("name=engineId"));
    	assertEquals("", selenium.getValue("name=contextEngineId"));
    	assertEquals("", selenium.getValue("name=contextName"));
    	assertEquals("", selenium.getValue("name=enterpriseId"));
    	
    	
    	//v3
    	gotoPage();
    	selenium.type("name=firstIPAddress",  "1.2.3.4");
    	selenium.select("name=version", "v3");
    	selenium.click("name=saveConfig");
    	waitForPageToLoad();
    	selenium.type("name=ipAddress",  "1.2.3.4");
    	selenium.click("name=getConfig");
    	waitForPageToLoad();
    	
    	assertEquals("", selenium.getValue("name=ipAddress"));
    	assertEquals("v3", selenium.getValue("name=version"));
    	assertEquals("1.2.3.4", selenium.getValue("name=firstIPAddress"));
    	assertEquals("", selenium.getValue("name=lastIPAddress"));
    	assertEquals("1800", selenium.getValue("name=timeout"));
    	assertEquals("1", selenium.getValue("name=retryCount"));
    	assertEquals("161", selenium.getValue("name=port"));
    	assertEquals("65535", selenium.getValue("name=maxRequestSize"));
    	assertEquals("10", selenium.getValue("name=maxVarsPerPdu"));
    	assertEquals("2", selenium.getValue("name=maxRepetitions"));
    	assertEquals("", selenium.getValue("name=readCommunityString"));
    	assertEquals("", selenium.getValue("name=writeCommunityString"));
    	assertEquals("opennmsUser", selenium.getValue("name=securityName"));
    	assertEquals("1", selenium.getValue("name=securityLevel")); //authNoPriv
    	assertEquals("0p3nNMSv3", selenium.getValue("name=authPassPhrase"));
    	assertEquals("MD5", selenium.getValue("name=authProtocol"));
    	assertEquals("0p3nNMSv3", selenium.getValue("name=privPassPhrase"));
    	assertEquals("DES", selenium.getValue("name=privProtocol"));
    	assertEquals("", selenium.getValue("name=engineId"));
    	assertEquals("", selenium.getValue("name=contextEngineId"));
    	assertEquals("", selenium.getValue("name=contextName"));
    	assertEquals("", selenium.getValue("name=enterpriseId"));
    	
    }
    
    /**
     * Tests that only one "version specifics" area is visible at the time. 
     */
    @Test
    public void testVersionHandling() {
    	assertEquals("v2c", selenium.getValue("name=version"));
    	assertTrue(selenium.isTextPresent("v1/v2c specific parameters"));
    	assertFalse(selenium.isTextPresent("v3 specific parameters"));
    	
    	// change to v3
    	selenium.select("name=version", "v3");
    	assertFalse(selenium.isTextPresent("v1/v2c specific parameters"));
    	assertTrue(selenium.isTextPresent("v3 specific parameters"));
    	
    	// change to v1
    	selenium.select("name=version", "v1");
    	assertTrue(selenium.isTextPresent("v1/v2c specific parameters"));
    	assertFalse(selenium.isTextPresent("v3 specific parameters"));
    }
    
    /**
     * Tests if the validation of the integer fields in the "saveConfig" form works fine.
     * 
     */
    @Test
    public void testIntegerValidation() {
    	final String defaultValidationErrorTemplate = "%s is not a valid %s. Please enter a number greater than 0 or leave it empty.";
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
    			defaultValidationErrorTemplate, 
    			defaultValidationErrorTemplate, 
    			defaultValidationErrorTemplate, 
    			defaultValidationErrorTemplate,
    			maxRequestSizeErrorTemplate};
    	assertTrue("integerFields and fieldDescriptions must have the same length", integerFields.length == fieldLabels.length);
    	assertTrue("integerFields and errorMessages must have the same length", integerFields.length == errorMessages.length);
    	
    	for (int i=0; i<integerFields.length; i++) {
    		if (i>0) gotoPage(); // reset page
    		final String fieldName = integerFields[i];
    		final String fieldLabel = fieldLabels[i];
    		final String errorMessageTemplate = errorMessages[i];
    		
    		// we must set first ip to a valid value, otherwise we get an "ip not set" error
    		selenium.type("name=firstIPAddress", "1.2.3.4");
    		// now do the validation
    		selenium.type("name=" + fieldName, "abc"); // no integer
    		validate(errorMessageTemplate, fieldName, fieldLabel, "abc", false);
    		selenium.type("name=" + fieldName, "-5"); // < 0
    		validate(errorMessageTemplate, fieldName, fieldLabel, "-5", false);
    		selenium.type("name=" +  fieldName, "0"); // = 0
    		validate(errorMessageTemplate, fieldName, fieldLabel, "0", false);
    		selenium.type("name=" + fieldName, "1000"); // > 0
    		validate(errorMessageTemplate, fieldName, fieldLabel, "1000", true);
    		// reset to default
    		selenium.type("name=" + fieldName,  "");
    	}
    	
    	// now test max request size individually
    	final String[] input = new String[]{"483", "484", "65535", "65536"};
    	final boolean[] success = new boolean[]{false, true, true, true};
    	for (int i=0; i<input.length; i++) {
    		gotoPage();
    		selenium.type("name=firstIPAddress", "1.2.3.4");
    		selenium.type("name=maxRequestSize", input[i]);
    		validate(maxRequestSizeErrorTemplate, "maxRequestSize", "Max Request Size", input[i], success[i]);
    	}
    }
    
    private void validate (String errorMessageTemplate, String fieldName, String fieldLabel, String fieldValue, Boolean success) {
    	selenium.click("name=saveConfig");
    	waitForPageToLoad();
    	assertTrue(fieldName+": On success, there should not be any alert", success == !selenium.isAlertPresent()); 
    	// if no success, validate the error message
    	if (!success) assertEquals(String.format(errorMessageTemplate, fieldValue, fieldLabel), selenium.getAlert());
    	assertTrue(fieldName + ": On Success, there should be a 'success message'", success == selenium.isTextPresent("Finished configuring SNMP"));
    }
    
    /**
     * Tests if the ip address validation in the "saveConfig" form works fine.
     * @throws Exception
     */
    @Test
    public void testIpValidation() throws Exception {
        // empty first and last ip
        selenium.type("name=firstIPAddress", "");
        selenium.type("name=lastIPAddress", "");
        selenium.click("name=saveConfig");
        waitForPageToLoad();
        assertTrue(selenium.isAlertPresent());
        assertEquals("Please enter a valid first IP address!", selenium.getAlert());
        assertFalse(selenium.isTextPresent("Finished configuring SNMP"));
        
        //invalid first and empty last ip
        gotoPage();
        selenium.type("name=firstIPAddress", "1234");
        selenium.type("name=lastIPAddress", "");
        selenium.click("name=saveConfig");
        waitForPageToLoad();
        assertTrue(selenium.isAlertPresent());
        assertEquals("1234 is not a valid IP address!", selenium.getAlert());
        assertFalse(selenium.isTextPresent("Finished configuring SNMP"));
        
        // valid first and invalid last ip
        gotoPage();
        selenium.type("name=firstIPAddress", "1.1.1.1");
        selenium.type("name=lastIPAddress", "abc");
        selenium.click("name=saveConfig");
        waitForPageToLoad();
        assertTrue(selenium.isAlertPresent());
        assertEquals("abc is not a valid IP address!", selenium.getAlert());
        assertFalse(selenium.isTextPresent("Finished configuring SNMP"));

        // valid first ip and empty last ip
        gotoPage();
        selenium.type("name=firstIPAddress", "1.1.1.1");
        selenium.type("name=lastIPAddress", "");
        selenium.click("name=saveConfig");
        waitForPageToLoad();
        assertFalse(selenium.isAlertPresent());
        assertTrue(selenium.isTextPresent("Finished configuring SNMP"));
        
        // valid first ip and valid last ip
        gotoPage();
        selenium.type("name=firstIPAddress", "1.1.1.1");
        selenium.type("name=lastIPAddress", "1.1.1.2");
        selenium.click("name=saveConfig");
        waitForPageToLoad();
        assertFalse(selenium.isAlertPresent());
        assertTrue(selenium.isTextPresent("Finished configuring SNMP"));
    }
    
    /**
     * Tests that the cancel button works as expected.
     */
    @Test
    public void testCancelButton() {
    	selenium.click("name=cancelButton");
    	waitForPageToLoad();
    	assertTrue(selenium.isTextPresent("OpenNMS System"));
        assertTrue(selenium.isTextPresent("Operations"));
        assertTrue(selenium.isTextPresent("Nodes"));
        assertTrue(selenium.isTextPresent("Distributed Monitoring"));
        assertTrue(selenium.isTextPresent("Descriptions"));
        assertTrue(selenium.isTextPresent("Scheduled Outages: Add"));
        assertTrue(selenium.isTextPresent("Notification Status:"));
        
    	// go anywhere, but admin page
    	selenium.click("link=Configure SNMP Community Names by IP"); 
    }
    
    /**
     * Tests that one or both save options can be selected, but that there must be at least one selection.
     */
    @Test
    public void testSaveOptions() {
    	// OK 
    	selenium.type("name=firstIPAddress", "1.1.1.1");
    	selenium.check("id=sendEventOption");
    	selenium.uncheck("id=saveLocallyOption");
    	selenium.click("name=saveConfig");
    	waitForPageToLoad();
        assertFalse(selenium.isAlertPresent());
        assertTrue(selenium.isTextPresent("Finished configuring SNMP"));
        
        // OK 
        gotoPage();
        selenium.type("name=firstIPAddress", "1.1.1.1");
        selenium.uncheck("id=sendEventOption");
        selenium.check("id=saveLocallyOption");
    	selenium.click("name=saveConfig");
    	waitForPageToLoad();
        assertFalse(selenium.isAlertPresent());
        assertTrue(selenium.isTextPresent("Finished configuring SNMP"));
        
        // OK 
        gotoPage();
        selenium.type("name=firstIPAddress", "1.1.1.1");
        selenium.check("id=sendEventOption");
        selenium.check("id=saveLocallyOption");
    	selenium.click("name=saveConfig");
    	waitForPageToLoad();
        assertFalse(selenium.isAlertPresent());
        assertTrue(selenium.isTextPresent("Finished configuring SNMP"));
        
        // Error
        gotoPage();
        selenium.type("name=firstIPAddress", "1.1.1.1");
        selenium.uncheck("id=sendEventOption");
        selenium.uncheck("id=saveLocallyOption");
    	selenium.click("name=saveConfig");
    	waitForPageToLoad();
        assertTrue(selenium.isAlertPresent());
        assertEquals("You must select either 'send Event' or 'save locally'. It is possible to select both options.", selenium.getAlert());
    }
    
}
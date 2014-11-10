/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminPageTest extends OpenNMSSeleniumTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        clickAndWait("link=Admin");
    }

    @Test
    public void a_testAllTextIsPresent() throws Exception {
        waitForText("OpenNMS System");
        waitForText("Operations");
        waitForText("Nodes");
        waitForText("Distributed Monitoring");
        waitForText("Descriptions");
        waitForText("Scheduled Outages: Add");
        waitForText("Notification Status:");
    }

    @Test
    public void b_testAllLinksArePresent() throws Exception  {

        waitForElement("link=Configure Users, Groups and On-Call Roles");
        waitForElement("link=System Information");
        waitForElement("link=Instrumentation Log Reader");
        waitForElement("link=Configure Discovery");
        waitForElement("link=Configure SNMP Community Names by IP");
        waitForElement("link=Configure SNMP Data Collection per Interface");
        waitForElement("link=Manage and Unmanage Interfaces and Services");
        waitForElement("link=Manage Thresholds");
        waitForElement("link=Configure Notifications");
        waitForElement("link=Scheduled Outages");
        waitForElement("link=Add Interface for Scanning");
        waitForElement("link=Manage Provisioning Requisitions");
        waitForElement("link=Import and Export Asset Information");
        waitForElement("link=Manage Surveillance Categories");
        waitForElement("link=Delete Nodes");
        waitForElement("link=Manage Applications");
        waitForElement("link=Manage Remote Pollers");
        waitForElement("link=the OpenNMS wiki");
    }

    @Test
    public void c_testLinkGroupOne() throws Exception {
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        waitForText("Users and Groups");
        waitForText("Users");
        waitForText("Groups");
        waitForText("Roles");
        clickAndWait("link=Admin");
        clickAndWait("link=System Information");
        waitForText("OpenNMS Configuration");
        waitForText("System Configuration");
        waitForText("Reports directory:");
        clickAndWait("link=Admin");
    }

    @Test
    public void d_testLinkGroupTwo() throws Exception {
        clickAndWait("link=Configure Discovery");
        waitForText("General settings");
        waitForText("Specifics");
        waitForText("Include URLs");
        waitForText("Include Ranges");
        waitForText("Exclude Ranges");
        clickAndWait("link=Admin");
        clickAndWait("link=Configure SNMP Community Names by IP");
        waitForText("SNMP Config Lookup");
        waitForText("Updating SNMP Configuration");
        waitForText("Descriptions");
        waitForText("optimize this list");
        clickAndWait("link=Admin");
        clickAndWait("link=Configure SNMP Data Collection per Interface");
        waitForText("Manage SNMP Data Collection per Interface");
        waitForText("datacollection-config.xml file");
        clickAndWait("link=Admin");
        clickAndWait("link=Manage and Unmanage Interfaces and Services");
        waitForText("Manage and Unmanage Interfaces and Services");
        waitForText("unchecked meaning");
        waitForText("mark each service");
        clickAndWait("link=Admin");
        clickAndWait("link=Manage Thresholds");
        waitForText("Threshold Configuration");
        waitForText("Name");
        waitForText("RRD Repository");
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=Configure Notifications");
        waitForText("Configure Notifications");
        waitForText("Event Notifications");
        waitForText("Destination Paths");
        waitForText("Path Outages");
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=Scheduled Outages");
        waitForText("Affects...");
        waitForText("Notifications");
        waitForText("Data collection");
        clickAndWait("link=Admin");
    }

    @Test
    public void e_testLinkGroupThree() throws Exception {
        clickAndWait("link=Add Interface for Scanning");
        waitForText("Enter IP address");
        waitForText("Add Interface");
        waitForText("valid IP address");
        clickAndWait("link=Admin");
        clickAndWait("link=Manage Provisioning Requisitions");
        assertEquals("Add New Requisition", selenium.getValue("css=input[type=submit]"));
        assertEquals("Edit Default Foreign Source Definition", selenium.getValue("css=input[type=button]"));
        assertEquals("Reset Default Foreign Source Definition", selenium.getValue("//input[@value='Reset Default Foreign Source Definition']"));
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=Import and Export Asset Information");
        waitForText("Import and Export Assets");
        waitForText("Importing Asset Information");
        waitForText("Exporting Asset Information");
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=Manage Surveillance Categories");
        waitForText("Surveillance Categories");
        waitForText("Category");
        assertEquals("Add New Category", selenium.getValue("css=input[type=submit]"));
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=Delete Nodes");
        long endTime = System.currentTimeMillis() + 30000;
        while(System.currentTimeMillis() < endTime){
            if("Delete Nodes | Admin | OpenNMS Web Console".equals(selenium.getTitle())){
                break;
            }
            if(endTime - System.currentTimeMillis() < 5000){
                fail ("25 second timeout trying to reach \"Admin/Delete Nodes\" Page");
            }
        }
        waitForText("Delete Nodes");
        assertEquals("Delete Nodes | Admin | OpenNMS Web Console", selenium.getTitle());
        clickAndWait("link=Admin");
    }

    @Test
    public void f_testLinkGroupFour() throws Exception {
        clickAndWait("link=Manage Applications");
        waitForText("Applications");
        waitForText("Edit");
        assertEquals("Add New Application", selenium.getValue("css=input[type=submit]"));
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        /* FIXME add location monitors so this link works
        clickAndWait("link=Manage Location Monitors");
        waitForText("Distributed Poller Status");
        waitForText("Hostname");
        assertEquals("Resume All", selenium.getValue("//input[@value='Resume All']"));
        clickAndWait("link=Admin");
        */
    }

    @Test
    public void g_testLinkGroupFive() throws Exception {
        waitForElement("//a[@href='http://www.opennms.org']");
    }
}

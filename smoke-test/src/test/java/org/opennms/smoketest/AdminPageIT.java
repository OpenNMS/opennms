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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminPageIT extends OpenNMSSeleniumTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(AdminPageIT.class);

    private final String[][] m_adminPageEntries = new String[][] {
        // OpenNMS System
        new String[] { "System Configuration", "//h3[text()='OpenNMS Configuration']" },
        new String[] { "Configure Users, Groups and On-Call Roles", "//h3[text()='Users and Groups']" },

        // Provisioning
        new String[] { "Manage Provisioning Requisitions", "//h4[contains(text(), 'Requisitions (')]" },
        new String[] { "Import and Export Asset Information", "//h3[text()='Import and Export Assets']" },
        new String[] { "Manage Surveillance Categories", "//h3[text()='Surveillance Categories']" },
        new String[] { "Configure Discovery", "//h3[text()='General Settings']" },
        new String[] { "Run Single Discovery Scan", "//h3[text()='Exclude Ranges']" },
        new String[] { "Configure SNMP Community Names by IP Address", "//h3[text()='SNMP Config Lookup']" },
        new String[] { "Manually Add an Interface", "//h3[text()='Enter IP Address']" },
        new String[] { "Delete Nodes", "//h3[text()='Delete Nodes']" },

        // Flow Management
        new String[] { "Manage Flow Classification", "//div/ol/li[text()='Flow Classification']" },

        // Event Management
        new String[] { "Manually Send an Event", "//h3[text()='Send Event to OpenNMS']" },
        new String[] { "Configure Notifications", "//h3[text()='Configure Notifications']" },
        new String[] { "Customize Event Configurations", "//div[@id='content']//iframe" },

        // Service Monitoring
        new String[] { "Configure Scheduled Outages", "//form//input[@value='New Name']" },
        new String[] { "Manage and Unmanage Interfaces and Services", "//h3[text()='Manage and Unmanage Interfaces and Services']" },
        new String[] { "Manage Business Services", "//div[@id='content']//iframe" },

        // Performance Measurement
        new String[] { "Configure SNMP Collections and Data Collection Groups", "//div[@id='content']//iframe" },
        new String[] { "Configure SNMP Data Collection per Interface", "//h3[text()='Manage SNMP Data Collection per Interface']" },
        new String[] { "Configure Thresholds", "//h3[text()='Threshold Configuration']" },

        // Distributed Monitoring
        new String[] { "Manage Monitoring Locations", "//div[contains(@class,'panel')]/table//tr//a[text()='Location Name']" },
        new String[] { "Manage Applications", "//h3[text()='Applications']" },
        new String[] { "Manage Remote Pollers", "//h3[contains(text(),'Remote Poller Status')]" },
        new String[] { "Manage Minions", "//div[contains(@class,'panel')]/table//th/a[text()='Location']" },

        // Additional Tools
        new String[] { "Instrumentation Log Reader", "//h3[text()='Filtering']" },
        new String[] { "SNMP MIB Compiler", "//div[@id='content']//iframe" },
        new String[] { "Ops Board Configuration", "//div[@id='content']//iframe" },
        new String[] { "Surveillance Views Configuration", "//div[@id='content']//iframe" },
        new String[] { "JMX Configuration Generator", "//div[@id='content']//iframe" },
        new String[] { "OpenNMS Plugin Manager", "/html/body/iframe" },
        new String[] { "Data Choices", "//*[@id='datachoices-enable']" }
    };

    @Before
    public void setUp() throws Exception {
        adminPage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(9, countElementsMatchingCss("h3.panel-title"));
        findElementByXpath("//h3[text()='OpenNMS System']");
        findElementByXpath("//h3[text()='Provisioning']");
        findElementByXpath("//h3[text()='Flow Management']");
        findElementByXpath("//h3[text()='Event Management']");
        findElementByXpath("//h3[text()='Service Monitoring']");
        findElementByXpath("//h3[text()='Performance Measurement']");
        findElementByXpath("//h3[text()='Distributed Monitoring']");
        findElementByXpath("//h3[text()='Additional Tools']");
        findElementByXpath("//h3[text()='Descriptions']");
    }

    @Test
    public void testAllLinks() throws Exception {
        adminPage();
        findElementById("content");
        findElementByXpath("//div[contains(@class,'panel-body')]");
        final int count = countElementsMatchingCss("div.panel-body > ul > li > a");
        assertEquals("We expect " + m_adminPageEntries.length + " link entries on the admin page.", m_adminPageEntries.length, count);

        for (final String[] entry : m_adminPageEntries) {
            LOG.debug("clicking: '{}', expecting: '{}'", entry[0], entry[1]);
            adminPage();
            findElementByLink(entry[0]).click();
            findElementByXpath(entry[1]);
        }
    }
}

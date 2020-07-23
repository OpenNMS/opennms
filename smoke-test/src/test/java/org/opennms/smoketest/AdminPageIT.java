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
public class AdminPageIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(AdminPageIT.class);

    private final String[][] m_adminPageEntries = new String[][] {
        // OpenNMS System
        new String[] { "System Configuration", "//span[text()='OpenNMS Configuration']" },
        new String[] { "Configure Users, Groups and On-Call Roles", "//span[text()='Users and Groups']" },

        // Provisioning
        new String[] { "Manage Provisioning Requisitions", "//h4[contains(text(), 'Requisitions (')]" },
        new String[] { "Import and Export Asset Information", "//span[text()='Import and Export Assets']" },
        new String[] { "Manage Surveillance Categories", "//span[text()='Surveillance Categories']" },
        new String[] { "Configure Discovery", "//span[text()='General Settings']" },
        new String[] { "Run Single Discovery Scan", "//span[text()='Exclude Ranges']" },
        new String[] { "Configure SNMP Community Names by IP Address", "//span[text()='SNMP Config Lookup']" },
        new String[] { "Manually Add an Interface", "//span[text()='Enter IP Address']" },
        new String[] { "Delete Nodes", "//span[text()='Delete Nodes']" },
        new String[] { "Configure Geocoder Service", "//div/nav/ol/li[text()='Geocoder Configuration']" },

        // Flow Management
        new String[] { "Manage Flow Classification", "//div/nav/ol/li[text()='Flow Classification']" },

        // Event Management
        new String[] { "Manually Send an Event", "//span[text()='Send Event to OpenNMS']" },
        new String[] { "Configure Notifications", "//span[text()='Configure Notifications']" },
        new String[] { "Customize Event Configurations", "//div[@id='content']//iframe" },

        // Service Monitoring
        new String[] { "Configure Scheduled Outages", "//form//input[@value='New Name']" },
        new String[] { "Manage and Unmanage Interfaces and Services", "//span[text()='Manage and Unmanage Interfaces and Services']" },
        new String[] { "Manage Business Services", "//div[@id='content']//iframe" },

        // Performance Measurement
        new String[] { "Configure SNMP Collections and Data Collection Groups", "//div[@id='content']//iframe" },
        new String[] { "Configure SNMP Data Collection per Interface", "//span[text()='Manage SNMP Data Collection per Interface']" },
        new String[] { "Configure Thresholds", "//span[text()='Threshold Configuration']" },

        // Distributed Monitoring
        new String[] { "Manage Monitoring Locations", "//div[contains(@class,'card')]/table//tr//a[text()='Location Name']" },
        new String[] { "Manage Applications", "//span[text()='Applications']" },
        new String[] { "Manage Remote Pollers", "//span[contains(text(),'Remote Poller Status')]" },
        new String[] { "Manage Minions", "//div[contains(@class,'card')]/table//th/a[text()='Location']" },

        // Additional Tools
        new String[] { "Configure Grafana Endpoints (Reports only)", "//div/ul/li/a[contains(text(),'Grafana Endpoints')]" },
        new String[] { "Instrumentation Log Reader", "//span[text()='Filtering']" },
        new String[] { "SNMP MIB Compiler", "//div[@id='content']//iframe" },
        new String[] { "Ops Board Configuration", "//div[@id='content']//iframe" },
        new String[] { "Surveillance Views Configuration", "//div[@id='content']//iframe" },
        new String[] { "JMX Configuration Generator", "//div[@id='content']//iframe" },
        new String[] { "Data Choices", "//*[@id='datachoices-enable']" }
    };

    @Before
    public void setUp() throws Exception {
        adminPage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(10, countElementsMatchingCss("div.card-header")); // the 10th is the hidden datachoices modal
        findElementByXpath("//span[text()='OpenNMS System']");
        findElementByXpath("//span[text()='Provisioning']");
        findElementByXpath("//span[text()='Flow Management']");
        findElementByXpath("//span[text()='Event Management']");
        findElementByXpath("//span[text()='Service Monitoring']");
        findElementByXpath("//span[text()='Performance Measurement']");
        findElementByXpath("//span[text()='Distributed Monitoring']");
        findElementByXpath("//span[text()='Additional Tools']");
        findElementByXpath("//span[text()='Descriptions']");
    }

    @Test
    public void testAllLinks() throws Exception {
        adminPage();
        findElementById("content");
        findElementByXpath("//div[contains(@class,'card-body')]");
        final int count = countElementsMatchingCss("div.card-body > ul > li > a");
        assertEquals("We expect " + m_adminPageEntries.length + " link entries on the admin page.", m_adminPageEntries.length, count);

        for (final String[] entry : m_adminPageEntries) {
            LOG.debug("clicking: '{}', expecting: '{}'", entry[0], entry[1]);
            adminPage();
            findElementByLink(entry[0]).click();
            findElementByXpath(entry[1]);
        }
    }
}

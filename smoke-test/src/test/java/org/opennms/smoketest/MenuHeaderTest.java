/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuHeaderTest extends OpenNMSSeleniumTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(MenuHeaderTest.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        frontPage();
    }

    @Test
    public void testNodeLink() throws Exception {
        clickAndWait("link=Node List");
        assertTrue(selenium.isTextPresent("/ Node List") || selenium.isTextPresent("Node Interfaces"));
    }

    @Test
    public void testSearchLink() throws Exception {
        clickAndVerifyText("link=Search", "Search for Nodes");
    }

    @Test
    public void testOutagesLink() throws Exception {
        clickAndVerifyText("link=Outages", "Outage Menu");
    }

    @Test
    public void testPathOutagesLink() throws Exception {
        clickAndVerifyText("link=Path Outages", "All path outages");
    }

    @Test
    public void testDashboardLink() throws Exception {
        if (selenium.isElementPresent("//a[@href='dashboards.htm']")) {
            // new style dashboard menu
            clickAndWait("//a[@href='dashboards.htm']");
            waitForText("OpenNMS Dashboards");

            clickAndWait("//div[@id='content']//a[@href='dashboard.jsp']");
            waitForText("Surveillance View:", LOAD_TIMEOUT);

            frontPage();
            clickAndWait("//a[@href='dashboards.htm']");
            waitForText("OpenNMS Dashboards");

            clickAndWait("//div[@id='content']//a[@href='vaadin-wallboard']");
            waitForElement("//span[@class='v-button-caption' and text() = 'Wallboard']");
        } else if (selenium.isElementPresent("//a[@href='dashboard.jsp']")) {
            // old style dashboard menu
            clickAndWait("//a[@href='dashboard.jsp']");
            waitForText("Surveillance View:", LOAD_TIMEOUT);
        } else {
            fail("No dashboard link found.");
        }
    }

    @Test
    public void testEventsLink() {
        clickAndVerifyText("link=Events", "Event Queries");
    }

    @Test
    public void testAlarmsLink() {
        clickAndVerifyText("link=Alarms", "Alarm Queries");
    }

    @Test
    public void testNotificationsLink() {
        clickAndVerifyText("link=Notifications", "Notification queries");
    }

    @Test
    public void testAssetsLink() {
        clickAndVerifyText("link=Assets", "Search Asset Information");
    }

    @Test
    public void testReportsLink() {
        clickAndVerifyText("link=Reports", "Resource Graphs");
    }

    @Test
    public void testChartsLink() {
        clickAndVerifyText("link=Charts", "/ Charts");
    }

    @Test
    public void testSurveillanceLink() throws InterruptedException {
        clickAndWait("link=Surveillance");
        waitForText("Surveillance View:", LOAD_TIMEOUT);
    }

    @Test
    public void testDistributedStatusLink() {
        clickAndWait("link=Distributed Status");
        assertTrue(selenium.isTextPresent("Distributed Poller Status Summary") || selenium.isTextPresent("No applications have been defined for this system"));
    }

    private void goToMapsPage() throws Exception {
        LOG.debug("goToMapsPage()");
        frontPage();
        clickAndVerifyText("//a[@href='maps.htm']", "OpenNMS Maps");
    }

    @Test
    public void testDistributedMapLink() throws Exception {
        goToMapsPage();
        clickAndWait("//div[@id='content']//a[contains(text(), 'Distributed')]");
        Thread.sleep(1000);
        waitForHtmlSource("RemotePollerMap");
        waitForText("Last update:");
    }
    
    @Test
    public void testTopologyMapLink() throws Exception {
        // the vaadin apps are finicky
        goToMapsPage();
        clickAndWait("//div[@id='content']//a[contains(text(), 'Topology')]");
        Thread.sleep(1000);
        assertTrue(selenium.getHtmlSource().contains("vaadin"));
        assertTrue(selenium.getHtmlSource().contains("opennmstopology"));
        handleVaadinErrorButtons();
    }
    
    @Test
    public void testGeographicalMapLink() throws Exception {
        goToMapsPage();
        clickAndWait("//div[@id='content']//a[contains(text(), 'Geographical')]");
        Thread.sleep(1000);
        assertTrue(selenium.getHtmlSource().contains("vaadin"));
        assertTrue(selenium.getHtmlSource().contains("opennmsnodemaps"));
        handleVaadinErrorButtons();
    }
    
    @Test
    public void testSvgMapLink() throws Exception {
        goToMapsPage();
        clickAndWait("//div[@id='content']//a[contains(text(), 'SVG')]");
        waitForText("/ Network Topology Maps", LOAD_TIMEOUT);
    }

    @Test
    public void testAdminLink() {
        clickAndVerifyText("link=Admin", "Configure Users, Groups and On-Call Roles");
    }

    @Test
    public void testSupportLink() throws Exception {
        clickAndVerifyText("link=Support", "Enter your OpenNMS Group commercial support login");
    }

}

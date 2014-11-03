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

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MenuHeaderTest extends OpenNMSSeleniumTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(MenuHeaderTest.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        frontPage();
    }

    @Test
    public void a_testNodeLink() throws Exception {
        clickAndWait("link=Node List");
        assertTrue(selenium.isTextPresent("/ Node List") || selenium.isTextPresent("Node Interfaces"));
    }

    @Test
    public void b_testSearchLink() throws Exception {
        clickAndVerifyText("link=Search", "Search for Nodes");
    }

    @Test
    public void c_testOutagesLink() throws Exception {
        clickAndVerifyText("link=Outages", "Outage Menu");
    }

    @Test
    public void d_testPathOutagesLink() throws Exception {
        clickAndVerifyText("link=Path Outages", "All path outages");
    }

    @Test
    public void e_testDashboardLink() throws Exception {
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
            waitForElement("//span[@class='v-button-caption' and text() = 'Ops Board']");
        } else if (selenium.isElementPresent("//a[@href='dashboard.jsp']")) {
            // old style dashboard menu
            clickAndWait("//a[@href='dashboard.jsp']");
            waitForText("Surveillance View:", LOAD_TIMEOUT);
        } else {
            fail("No dashboard link found.");
        }
    }

    @Test
    public void f_testEventsLink() {
        clickAndVerifyText("link=Events", "Event Queries");
    }

    @Test
    public void g_testAlarmsLink() {
        clickAndVerifyText("link=Alarms", "Alarm Queries");
    }

    @Test
    public void h_testNotificationsLink() {
        clickAndVerifyText("link=Notifications", "Notification queries");
    }

    @Test
    public void i_testAssetsLink() {
        clickAndVerifyText("link=Assets", "Search Asset Information");
    }

    @Test
    public void j_testReportsLink() {
        clickAndVerifyText("link=Reports", "Resource Graphs");
    }

    @Test
    public void k_testChartsLink() {
        clickAndVerifyText("link=Charts", "/ Charts");
    }

    @Test
    public void l_testSurveillanceLink() throws InterruptedException {
        clickAndWait("link=Surveillance");
        waitForText("Surveillance View:", LOAD_TIMEOUT);
    }

    @Test
    public void m_testDistributedStatusLink() {
        clickAndWait("link=Distributed Status");
        assertTrue(selenium.isTextPresent("Distributed Status Summary") || selenium.isTextPresent("No applications have been defined for this system"));
    }

    private void goToMapsPage() throws Exception {
        LOG.debug("goToMapsPage()");
        frontPage();
        clickAndVerifyText("//a[@href='maps.htm']", "OpenNMS Maps");
    }

    @Test
    public void n_testDistributedMapLink() throws Exception {
        goToMapsPage();
        clickAndWait("//div[@id='content']//a[contains(text(), 'Distributed')]");
        Thread.sleep(1000);
        waitForHtmlSource("RemotePollerMap");
        waitForText("Last update:");
    }
    
    @Test
    public void o_testTopologyMapLink() throws Exception {
        // the vaadin apps are finicky
        goToMapsPage();
        clickAndWait("//div[@id='content']//a[contains(text(), 'Topology')]");
        waitForHtmlSource("vaadin", 20000, true);
        waitForHtmlSource("opennmstopology", 20000, true);
        // Make sure that the alarm browser has loaded
        waitForText("Select All", 20000, true);
        handleVaadinErrorButtons();
    }
    
    @Test
    public void p_testGeographicalMapLink() throws Exception {
        goToMapsPage();
        clickAndWait("//div[@id='content']//a[contains(text(), 'Geographical')]");
        waitForHtmlSource("vaadin", 20000, true);
        waitForHtmlSource("opennmsnodemaps", 20000, true);
        handleVaadinErrorButtons();
    }
    
    @Test
    public void q_testSvgMapLink() throws Exception {
        goToMapsPage();
        clickAndWait("//div[@id='content']//a[contains(text(), 'SVG')]");
        waitForText("/ Network Topology Maps", LOAD_TIMEOUT);
    }

    @Test
    public void r_testAdminLink() {
        clickAndVerifyText("link=Admin", "Configure Users, Groups and On-Call Roles");
    }

    @Test
    public void s_testSupportLink() throws Exception {
        clickAndVerifyText("link=Support", "Enter your OpenNMS Group commercial support login");
    }

}

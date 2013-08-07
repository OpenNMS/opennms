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

import org.junit.Test;

public class MenuHeaderTest extends OpenNMSSeleniumTestCase {

    @Test
    public void testHeaderMenuLinks() throws Exception {
        clickAndWait("link=Node List");
        clickAndVerifyText("link=Search", "Search for Nodes");
        clickAndVerifyText("link=Outages", "Outage Menu");
        clickAndVerifyText("link=Path Outages", "All path outages");
        clickAndWait("link=Dashboard");
        waitForText("Surveillance View:", LOAD_TIMEOUT);
        clickAndVerifyText("link=Events", "Event Queries");
        clickAndVerifyText("link=Alarms", "Alarm Queries");
        clickAndVerifyText("link=Notifications", "Notification queries");
        clickAndVerifyText("link=Assets", "Search Asset Information");
        clickAndVerifyText("link=Reports", "Resource Graphs");
        clickAndVerifyText("link=Charts", "/ Charts");
        clickAndWait("link=Surveillance");
        waitForText("Surveillance View:", LOAD_TIMEOUT);
        clickAndWait("link=Distributed Status");
        assertTrue(selenium.isTextPresent("Distributed Poller Status Summary") || selenium.isTextPresent("No applications have been defined for this system"));
        clickAndVerifyText("//a[@href='maps.htm']", "OpenNMS Maps");
        clickAndVerifyText("//div[@id='content']//a[contains(text(), 'Distributed')]", "clear selected tags");
        goBack();

        // the vaadin apps are finicky
        clickAndWait("//div[@id='content']//a[contains(text(), 'Topology')]");
        Thread.sleep(1000);
        assertTrue(selenium.getHtmlSource().contains("vaadin"));
        assertTrue(selenium.getHtmlSource().contains("opennmstopology"));
        handleVaadinErrorButtons();
        goBack();
        goBack();

        clickAndVerifyText("//a[@href='maps.htm']", "OpenNMS Maps");
        clickAndWait("//div[@id='content']//a[contains(text(), 'Geographical')]");
        Thread.sleep(1000);
        assertTrue(selenium.getHtmlSource().contains("vaadin"));
        assertTrue(selenium.getHtmlSource().contains("opennmsnodemaps"));
        handleVaadinErrorButtons();

        clickAndVerifyText("//a[@href='maps.htm']", "OpenNMS Maps");
        clickAndWait("//div[@id='content']//a[contains(text(), 'SVG')]");
        waitForText("/ Network Topology Maps", LOAD_TIMEOUT);

        clickAndVerifyText("link=Add Node", "Community String:");
        clickAndVerifyText("link=Admin", "Configure Users, Groups and On-Call Roles");
        clickAndVerifyText("link=Support", "Enter your OpenNMS Group commercial support login");
    }

}

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

import java.util.concurrent.TimeUnit;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.smoketest.expectations.ExpectationBuilder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MenuHeaderTest extends OpenNMSSeleniumTestCase {
    @Test
    public void testMenuEntries() throws Exception {
        new ExpectationBuilder("link=Node List").withText("/ Node List").or().withText("Node Interfaces").check(m_driver);
        new ExpectationBuilder("link=Search").withText("Search for Nodes").check(m_driver);
        new ExpectationBuilder("link=Outages").withText("Outage Menu").check(m_driver);
        new ExpectationBuilder("link=Path Outages")
            .withText("All Path Outages").and().withText("Critical Path Node").check(m_driver);
        // Dashboards below
        new ExpectationBuilder("link=Events").withText("Event Queries").check(m_driver);
        new ExpectationBuilder("link=Alarms").withText("Alarm Queries").check(m_driver);
        new ExpectationBuilder("link=Notifications").withText("Notification queries").check(m_driver);
        new ExpectationBuilder("link=Assets").withText("Search Asset Information").check(m_driver);
        new ExpectationBuilder("link=Reports")
            .withText("Resource Graphs").and().withText("Database Reports").check(m_driver);
        new ExpectationBuilder("link=Charts").withText("/ Charts").check(m_driver);
        new ExpectationBuilder("link=Surveillance")
            .waitFor(2, TimeUnit.SECONDS)
            .withText("Finding status for nodes in").or().withText("Surveillance View:").check(m_driver);
        new ExpectationBuilder("link=Distributed Status")
            .withText("Distributed Status Summary").or()
            .withText("No applications have been defined for this system").check(m_driver);
        // Maps below
        new ExpectationBuilder("link=Add Node").withText("Basic Attributes (required)").check(m_driver);
        new ExpectationBuilder("link=Admin").withText("Node Provisioning").check(m_driver);
        new ExpectationBuilder("link=Support").withText("Commercial Support").check(m_driver);

        // Dashboard Menu(s)
        final ExpectationBuilder dashboardsLink = new ExpectationBuilder("//a[@href='dashboards.htm']");
        dashboardsLink.check(m_driver);
        new ExpectationBuilder("link=Dashboard")
            .waitFor(2, TimeUnit.SECONDS).withText("Surveillance View: default").check(m_driver);
        // back to dashboard to make it happy
        dashboardsLink.check(m_driver);
        new ExpectationBuilder("link=Ops Board")
            .waitFor(2, TimeUnit.SECONDS).withText("Ops Panel").check(m_driver);
        ExpectationBuilder.frontPage().check(m_driver);

        // Map Menu(s)
        final ExpectationBuilder mapLink = new ExpectationBuilder("//a[@href='maps.htm']");
        mapLink.check(m_driver);
        new ExpectationBuilder("link=Distributed")
            .waitFor(2, TimeUnit.SECONDS).withText("Last update:").check(m_driver);
        ExpectationBuilder.frontPage().check(m_driver);
        mapLink.check(m_driver);
        new ExpectationBuilder("link=Topology")
            .waitFor(10, TimeUnit.SECONDS).withText("Select All").check(m_driver);
        mapLink.check(m_driver);
        new ExpectationBuilder("link=Geographical")
            .waitFor(5, TimeUnit.SECONDS).withText("Show Severity").check(m_driver);
        mapLink.check(m_driver);
        new ExpectationBuilder("link=SVG")
            .waitFor(2, TimeUnit.SECONDS).withText("Network Topology Maps").check(m_driver);
    }

}

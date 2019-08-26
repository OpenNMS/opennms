/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

/**
 * Verifies that the scheduled outage text is correctly displayed. See LTS-233.
 */
public class ScheduledOutageIT extends OpenNMSSeleniumIT {
    @Before
    public void beforeClass() throws Exception {
        final String node = "<node type=\"A\" label=\"TestMachine\" foreignSource=\"" + REQUISITION_NAME + "\" foreignId=\"TestMachine\">" +
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "</node>";
        sendPost("/rest/nodes", node, 201);
    }

    @After
    public void after() throws Exception {
        deleteTestRequisition();
    }

    @Test
    public void testWeekly() throws Exception {
        testOption("Weekly", "Every Sunday, From 00:00:00 Through 23:59:59");
    }

    @Test
    public void testMonthly() throws Exception {
        testOption("Monthly", "Every Sunday, From 00:00:00 Through 23:59:59");
    }

    @Test
    public void testDaily() throws Exception {
        testOption("Daily", "Daily, From 00:00:00 Through 23:59:59");
    }

    @Test
    public void testSpecific() throws Exception {
        final String dateString = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
        testOption("Specific", "One-Time, From " + dateString + " 00:00:00 Through " + dateString + " 23:59:59");
    }

    public void testOption(final String option, final String text) throws Exception {
        // Visit the scheduled outage page.
        getDriver().get(getBaseUrlInternal() + "opennms/admin/sched-outages/index.jsp");
        // Enter the name...
        enterText(By.xpath("//form[@action='admin/sched-outages/editoutage.jsp']//input[@name='newName']"), "My-Scheduled-Outage");
        // ...and hit the button.
        findElementByXpath("//form[@action='admin/sched-outages/editoutage.jsp']//input[@name='newOutage']").click();

        // Wait till the editor page appears.
        with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> pageContainsText("Editing Outage: My-Scheduled-Outage"));
        // Now add all nodes and interfaces...
        findElementByXpath("//form[@id='matchAnyForm']//input[@name='matchAny']").click();
        // ...and confirm the alert box.
        getDriver().switchTo().alert().accept();

        // Set the specified outage type...
        new Select(findElementByXpath("//select[@id='outageTypeSelector']")).selectByVisibleText(option);
        // ...and apply.
        findElementByXpath("//input[@name='setOutageType']").click();
        // now add the outage
        findElementByXpath("//input[@name='addOutage']").click();

        // check whether the outage text appears correctly...
        with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> pageContainsText(text));
    }
}

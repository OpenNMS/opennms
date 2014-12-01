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

import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.TcpEventProxy;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AlarmsPageTest extends OpenNMSSeleniumTestCase {
    @BeforeClass
    public static void createAlarm() throws Exception {

        final EventProxy eventProxy = new TcpEventProxy(new InetSocketAddress(OPENNMS_EVENT_HOST, OPENNMS_EVENT_PORT));
        final EventBuilder builder = new EventBuilder(EventConstants.IMPORT_FAILED_UEI, "AlarmsPageTest");
        builder.setParam("importResource", "foo");
        eventProxy.send(builder.getEvent());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        clickAndWait("link=Alarms");
    }

    @Test
    public void a_testAllTextIsPresent() throws InterruptedException {
        waitForText("Alarm Queries");
        waitForText("Outstanding and acknowledged alarms");
        waitForText("To view acknowledged alarms");
        waitForElement("css=input[type=submit]");
        waitForText("Alarm ID:");
    }

    @Test
    public void b_testAllLinksArePresent() throws InterruptedException { 
        waitForElement("link=All alarms (summary)");
        waitForElement("link=All alarms (detail)");
        waitForElement("link=Advanced Search");
    }

    @Test
    public void c_testAllLinks() throws InterruptedException{
        clickAndWait("link=All alarms (summary)");
        waitForText("Alarm(s) outstanding");
        waitForElement("//input[@value='Go']");
        waitForElement("css=input[type='submit']");
        clickAndWait("css=a[title='Alarms System Page']");
        clickAndWait("link=All alarms (detail)");
        waitForElement("link=First Event Time");
        waitForElement("link=Last Event Time");
        waitForElement("css=input[type='reset']");
        waitForText("Ack");
        clickAndWait("css=a[title='Alarms System Page']");
        clickAndWait("link=Advanced Search");
        waitForText("Alarm Text Contains:");
        waitForText("Advanced Alarm Search");
        selenium.open("/opennms/alarm/advsearch.jsp");
        waitForText("Advanced Alarm Search page");
        waitForElement("css=input[type='submit']");
        waitForElement("name=beforefirsteventtimemonth");
        clickAndWait("//div[@id='content']/div/h2/a[2]");
    }

    @Test
    public void d_testAlarmLink() throws Exception {
        createAlarm();
        clickAndWait("link=All alarms (summary)");

        final int sleepTime = 5000; // 5 seconds
        final long end = System.currentTimeMillis() + 300000; // 5 minutes
        while (!hasAlarmDetailLink() && (System.currentTimeMillis() < end)) {
            Thread.sleep(sleepTime);
            selenium.refresh();
            waitForPageToLoad();
        }

        assertTrue(hasAlarmDetailLink());

        waitForText("Alarm(s) outstanding");
        waitForElement("//input[@value='Go']");
        waitForElement("css=input[type='submit']");
        assertTrue(hasAlarmDetailLink());
        clickAndWait("//a[contains(@href,'alarm/detail.htm')]");
        waitForText("Severity");
        waitForText("Ticket State");
        waitForText("Acknowledgment and Severity Actions");
    }

    @Test
    public void e_testAlarmIdNotFoundPage() throws InterruptedException {
        selenium.open("/opennms/alarm/detail.htm?id=999999999");
        waitForText("Alarm ID Not Found");
    }

    private boolean hasAlarmDetailLink() {
        return selenium.isElementPresent("//a[contains(@href,'alarm/detail.htm')]");
    }
}

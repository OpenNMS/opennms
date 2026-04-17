/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.smoketest.utils.RestClient;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AlarmsPageIT extends OpenNMSSeleniumIT {
    @Before
    public void createAlarm() throws Exception {
        final EventBuilder builder = new EventBuilder(EventConstants.IMPORT_FAILED_UEI, "AlarmsPageTest");
        builder.setParam("importResource", "foo");
        final Event ev = builder.getEvent();

        final RestClient restClient = stack.opennms().getRestClient();
        restClient.sendEvent(ev);
    }

    @Before
    public void setUp() throws Exception {
        alarmsListPage();
    }

    protected void alarmsListPage() {
        driver.get(getBaseUrlInternal() + "opennms/alarm/list.htm");
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        findElementByXpath("//div[@id='content']//div[@id='advancedSearchModal']");
        findElementByXpath("//div[@id='content']//div[@id='severityLegendModal']");
        findElementByXpath("//div[@id='content']//div[@id='helpModal']");

        findElementByXpath("//div[@id='content']//a[text()='Advanced Search']");
        findElementByXpath("//div[@id='content']//a[text()='Severity Legend']");
        findElementByXpath("//div[@id='content']//a[text()='Help']");
    }

    @Test
    public void testAlarmIdNotFoundPage() throws InterruptedException {
        driver.get(getBaseUrlInternal() + "opennms/alarm/detail.htm?id=999999999");
        findElementByXpath("//h1[text()='Alarm Cleared or Not Found']");
    }

    @Test
    public void testNMS16417() throws InterruptedException {
        enterText(By.xpath("//form[@name='get_details_from_alarm_id_form']//input[@id='byalarmid_id']"), "1");
        clickElement(By.xpath("//form[@name='get_details_from_alarm_id_form']//button[@type='submit']"));
        wait.until(ExpectedConditions.urlMatches(".*id=1"));
    }
}

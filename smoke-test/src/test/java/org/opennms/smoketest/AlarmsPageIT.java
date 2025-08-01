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
        alarmsPage();
    }

    protected void alarmsPage() {
        driver.get(getBaseUrlInternal() + "opennms/alarm/index.htm");
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(3, countElementsMatchingCss("div.card-header"));
        findElementByXpath("//span[text()='Alarm Queries']");
        findElementByXpath("//span[text()='Alarm Filter Favorites']");
        findElementByXpath("//span[text()='Outstanding and acknowledged alarms']");

        findElementByXpath("//form//input[@name='id']");
        findElementByXpath("//form//button[@type='submit']");
    }

    @Test
    public void testAllLinks() throws InterruptedException{
        findElementByLink("All alarms (summary)").click();
        findElementByXpath("//a[@title='Show acknowledged alarm(s)']");
        assertElementDoesNotExist(By.cssSelector("//table//th//a[text()='First Event Time']"));

        alarmsPage();
        findElementByLink("All alarms (detail)").click();
        findElementByXpath("//a[@title='Show acknowledged alarm(s)']");
        findElementByLink("First Event Time");

        alarmsPage();
        findElementByLink("Advanced Search").click();
        findElementByName("alarmtext");
        findElementByName("iplike");
    }

    @Test
    public void testAlarmLink() throws Exception {
        findElementByLink("All alarms (summary)").click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href,'alarm/detail.htm')]")));

        findElementByXpath("//a[contains(@href,'alarm/detail.htm')]").click();
        findElementByXpath("//tr[@class]//th[text()='Severity']");
    }

    @Test
    public void testAlarmIdNotFoundPage() throws InterruptedException {
        driver.get(getBaseUrlInternal() + "opennms/alarm/detail.htm?id=999999999");
        findElementByXpath("//h1[text()='Alarm Cleared or Not Found']");
    }

    @Test
    public void testNMS16417() throws InterruptedException {
        enterText(By.xpath("//form//input[@name='id']"), "1");
        clickElement(By.xpath("//form//button[@type='submit']"));
        wait.until(ExpectedConditions.urlMatches(".*id=1"));
    }
}

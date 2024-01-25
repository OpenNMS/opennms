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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OutagePageIT extends OpenNMSSeleniumIT {
    @Before
    public void setUp() throws Exception {
        outagePage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(2, countElementsMatchingCss("div.card-header"));
        findElementByXpath("//span[text()='Outage Menu']");
        findElementByXpath("//span[text()='Outages and Service Level Availability']");
        findElementByName("outageIdForm").findElement(By.name("id"));
    }  

    @Test
    public void testAllLinks() throws InterruptedException {
        findElementByLink("Current outages").click();
        findElementByXpath("//button[contains(@class, 'active') and contains(@onclick, 'current')]");
        findElementByXpath("//button[not(contains(@class, 'active')) and contains(@onclick, 'resolved')]");
        findElementByXpath("//button[not(contains(@class, 'active')) and contains(@onclick, 'both')]");
        findElementByLink("Interface");

        outagePage();
        findElementByLink("All outages").click();
        findElementByXpath("//button[not(contains(@class, 'active')) and contains(@onclick, 'current')]");
        findElementByXpath("//button[not(contains(@class, 'active')) and contains(@onclick, 'resolved')]");
        findElementByXpath("//button[contains(@class, 'active') and contains(@onclick, 'both')]");
        findElementByLink("Interface");

        outagePage();
        findElementByName("outageIdForm").findElement(By.xpath("//button[@type='submit']")).click();
        final Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertNotNull(alert);
        assertEquals("Please enter a valid outage ID.", alert.getText());
        alert.dismiss();
    }

}

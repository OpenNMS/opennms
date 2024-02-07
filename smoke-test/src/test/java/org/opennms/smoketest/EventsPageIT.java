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

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventsPageIT extends OpenNMSSeleniumIT {
    @Before
    public void setUp() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/event/list");
    }

    @Test
    public void testLinksAndForms() throws Exception {
        findElementByName("event_search");
        findElementByName("acknowledge_form");
        findElementByLink("ID");
        findElementByLink("Severity");
        findElementByLink("Time");
        findElementByLink("Node");
        findElementByLink("Interface");
        findElementByLink("Service");
    }

    @Test 
    public void testAdvancedSearch() throws InterruptedException {
        findElementByXpath("//button[@type='button' and text() = 'Search']").click();
        findElementByName("eventtext");
        findElementByName("iplike");
        findElementByName("nodenamelike");
        findElementByName("severity-1");
        findElementByName("exactuei");
        findElementByName("usebeforetime");
    }

    @Test
    public void testNodeIdNotFoundPage() throws InterruptedException {
        driver.get(getBaseUrlInternal() + "opennms/event/detail.jsp?id=999999999");
        findElementByXpath("//h1[text()='Event ID Not Found']");
    }

    @Test
    public void testEventIdEmpty() throws InterruptedException {
        driver.get(getBaseUrlInternal() + "opennms/event/detail.jsp?id=");
        findElementByXpath("//h1[text()='Event ID Not Found']");
    }

    @Test
    public void testEventIdNotParsable() throws InterruptedException {
        driver.get(getBaseUrlInternal() + "opennms/event/detail.jsp?id=foo");
        findElementByXpath("//h1[text()='Event ID Not Found']");
    }
}

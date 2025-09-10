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

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpConfigPageIT extends OpenNMSSeleniumIT {
    private static Logger LOG = LoggerFactory.getLogger(SnmpConfigPageIT.class);

    /**
     * Checks whether Snmp config data is saved and
     * correctly retrieved in the Web UI (see NMS-13512)
     */
    @Test
    public void lookupSaveAndLookupAgain() {
        // lookup IP 1.2.3.4
        lookupIpAddress("1.2.3.4");
        // fill in details for IPs 1.2.3.4 and 1.2.3.5
        clearAndEnterTextById("lastIPAddress", "1.2.3.5");
        clearAndEnterTextById("timeout", "1801");
        clearAndEnterTextById("retryCount", "2");
        clearAndEnterTextById("port", "162");
        clearAndEnterTextById("proxyHost", "10.20.30.40");
        clearAndEnterTextById("maxRequestSize", "65534");
        clearAndEnterTextById("maxVarsPerPdu", "9");
        clearAndEnterTextById("maxRepetitions", "1");
        clearAndEnterTextById("ttl", "42");
        clearAndEnterTextById("readCommunityString", "fooReadBar");
        clearAndEnterTextById("writeCommunityString", "fooWriteBar");
        clickElementByName("saveConfig");

        // lookup 1.2.3.4
        lookupIpAddress("1.2.3.4");
        // check that all data (including proxyHost) was correctly saved
        assertEquals("1.2.3.4", scrollAndFindElementById("firstIPAddress").getAttribute("value"));
        assertEquals("1801", scrollAndFindElementById("timeout").getAttribute("value"));
        assertEquals("2", scrollAndFindElementById("retryCount").getAttribute("value"));
        assertEquals("162", scrollAndFindElementById("port").getAttribute("value"));
        assertEquals("10.20.30.40", scrollAndFindElementById("proxyHost").getAttribute("value"));
        assertEquals("65534", scrollAndFindElementById("maxRequestSize").getAttribute("value"));
        assertEquals("9", scrollAndFindElementById("maxVarsPerPdu").getAttribute("value"));
        assertEquals("1", scrollAndFindElementById("maxRepetitions").getAttribute("value"));
        assertEquals("42", scrollAndFindElementById("ttl").getAttribute("value"));
        assertEquals("fooReadBar", scrollAndFindElementById("readCommunityString").getAttribute("value"));
        assertEquals("fooWriteBar", scrollAndFindElementById("writeCommunityString").getAttribute("value"));

        // lookup 1.2.3.5
        lookupIpAddress("1.2.3.5");
        // check that all data (including proxyHost) was correctly saved
        assertEquals("1.2.3.5", scrollAndFindElementById("firstIPAddress").getAttribute("value"));
        assertEquals("1801", scrollAndFindElementById("timeout").getAttribute("value"));
        assertEquals("2", scrollAndFindElementById("retryCount").getAttribute("value"));
        assertEquals("162", scrollAndFindElementById("port").getAttribute("value"));
        assertEquals("10.20.30.40", scrollAndFindElementById("proxyHost").getAttribute("value"));
        assertEquals("65534", scrollAndFindElementById("maxRequestSize").getAttribute("value"));
        assertEquals("9", scrollAndFindElementById("maxVarsPerPdu").getAttribute("value"));
        assertEquals("1", scrollAndFindElementById("maxRepetitions").getAttribute("value"));
        assertEquals("42", scrollAndFindElementById("ttl").getAttribute("value"));
        assertEquals("fooReadBar", scrollAndFindElementById("readCommunityString").getAttribute("value"));
        assertEquals("fooWriteBar", scrollAndFindElementById("writeCommunityString").getAttribute("value"));
    }

    private void lookupIpAddress(final String ipAddress) {
        LOG.debug("lookupIpAddress: {}", ipAddress);

        gotoSnmpConfigPage();
        clearAndEnterTextById("lookup_ipAddress", ipAddress);
        clickElementByName("getConfig");
    }

    private WebElement scrollAndFindElementById(final String id) {
        return scrollToElement(By.id(id));
    }

    private void clearAndEnterTextById(final String id, final String input) {
        WebElement elem = scrollAndFindElementById(id);
        elem.clear();
        elem.sendKeys(input);
    }

    private void clickElementByName(final String name) {
        WebElement elem = scrollToElement(By.name(name));
        clickElementUsingScript(elem);
    }

    private void gotoSnmpConfigPage() {
        LOG.debug("gotoSnmpConfigPage");

        adminPage();
        WebElement elem = findElementByLink("Configure SNMP Community Names by IP Address");
        clickElementUsingScript(elem);
    }
}

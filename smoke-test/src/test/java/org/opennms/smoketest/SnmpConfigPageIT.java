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

import org.junit.Test;
import org.openqa.selenium.By;

public class SnmpConfigPageIT extends OpenNMSSeleniumIT {

    /**
     * Checks whether Snmp config data is saved and
     * correctly retrieved in the Web UI (see NMS-13512)
     */
    @Test
    public void lookupSaveAndLookupAgain() {
        // lookup IP 1.2.3.4
        lookupIpAddress("1.2.3.4");
        // fill in details for IPs 1.2.3.4 and 1.2.3.5
        enterText(By.id("lastIPAddress"), "1.2.3.5");
        enterText(By.id("timeout"), "1801");
        enterText(By.id("retryCount"), "2");
        enterText(By.id("port"), "162");
        enterText(By.id("proxyHost"), "10.20.30.40");
        enterText(By.id("maxRequestSize"), "65534");
        enterText(By.id("maxVarsPerPdu"), "9");
        enterText(By.id("maxRepetitions"), "1");
        enterText(By.id("ttl"), "42");
        enterText(By.id("readCommunityString"), "fooReadBar");
        enterText(By.id("writeCommunityString"), "fooWriteBar");
        findElementByName("saveConfig").click();

        // lookup 1.2.3.4
        lookupIpAddress("1.2.3.4");
        // check that all data (including proxyHost) was correctly saved
        assertEquals("1.2.3.4", findElementById("firstIPAddress").getAttribute("value"));
        assertEquals("1801", findElementById("timeout").getAttribute("value"));
        assertEquals("2", findElementById("retryCount").getAttribute("value"));
        assertEquals("162", findElementById("port").getAttribute("value"));
        assertEquals("10.20.30.40", findElementById("proxyHost").getAttribute("value"));
        assertEquals("65534", findElementById("maxRequestSize").getAttribute("value"));
        assertEquals("9", findElementById("maxVarsPerPdu").getAttribute("value"));
        assertEquals("1", findElementById("maxRepetitions").getAttribute("value"));
        assertEquals("42", findElementById("ttl").getAttribute("value"));
        assertEquals("fooReadBar", findElementById("readCommunityString").getAttribute("value"));
        assertEquals("fooWriteBar", findElementById("writeCommunityString").getAttribute("value"));

        // lookup 1.2.3.5
        lookupIpAddress("1.2.3.5");
        // check that all data (including proxyHost) was correctly saved
        assertEquals("1.2.3.5", findElementById("firstIPAddress").getAttribute("value"));
        assertEquals("1801", findElementById("timeout").getAttribute("value"));
        assertEquals("2", findElementById("retryCount").getAttribute("value"));
        assertEquals("162", findElementById("port").getAttribute("value"));
        assertEquals("10.20.30.40", findElementById("proxyHost").getAttribute("value"));
        assertEquals("65534", findElementById("maxRequestSize").getAttribute("value"));
        assertEquals("9", findElementById("maxVarsPerPdu").getAttribute("value"));
        assertEquals("1", findElementById("maxRepetitions").getAttribute("value"));
        assertEquals("42", findElementById("ttl").getAttribute("value"));
        assertEquals("fooReadBar", findElementById("readCommunityString").getAttribute("value"));
        assertEquals("fooWriteBar", findElementById("writeCommunityString").getAttribute("value"));
    }

    private void lookupIpAddress(final String ipAddress) {
        gotoSnmpConfigPage();
        enterText(By.id("lookup_ipAddress"), ipAddress);
        findElementByName("getConfig").click();
    }

    private void gotoSnmpConfigPage() {
        adminPage();
        findElementByLink("Configure SNMP Community Names by IP Address").click();
    }
}

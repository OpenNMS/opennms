/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

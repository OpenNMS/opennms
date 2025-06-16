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

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Test Class for the Admin Page to configure data collection per SNMP interface.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AdminDataCollectionSnmpInterfacesIT extends OpenNMSSeleniumIT {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AdminDataCollectionSnmpInterfacesIT.class);

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        // Creating a node

        LOG.debug("creating node");
        String node = "<node type=\"A\" label=\"TestMachine1\" foreignSource=\"SmokeTests\" foreignId=\"TestMachine1\">" +
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine1</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "<createTime>2011-09-24T07:12:46.421-04:00</createTime>" +
                "<lastCapsdPoll>2011-09-24T07:12:46.421-04:00</lastCapsdPoll>" +
                "</node>";
        sendPost("rest/nodes", node, 201);
        LOG.debug("node created!");

        // Adding IP Interface

        LOG.debug("creating ip interface");
        String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "<hostName>test-machine1.local</hostName>" +
                "</ipInterface>";
        sendPost("rest/nodes/SmokeTests:TestMachine1/ipinterfaces", ipInterface, 201);
        LOG.debug("interface created!");

        // Adding SNMP Interfaces

        for (int i = 1; i < 5; i ++) {
            LOG.debug("creating snmp interface " + i);
            String snmpInterface = "<snmpInterface collectFlag=\"" + (i < 2 ? "C" : "N") + "\" ifIndex=\"" + i + "\" pollFlag=\"N\">" +
                    "<ifAdminStatus>1</ifAdminStatus>" +
                    "<ifDescr>eth" + i + "</ifDescr>" +
                    "<ifName>eth" + i + "</ifName>" +
                    "<ifAlias>LAN Access " + i + "</ifAlias>" +
                    "<ifOperStatus>1</ifOperStatus>" +
                    "<ifSpeed>10000000</ifSpeed>" +
                    "<ifType>6</ifType>" +
                    "<physAddr>74d02b3f267" + i + "</physAddr>" +
                    "</snmpInterface>";

            sendPost("rest/nodes/SmokeTests:TestMachine1/snmpinterfaces", snmpInterface, 201);
            LOG.debug("interface created!");
        }

        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=SmokeTests:TestMachine1");
    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        sendDelete("rest/nodes/SmokeTests:TestMachine1", 202);
    }

    /**
     * Can render page.
     *
     * @throws Exception the exception
     */
    @Test
    public void canRenderPage() throws Exception {
        findElementByLink("Admin").click();
        findElementByLink("Configure SNMP Data Collection per Interface").click();
        // 5 columns: ifIndex, ifType, ifDescr, ifName, ifAlias
        List<WebElement> interfaces = driver.findElements(By.cssSelector("onms-interfaces-config td.ng-binding"));
        Assert.assertNotNull(interfaces);
        Assert.assertEquals(20, interfaces.size());
        // 1 column: collect
        List<WebElement> collect = driver.findElements(By.cssSelector("onms-interfaces-config td input"));
        Assert.assertNotNull(collect);
        Assert.assertEquals(4, collect.size());
        // 1st Row: collection enabled
        Assert.assertEquals("1", interfaces.get(0).getText());
        Assert.assertEquals("eth1", interfaces.get(3).getText());
        Assert.assertTrue(collect.get(0).isSelected());
        // 4th Row: collection disabled
        Assert.assertEquals("4", interfaces.get(15).getText());
        Assert.assertEquals("eth4", interfaces.get(18).getText());
        Assert.assertFalse(collect.get(3).isSelected());
    }

}

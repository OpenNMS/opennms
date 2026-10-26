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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.smoketest.ui.NodeDetailsPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class InterfacesOnNodePageIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InterfacesOnNodePageIT extends OpenNMSSeleniumIT {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(InterfacesOnNodePageIT.class);

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

        for (int i = 1; i < 3; i ++) {
            LOG.debug("creating snmp interface " + i);
            String snmpInterface = "<snmpInterface collectFlag=\"C\" ifIndex=\"" + i + "\" pollFlag=\"N\">" +
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

        new NodeDetailsPage(this).open("SmokeTests:TestMachine1");
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
     * Test node page.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNodePage() throws Exception {
        final NodeDetailsPage nodePage = new NodeDetailsPage(this);

        // Verify main banner
        nodePage.waitForLabel("TestMachine1");

        // Verify IP Interfaces: IP Address, IP Host Name, ...
        List<String> cells = nodePage.getFirstRowCells("ip-interfaces-table");
        Assert.assertEquals("10.10.10.10", cells.get(0));
        Assert.assertEquals("test-machine1.local", cells.get(1));

        // Click on SNMP Interfaces Tab
        nodePage.showSnmpInterfaces();

        // Verify SNMP Interfaces: Index, Name, Status, Alias, Speed, Descr, ...
        cells = nodePage.getFirstRowCells("snmp-interfaces-table");
        Assert.assertEquals("1", cells.get(0));
        Assert.assertEquals("eth1", cells.get(1));
        Assert.assertEquals("LAN Access 1", cells.get(3));
        Assert.assertEquals("eth1", cells.get(5));
    }

}

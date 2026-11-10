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
package org.opennms.netmgt.config.snmpinterfacepoller;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SnmpInterfacePollerConfigurationTest extends XmlTestNoCastor<SnmpInterfacePollerConfiguration> {

    public SnmpInterfacePollerConfigurationTest(SnmpInterfacePollerConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/snmp-interface-poller-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<snmp-interface-poller-configuration threads=\"30\" service=\"SNMP\" up-values=\"1\" down-values=\"2\" >\n" +
                "  <node-outage>\n" + 
                "    <critical-service name=\"ICMP\" />\n" + 
                "    <critical-service name=\"SNMP\" />\n" + 
                "  </node-outage>\n" + 
                "  <package name=\"example1\">\n" + 
                "    <filter>IPADDR != '0.0.0.0'</filter>\n" + 
                "    <include-range begin=\"1.1.1.1\" end=\"1.1.1.1\" />\n" + 
                "    <interface name=\"Ethernet\" criteria=\"snmpiftype = 6\" interval=\"300000\" user-defined=\"false\" status=\"on\" max-vars-per-pdu=\"10\" max-interface-per-pdu=\"0\"/>\n" +
                "  </package>\n" +
                "</snmp-interface-poller-configuration>"
            }
        });
    }

    private static SnmpInterfacePollerConfiguration getConfig() {
        SnmpInterfacePollerConfiguration config = new SnmpInterfacePollerConfiguration();
        config.setThreads(30);
        config.setService("SNMP");

        NodeOutage nodeOutage = new NodeOutage();
        config.setNodeOutage(nodeOutage);

        CriticalService icmpCrit = new CriticalService();
        icmpCrit.setName("ICMP");
        nodeOutage.addCriticalService(icmpCrit);

        CriticalService snmpCrit = new CriticalService();
        snmpCrit.setName("SNMP");
        nodeOutage.addCriticalService(snmpCrit);

        Package pkg = new Package();
        pkg.setName("example1");
        config.addPackage(pkg);

        Filter filter = new Filter();
        filter.setContent("IPADDR != '0.0.0.0'");
        pkg.setFilter(filter);

        IncludeRange includeRange = new IncludeRange();
        includeRange.setBegin("1.1.1.1");
        includeRange.setEnd("1.1.1.1");
        pkg.addIncludeRange(includeRange);

        Interface inf = new Interface();
        inf.setName("Ethernet");
        inf.setCriteria("snmpiftype = 6");
        inf.setInterval(300000L);
        inf.setUserDefined(false);
        inf.setStatus("on");
        pkg.addInterface(inf);
        return config;
    }
}


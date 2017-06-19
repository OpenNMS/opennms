/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
                "<snmp-interface-poller-configuration threads=\"30\" service=\"SNMP\">\n" + 
                "  <node-outage>\n" + 
                "    <critical-service name=\"ICMP\" />\n" + 
                "    <critical-service name=\"SNMP\" />\n" + 
                "  </node-outage>\n" + 
                "  <package name=\"example1\">\n" + 
                "    <filter>IPADDR != '0.0.0.0'</filter>\n" + 
                "    <include-range begin=\"1.1.1.1\" end=\"1.1.1.1\" />\n" + 
                "    <interface name=\"Ethernet\" criteria=\"snmpiftype = 6\" interval=\"300000\" user-defined=\"false\" status=\"on\"/>\n" + 
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


/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.collectd;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class CollectdConfigurationTest extends XmlTestNoCastor<CollectdConfiguration> {

    public CollectdConfigurationTest(final CollectdConfiguration sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Override
    protected boolean ignoreNamespace(final String namespace) {
        return "http://xmlns.opennms.org/xsd/config/collectd".equals(namespace);
    }
    
    protected String getSchemaFile() {
        return "target/classes/xsds/collectd-configuration.xsd";
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getBasicCollectdConfiguration(),
                    "<?xml version=\"1.0\"?>\n" + 
                            "<?castor class-name=\"org.opennms.netmgt.collectd.CollectdConfiguration\"?>\n" + 
                            "<collectd-configuration threads=\"50\">\n" + 
                            "        <package name=\"example1\" remote=\"false\">\n" +
                            "                <filter>IPADDR != '0.0.0.0'</filter>\n" + 
                            "                <include-range begin=\"1.1.1.1\" end=\"254.254.254.254\" />\n" + 
                            "                <service name=\"SNMP\" interval=\"30000\" user-defined=\"false\" status=\"on\">\n" + 
                            "                        <parameter key=\"collection\" value=\"default\" />\n" + 
                            "                        <parameter key=\"thresholding-enabled\" value=\"true\" />\n" + 
                            "                </service>\n" + 
                            "                <service name=\"OpenNMS-JVM\" interval=\"30000\" user-defined=\"false\" status=\"on\">\n" + 
                            "                        <parameter key=\"port\" value=\"18980\" />\n" + 
                            "                        <parameter key=\"retry\" value=\"2\" />\n" + 
                            "                        <parameter key=\"timeout\" value=\"3000\" />\n" + 
                            "                        <parameter key=\"protocol\" value=\"rmi\" />\n" + 
                            "                        <parameter key=\"urlPath\" value=\"/jmxrmi\" />\n" + 
                            "                        <parameter key=\"rrd-base-name\" value=\"java\" />\n" + 
                            "                        <parameter key=\"ds-name\" value=\"opennms-jvm\" />\n" + 
                            "                        <parameter key=\"friendly-name\" value=\"opennms-jvm\" />\n" + 
                            "                        <parameter key=\"collection\" value=\"jsr160\" />\n" + 
                            "                        <parameter key=\"thresholding-enabled\" value=\"true\" />\n" + 
                            "                </service>\n" + 
                            "        </package>\n" +
                            "        <package name=\"example2\" remote=\"true\">\n" +
                            "                <filter>IPADDR != '0.0.0.0'</filter>\n" +
                            "                <service name=\"dummy\" interval=\"30000\" user-defined=\"true\" status=\"off\"/>\n" +
                            "        </package>\n" +
                            "        <collector service=\"SNMP\"\n" + 
                            "                class-name=\"org.opennms.netmgt.collectd.SnmpCollector\" />\n" + 
                            "        <collector service=\"OpenNMS-JVM\"\n" + 
                            "                class-name=\"org.opennms.netmgt.collectd.Jsr160Collector\" />\n" + 
                            "</collectd-configuration>"
                }
        });
    }

    private static CollectdConfiguration getBasicCollectdConfiguration() {
        final CollectdConfiguration config = new CollectdConfiguration();

        config.setThreads(50);

        final Package p = new Package();
        p.setName("example1");

        p.setFilter(new Filter("IPADDR != '0.0.0.0'"));
        p.addIncludeRange(new IncludeRange("1.1.1.1", "254.254.254.254"));

        final Service snmp = new Service();
        snmp.setName("SNMP");
        snmp.setInterval(30000l);
        snmp.setUserDefined("false");
        snmp.setStatus("on");
        snmp.addParameter(new Parameter("collection", "default"));
        snmp.addParameter(new Parameter("thresholding-enabled", "true"));
        p.addService(snmp);
        
        final Service jvm = new Service();
        jvm.setName("OpenNMS-JVM");
        jvm.setInterval(30000l);
        jvm.setUserDefined("false");
        jvm.setStatus("on");
        jvm.addParameter("port", "18980");
        jvm.addParameter("retry", "2");
        jvm.addParameter("timeout", "3000");
        jvm.addParameter("protocol", "rmi");
        jvm.addParameter("urlPath", "/jmxrmi");
        jvm.addParameter("rrd-base-name", "java");
        jvm.addParameter("ds-name", "opennms-jvm");
        jvm.addParameter("friendly-name", "opennms-jvm");
        jvm.addParameter("collection", "jsr160");
        jvm.addParameter("thresholding-enabled", "true");
        p.addService(jvm);

        final Service dummyService = new Service();
        dummyService.setName("dummy");
        dummyService.setStatus("off");
        dummyService.setUserDefined("true");
        dummyService.setInterval(30000L);

        final Package p2 = new Package();
        p2.setName("example2");
        p2.setRemote(true);
        p2.setFilter(p.getFilter());
        p2.addService(dummyService);

        config.addPackage(p);
        config.addPackage(p2);

        config.addCollector("SNMP", "org.opennms.netmgt.collectd.SnmpCollector");
        config.addCollector("OpenNMS-JVM", "org.opennms.netmgt.collectd.Jsr160Collector");

        return config;
    }
}

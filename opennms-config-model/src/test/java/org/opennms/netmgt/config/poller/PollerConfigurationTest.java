/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.poller;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.core.xml.JaxbUtils;

public class PollerConfigurationTest extends XmlTest<PollerConfiguration> {

	public PollerConfigurationTest(final PollerConfiguration sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		final NodeOutage no = new NodeOutage();
		final CriticalService cs = new CriticalService();
		cs.setName("ICMP");
		no.setCriticalService(cs);
		no.setStatus("on");
		no.setPollAllIfNoCriticalServiceDefined("true");

		final Parameter parameter = new Parameter();
		parameter.setKey("oid");
		parameter.setValue(".1.3.6.1.2.1.1.2.0");
		final Service service = new Service();
		service.setName("SNMP");
		service.setInterval(300000);
		service.setUserDefined("false");
		service.setStatus("on");
		service.addParameter(parameter);
		final Downtime downtime = new Downtime();
		downtime.setBegin(0);
		downtime.setEnd(300000);
		downtime.setInterval(30000);
		final Filter filter = new Filter();
		filter.setContent("IPADDR != '0.0.0.0'");
		final Rrd rrd = new Rrd();
		rrd.setStep(300);
		rrd.addRra("RRA:AVERAGE:0.5:1:2016");
		rrd.addRra("RRA:AVERAGE:0.5:12:1488");
		final Package pkg = new Package();
		pkg.setName("example");
		pkg.setRemote(false);
		pkg.addService(service);
		pkg.addDowntime(downtime);
		pkg.setFilter(filter);
		pkg.setRrd(rrd);

		final Monitor monitor = new Monitor();
		monitor.setService("SNMP");
		monitor.setClassName("org.opennms.netmgt.poller.monitors.SnmpMonitor");

		final PollerConfiguration cfg = new PollerConfiguration();
		cfg.setThreads(30);
		cfg.setPathOutageEnabled("false");
		cfg.setServiceUnresponsiveEnabled("false");
		cfg.setXmlrpc("false");
		cfg.setNextOutageId("SELECT nextval('outageNxtId')");
		cfg.setNodeOutage(no);
		cfg.addPackage(pkg);
		cfg.addMonitor(monitor);

        return Arrays.asList(new Object[][] {
            {
            	cfg,
                "<poller-configuration threads='30' nextOutageId=\"SELECT nextval('outageNxtId')\"\n" +
                "  serviceUnresponsiveEnabled='false' xmlrpc='false' pathOutageEnabled='false'>\n" +
                "  <node-outage status='on' pollAllIfNoCriticalServiceDefined='true'>\n" +
                "    <critical-service name='ICMP'/>\n" +
                "  </node-outage>\n" +
            	"  <package name='example' remote='false'>\n" +
                "    <filter>IPADDR != '0.0.0.0'</filter>\n" +
                "    <rrd step='300'>\n" +
                "      <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" +
                "      <rra>RRA:AVERAGE:0.5:12:1488</rra>\n" +
                "    </rrd>\n" +
                "    <service name='SNMP' interval='300000' user-defined='false' status='on'>\n" +
                "      <parameter key='oid' value='.1.3.6.1.2.1.1.2.0'/>\n" +
                "    </service>\n" +
                "    <downtime begin='0' end='300000' interval='30000'/>\n" +
                "  </package>\n" +
                "  <monitor service='SNMP' class-name='org.opennms.netmgt.poller.monitors.SnmpMonitor'/>\n" +
                "</poller-configuration>",
                "target/classes/xsds/poller-configuration.xsd"
            }
        });
    }

	@Test
	public void testLoadConfig() {
		String xml =
				"<poller-configuration threads='10' nextOutageId=\"SELECT nextval('outageNxtId')\"\n" +
		        "  serviceUnresponsiveEnabled='false' xmlrpc='false' pathOutageEnabled='false'>\n" +
				"  <node-outage status='on' pollAllIfNoCriticalServiceDefined='true'>\n" +
		        "    <critical-service name='ICMP'/>\n" +
				"  </node-outage>\n" +
				"  <package name='default' remote='false'>\n" +
				"    <filter>IPADDR IPLIKE *.*.*.*</filter>\n" +
				"    <rrd step='300'>\n" +
				"        <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" +
				"        <rra>RRA:AVERAGE:0.5:12:4464</rra>\n" +
				"        <rra>RRA:MIN:0.5:12:4464</rra>\n" +
				"        <rra>RRA:MAX:0.5:12:4464</rra>\n" +
				"    </rrd>\n" +
				"    <service name='ICMP' interval='300000' user-defined='false' status='on'>\n" +
				"      <parameter key='test-key' value='test-value'/>\n" +
 				"      <parameter key='owner'><person firstName='alejandro' lastName='galue'/></parameter>\n" +
				"    </service>\n" +
				"    <downtime begin='0' end='30000' interval='30000'/>\n" +
				"</package>\n" +
				"<monitor service='ICMP' class-name='org.opennms.netmgt.mock.MockMonitor'/>\n" +
				"</poller-configuration>";
		PollerConfiguration cfg = JaxbUtils.unmarshal(PollerConfiguration.class, xml);
		Assert.assertNotNull(cfg);
		Assert.assertEquals("default", cfg.getPackageCollection().get(0).getName());
	}

    @Test
    public void testPollerConfigFactorySampleData() throws Exception {
        final String pollerConfig = "\n" +
                "<poller-configuration\n" +
                "   threads=\"10\"\n" +
                "   nextOutageId=\"SELECT nextval(\'outageNxtId\')\"\n" +
                "   serviceUnresponsiveEnabled=\"false\">\n" +
                "   <node-outage status=\"on\" pollAllIfNoCriticalServiceDefined=\"true\"></node-outage>\n" +
                "   <package name=\"default\">\n" +
                "       <filter>IPADDR IPLIKE *.*.*.*</filter>\n" +
                "       <rrd step = \"300\">\n" + 
                "           <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" + 
                "           <rra>RRA:AVERAGE:0.5:12:4464</rra>\n" + 
                "           <rra>RRA:MIN:0.5:12:4464</rra>\n" + 
                "           <rra>RRA:MAX:0.5:12:4464</rra>\n" + 
                "       </rrd>\n" +
                "       <service name=\"ICMP\" interval=\"300000\">\n" +
                "         <parameter key=\"test-key\" value=\"test-value\"/>\n" +
                "         <parameter key=\"owner\">" +
                "            <person firstName='alejandro' lastName='galue'/>" +
                "         </parameter>" +
                "       </service>\n" +
                "       <downtime begin=\"0\" end=\"30000\"/>\n" + 
                "   </package>\n" +
                "   <monitor service=\"ICMP\" class-name=\"org.opennms.netmgt.mock.MockMonitor\"/>\n"+
                "</poller-configuration>\n";
		PollerConfiguration cfg = JaxbUtils.unmarshal(PollerConfiguration.class, pollerConfig);
		Assert.assertNotNull(cfg);
		Assert.assertEquals("default", cfg.getPackageCollection().get(0).getName());
    }


}

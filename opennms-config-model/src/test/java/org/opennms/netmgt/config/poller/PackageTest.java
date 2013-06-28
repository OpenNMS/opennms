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

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;

public class PackageTest extends XmlTest<Package> {

	public PackageTest(final Package sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
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
		final IncludeRange incRange = new IncludeRange();
		incRange.setBegin("10.0.0.1");
		incRange.setEnd("10.255.255.254");
		final ExcludeRange excRange = new ExcludeRange();
		excRange.setBegin("10.10.0.1");
		excRange.setEnd("10.10.255.254");
		final Package pkg = new Package();
		pkg.setName("example");
		pkg.setRemote(false);
		pkg.addService(service);
		pkg.addDowntime(downtime);
		pkg.setFilter(filter);
		pkg.addIncludeRange(incRange);
		pkg.addExcludeRange(excRange);
		pkg.setRrd(rrd);
		pkg.addIncludeUrl("file:///home/monitoring/include.txt");
		pkg.addSpecific("10.10.10.10");

        return Arrays.asList(new Object[][] {
            {
            	pkg,
            	"<package name='example' remote='false'>\n" +
                "  <filter>IPADDR != '0.0.0.0'</filter>\n" +
                "  <specific>10.10.10.10</specific>\n" +
                "  <include-range begin='10.0.0.1' end='10.255.255.254'/>\n" +
                "  <exclude-range begin='10.10.0.1' end='10.10.255.254'/>\n" +
                "  <include-url>file:///home/monitoring/include.txt</include-url>\n" +
                "  <rrd step='300'>\n" +
                "    <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" +
                "    <rra>RRA:AVERAGE:0.5:12:1488</rra>\n" +
                "  </rrd>\n" +
                "  <service name='SNMP' interval='300000' user-defined='false' status='on'>\n" +
                "    <parameter key='oid' value='.1.3.6.1.2.1.1.2.0'/>\n" +
                "  </service>\n" +
                "  <downtime begin='0' end='300000' interval='30000'/>\n" +
            	"</package>",
                "target/classes/xsds/poller-configuration.xsd"
            }
        });
    }

}

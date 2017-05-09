/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.wmi;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.collection.api.AttributeType;

public class WmiDatacollectionConfigTest extends XmlTestNoCastor<WmiDatacollectionConfig> {

    public WmiDatacollectionConfigTest(final WmiDatacollectionConfig sampleObject,
            final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        WmiDatacollectionConfig config = new WmiDatacollectionConfig();
        config.setRrdRepository("/opt/opennms/rrd/snmp/");

        Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.getRra().add("RRA:AVERAGE:0.5:1:2016");

        WmiCollection wmiCollection = new WmiCollection();
        wmiCollection.setName("not-default");
        wmiCollection.setRrd(rrd);

        Wpm wpm = new Wpm();
        wpm.setName("wmiOSMemory");
        wpm.setWmiClass("Win32_PerfFormattedData_PerfOS_Memory");
        wpm.setWmiNamespace("root/cimv2");
        wpm.setKeyvalue("Name");
        wpm.setRecheckInterval(3600000);
        wpm.setIfType("all");
        wpm.setResourceType("node");
        wmiCollection.addWpm(wpm);

        Attrib attrib = new Attrib();
        attrib.setName("AvailableBytes");
        attrib.setAlias("wmiOSMemAvailBytes");
        attrib.setWmiObject("AvailableBytes");
        attrib.setType(AttributeType.GAUGE);
        wpm.getAttribs().add(attrib);

        config.getWmiCollections().add(wmiCollection);

        return Arrays.asList(new Object[][] { {
            config,
            "<wmi-datacollection-config rrdRepository=\"/opt/opennms/rrd/snmp/\">" +
                 "<wmi-collection name=\"not-default\">" +
                     "<rrd step=\"300\">" +
                         "<rra>RRA:AVERAGE:0.5:1:2016</rra>" +
                     "</rrd>" +
                     "<wpms>" +
                         "<wpm name=\"wmiOSMemory\" wmiClass=\"Win32_PerfFormattedData_PerfOS_Memory\" wmiNamespace=\"root/cimv2\" " +
                             " keyvalue=\"Name\" recheckInterval=\"3600000\" ifType=\"all\" resourceType=\"node\">" +
                             "<attrib name=\"AvailableBytes\" alias=\"wmiOSMemAvailBytes\" wmiObject=\"AvailableBytes\" type=\"GAUGE\"/>" +
                         "</wpm>" +
                     "</wpms>" +
                 "</wmi-collection>" +
            "</wmi-datacollection-config>",
            "target/classes/xsds/wmi-datacollection.xsd", }, });
    }
}

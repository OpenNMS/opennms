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

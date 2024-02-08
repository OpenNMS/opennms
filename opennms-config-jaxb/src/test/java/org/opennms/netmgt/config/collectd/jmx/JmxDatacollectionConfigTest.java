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
package org.opennms.netmgt.config.collectd.jmx;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.collection.api.AttributeType;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

/**
 * The Test Class for JmxDatacollectionConfig.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class JmxDatacollectionConfigTest extends XmlTestNoCastor<JmxDatacollectionConfig> {

    /**
     * Instantiates a new attribute test.
     *
     * @param sampleObject the sample object
     * @param sampleXml the sample XML
     * @param schemaFile the schema file
     */
    public JmxDatacollectionConfigTest(JmxDatacollectionConfig sampleObject, String sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    /**
     * Data.
     *
     * @return the collection
     * @throws java.text.ParseException the parse exception
     */
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Attrib a = new Attrib();
        a.setName("CollectionUsageThreshold");
        a.setAlias("EdenCollUseThrsh");
        a.setType(AttributeType.GAUGE);

        final CompAttrib comp = new CompAttrib();
        comp.setName("PeakUsage");
        comp.setAlias("EdenPeakUsage");
        comp.setType("Composite");
        final CompMember m1 = new CompMember();
        m1.setName("used");
        m1.setAlias("EdenPeakUsageUsed");
        m1.setType(AttributeType.GAUGE);
        comp.addCompMember(m1);
        final CompMember m2 = new CompMember();
        m2.setName("committed");
        m2.setAlias("EdenPeakUsgCmmttd");
        m2.setType(AttributeType.GAUGE);
        comp.addCompMember(m2);
        
        final Mbean bean = new Mbean();
        bean.setName("JVM MemoryPool:Eden Space");
        bean.setObjectname("java.lang:type=MemoryPool,name=Eden Space");
        bean.addAttrib(a);
        bean.addCompAttrib(comp);

        final Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.addRra("RRA:AVERAGE:0.5:1:4032");

        final JmxCollection collection = new JmxCollection();
        collection.setName("default");
        collection.setRrd(rrd);
        collection.addMbean(bean);

        final JmxDatacollectionConfig config = new JmxDatacollectionConfig();
        config.setRrdRepository("/opt/opennms/share/rrd/snmp");
        config.addJmxCollection(collection);

        return Arrays.asList(new Object[][] { {
            config,
            "<jmx-datacollection-config rrdRepository=\"/opt/opennms/share/rrd/snmp\">"
            + "<jmx-collection name=\"default\">"
            + "<rrd step=\"300\">"
            + "<rra>RRA:AVERAGE:0.5:1:4032</rra>"
            + "</rrd>"
            + "<mbeans>"
            + "<mbean name=\"JVM MemoryPool:Eden Space\" objectname=\"java.lang:type=MemoryPool,name=Eden Space\">"
            + "<attrib name=\"CollectionUsageThreshold\" alias=\"EdenCollUseThrsh\" type=\"gauge\" />"
            + "<comp-attrib name=\"PeakUsage\" alias=\"EdenPeakUsage\" type=\"Composite\">"
            + "<comp-member name=\"used\" alias=\"EdenPeakUsageUsed\" type=\"gauge\" />"
            + "<comp-member name=\"committed\" alias=\"EdenPeakUsgCmmttd\" type=\"gauge\" />"
            + "</comp-attrib>"
            + "</mbean>"
            + "</mbeans>"
            + "</jmx-collection>"
            + "</jmx-datacollection-config>",
            "target/classes/xsds/jmx-datacollection-config.xsd" } });
    }
}

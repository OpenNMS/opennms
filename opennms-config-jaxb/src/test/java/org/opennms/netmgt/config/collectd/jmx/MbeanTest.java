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
 * The Test Class for Mbean.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MbeanTest extends XmlTestNoCastor<Mbean> {

    /**
     * Instantiates a new attribute test.
     *
     * @param sampleObject the sample object
     * @param sampleXml the sample XML
     * @param schemaFile the schema file
     */
    public MbeanTest(Mbean sampleObject, String sampleXml, String schemaFile) {
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
        final Mbean bean = new Mbean();
        bean.setName("JVM MemoryPool:Eden Space");
        bean.setObjectname("java.lang:type=MemoryPool,name=Eden Space");
        bean.setResourceType("jvm-default");
        final Attrib a = new Attrib();
        a.setName("CollectionUsageThreshold");
        a.setAlias("EdenCollUseThrsh");
        a.setType(AttributeType.GAUGE);
        bean.addAttrib(a);
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
        bean.addCompAttrib(comp);

        return Arrays.asList(new Object[][] { {
            bean,
            "<mbean name=\"JVM MemoryPool:Eden Space\" objectname=\"java.lang:type=MemoryPool,name=Eden Space\" resource-type=\"jvm-default\">"
            + "<attrib name=\"CollectionUsageThreshold\" alias=\"EdenCollUseThrsh\" type=\"gauge\" />"
            + "<comp-attrib name=\"PeakUsage\" alias=\"EdenPeakUsage\" type=\"Composite\">"
            + "<comp-member name=\"used\" alias=\"EdenPeakUsageUsed\" type=\"gauge\" />"
            + "<comp-member name=\"committed\" alias=\"EdenPeakUsgCmmttd\" type=\"gauge\" />"
            + "</comp-attrib>"
            + "</mbean>",
            "target/classes/xsds/jmx-datacollection-config.xsd" } });
    }
}

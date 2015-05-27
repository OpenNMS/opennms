/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.collectd.jmx;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

/**
 * The Test Class for JmxCollection.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class JmxCollectionTest extends XmlTestNoCastor<JmxCollection> {

    /**
     * Instantiates a new attribute test.
     *
     * @param sampleObject the sample object
     * @param sampleXml the sample XML
     * @param schemaFile the schema file
     */
    public JmxCollectionTest(JmxCollection sampleObject, String sampleXml, String schemaFile) {
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
        a.setType("gauge");

        final CompAttrib comp = new CompAttrib();
        comp.setName("PeakUsage");
        comp.setAlias("EdenPeakUsage");
        comp.setType("Composite");
        final CompMember m1 = new CompMember();
        m1.setName("used");
        m1.setAlias("EdenPeakUsageUsed");
        m1.setType("gauge");
        comp.addCompMember(m1);
        final CompMember m2 = new CompMember();
        m2.setName("committed");
        m2.setAlias("EdenPeakUsgCmmttd");
        m2.setType("gauge");
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
        
        return Arrays.asList(new Object[][] { {
            collection,
            "<jmx-collection name=\"default\">"
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
            + "</jmx-collection>",
            "target/classes/xsds/jmx-datacollection-config.xsd" } });
    }
}

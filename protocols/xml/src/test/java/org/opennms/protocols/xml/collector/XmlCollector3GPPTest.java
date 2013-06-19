/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

/**
 * The Test class for XML Collector for 3GPP Statistics
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollector3GPPTest extends AbstractXmlCollectorTest {

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlConfigFileName()
     */
    @Override
    public String getXmlConfigFileName() {
        return "src/test/resources/3gpp-xml-datacollection-config.xml";
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getXmlSampleFileName() {
        return "src/test/resources/A20111025.0030-0500-0045-0500_MME00001.xml";
    }

    /**
     * Test time parser.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTimeParser() throws Exception {
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
        String value = "2011-10-25T00:45:00-05:00";
        long expectedTimestamp = 1319521500000l;
        DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
        DateTime dateTime = dtf.parseDateTime(value);
        Date date = dateTime.toDate();
        Assert.assertEquals(expectedTimestamp, date.getTime());

        MockDefaultXmlCollectionHandler handler = new MockDefaultXmlCollectionHandler();
        XPath xpath = XPathFactory.newInstance().newXPath();
        date = handler.getTimeStamp(MockDocumentBuilder.getXmlDocument(), xpath, getConfigDao().getDataCollectionByName("3GPP").getXmlSources().get(0).getXmlGroups().get(0));
        Assert.assertEquals(expectedTimestamp, date.getTime());
    }

    /**
     * Test XML collector with Standard handler.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultXmlCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "3GPP");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockDefaultXmlCollectionHandler");
        executeCollectorTest(parameters, 147);
        // Test a JRB.
        File file = new File("target/snmp/1/platformSystemResource/processor_v1_frame0_shelf0_slot4_sub-slot1/platform-system-resource.jrb");
        String[] dsnames = new String[] { "cpuUtilization", "memoryUtilization" };
        Double[] dsvalues = new Double[] { 1.0, 18.0 };
        validateJrb(file, dsnames, dsvalues);
    }

}

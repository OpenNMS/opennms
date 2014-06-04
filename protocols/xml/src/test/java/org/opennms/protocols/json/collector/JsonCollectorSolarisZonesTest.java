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

package org.opennms.protocols.json.collector;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.protocols.json.collector.AbstractJsonCollectorTest;
import org.opennms.protocols.json.collector.MockDocumentBuilder;

/**
 * The Test class for JSON Collector for Solaris Zones Statistics
 * 
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class JsonCollectorSolarisZonesTest extends AbstractJsonCollectorTest {

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlConfigFileName()
     */
    @Override
    public String getJSONConfigFileName() {
        return "src/test/resources/solaris-zones-datacollection-config.xml";
    }
    
    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getJSONSampleFileName() {
        return "src/test/resources/solaris-zones.json";
    }

    /**
     * Test to verify XPath content.
     *
     * @throws Exception the exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testXpath() throws Exception {
        JSONObject json = MockDocumentBuilder.getJSONDocument();
        JXPathContext context = JXPathContext.newContext(json);
        Iterator<Pointer> itr = context.iteratePointers("/zones/zone");
        while (itr.hasNext()) {
            Pointer resPtr = itr.next();
            JXPathContext relativeContext = context.getRelativeContext(resPtr);
            String resourceName = (String) relativeContext.getValue("@name");
            Assert.assertNotNull(resourceName);
            String value = (String) relativeContext.getValue("parameter[@key='nproc']/@value");
            Assert.assertNotNull(Integer.valueOf(value));
        }
    }

    /**
     * Test JSON collector with Standard handler.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultJsonCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "Solaris");
        parameters.put("handler-class", "org.opennms.protocols.json.collector.MockDefaultJsonCollectionHandler");
        // Files expected: one JRB for each zone: global, zone1 and zone2 (3 in total)
        executeCollectorTest(parameters, 3);
        Assert.assertTrue(new File("target/snmp/1/solarisZoneStats/global/solaris-zone-stats.jrb").exists());
        Assert.assertTrue(new File("target/snmp/1/solarisZoneStats/zone1/solaris-zone-stats.jrb").exists());
        Assert.assertTrue(new File("target/snmp/1/solarisZoneStats/zone2/solaris-zone-stats.jrb").exists());
        // Checking data from Global Zone.
        File file = new File("target/snmp/1/solarisZoneStats/global/solaris-zone-stats.jrb");
        String[] dsnames = new String[] { "nproc", "nlwp", "pr_size", "pr_rssize", "pctmem", "pctcpu" };
        Double[] dsvalues = new Double[] { 245.0, 1455.0, 2646864.0, 1851072.0, 0.7, 0.24 };
        validateJrb(file, dsnames, dsvalues);
    }

}

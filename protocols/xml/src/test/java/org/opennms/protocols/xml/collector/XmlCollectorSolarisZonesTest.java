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
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Test class for XML Collector for Solaris Zones Statistics
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollectorSolarisZonesTest extends AbstractXmlCollectorTest {

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlConfigFileName()
     */
    @Override
    public String getXmlConfigFileName() {
        return "src/test/resources/solaris-zones-datacollection-config.xml";
    }
    
    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getXmlSampleFileName() {
        return "src/test/resources/solaris-zones.xml";
    }

    /**
     * Test to verify XPath content.
     *
     * @throws Exception the exception
     */
    @Test
    public void testXpath() throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Document doc = MockDocumentBuilder.getXmlDocument();
        NodeList resourceList = (NodeList) xpath.evaluate("/zones/zone", doc, XPathConstants.NODESET);
        for (int j = 0; j < resourceList.getLength(); j++) {
            Node resource = resourceList.item(j);
            Node resourceName = (Node) xpath.evaluate("@name", resource, XPathConstants.NODE);
            Assert.assertNotNull(resourceName);
            String value = (String) xpath.evaluate("parameter[@key='nproc']/@value", resource, XPathConstants.STRING);
            Assert.assertNotNull(Integer.valueOf(value));
        }
    }

    /**
     * Test XML collector with Standard handler.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultXmlCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "Solaris");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockDefaultXmlCollectionHandler");
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

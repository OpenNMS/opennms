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
 * The Test class for XML Collector on a file with XML namespaces
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:david.schlenk@spanlink.com">David Schlenk</a>
 */
public class XmlCollectorNamespaceTest extends XmlCollectorITCase {

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlConfigFileName()
     */
    @Override
    public String getConfigFileName() {
        return "src/test/resources/namespace-datacollection-config.xml";
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getSampleFileName() {
        return "src/test/resources/namespace.xml";
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
        DocumentNamespaceResolver dnr = new DocumentNamespaceResolver(doc);
        xpath.setNamespaceContext(dnr);
        NodeList resourceList = (NodeList) xpath.evaluate("/dp:RootElement/dp:ChildElement/dp:StatList", doc, XPathConstants.NODESET);
        for (int j = 0; j < resourceList.getLength(); j++) {
            Node resource = resourceList.item(j);
            Node resourceID = (Node) xpath.evaluate("dp:Stat[@Name = 'ID']", resource, XPathConstants.NODE);
            Assert.assertNotNull(resourceID);
            String value = (String) xpath.evaluate("dp:Stat[@Name='PANTS']/@Value", resource, XPathConstants.STRING);
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
        parameters.put("collection", "Namespace");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockDefaultXmlCollectionHandler");
        // Files expected: one JRB for each zone: global, zone1 and zone2 (3 in total)
        executeCollectorTest(parameters, 1);
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/namespace-stats.jrb").exists());
        // Checking data from Global Zone.
        File file = new File(getSnmpRootDirectory(), "1/namespace-stats.jrb");
        String[] dsnames = new String[] { "requests", "responses", "errors", "warnings", "hats", "pants" };
        Double[] dsvalues = new Double[] { 13.0, 12.0, 1.0, 2.0, 3.0, 4.0 };
        validateJrb(file, dsnames, dsvalues);      
    }

}

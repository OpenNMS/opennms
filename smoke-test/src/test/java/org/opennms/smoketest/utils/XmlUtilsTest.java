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
package org.opennms.smoketest.utils;

import org.junit.Test;
import org.testcontainers.shaded.com.google.common.io.CharSource;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class XmlUtilsTest {

    /**
     * Verifies that we can filter attributes from XML.
     *
     * @throws Exception on error
     */
    @Test
    public void canFilterAttributesFromXml() throws Exception {
        String xmlIn = "<ipInterface isDown=\"true\" hasFlows=\"false\" monitoredServiceCount=\"0\" snmpPrimary=\"N\">\n" +
                "   <ipAddress>192.168.1.1</ipAddress>\n" +
                "   <hostName>192.168.1.1</hostName>\n" +
                "   <nodeId>1</nodeId>\n" +
                "</ipInterface>";
        String expectedFilteredXml = "<ipInterface monitoredServiceCount=\"0\" snmpPrimary=\"N\">\n" +
                "   <ipAddress>192.168.1.1</ipAddress>\n" +
                "   <hostName>192.168.1.1</hostName>\n" +
                "   <nodeId>1</nodeId>\n" +
                "</ipInterface>";
        String actualFilteredXml = XmlUtils.filterAttributesFromXml(xmlIn, "isDown", "hasFlows");
        assertThat(actualFilteredXml, equalTo(expectedFilteredXml));
    }
}

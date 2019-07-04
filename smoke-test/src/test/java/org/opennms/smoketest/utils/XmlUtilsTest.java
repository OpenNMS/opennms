/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

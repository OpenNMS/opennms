/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.systemreport.sanitizer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class XmlFileSanitizer implements ConfigFileSanitizer {

    private final XPath xPath;

    private final DocumentBuilder builder;

    private final Transformer transformer;

    private final Set<String> ATTRIBUTES_TO_SANITIZE = new LinkedHashSet<>(Arrays.asList("password", "authen-password", "oauth-consumer-secret", "oauth-access-token-secret", "writeCommunity"));

    private final Set<String> PARAM_KEYS_TO_SANITIZE = new LinkedHashSet<>(Arrays.asList("password", "j_password", "login:command/password"));

    private final Set<String> TAGS_TO_SANITIZE = new LinkedHashSet<>(Arrays.asList("password", "login-password"));

    private final String SANITIZED_VALUE = "***";

    public XmlFileSanitizer() throws ParserConfigurationException, TransformerConfigurationException {
        this.xPath = XPathFactory.newInstance().newXPath();
        this.builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.transformer = TransformerFactory.newInstance().newTransformer();
    }

    @Override
    public String getFileType() {
        return "xml";
    }

    public SanitizedResource getSanitizedResource(final File file) throws FileSanitizationException {
        try {
            return sanitizeXml(file);
        } catch (SAXException e) {
            throw new FileSanitizationException("Could not parse XML file", e);
        } catch (Exception e) {
            throw new FileSanitizationException("Could not sanitize file", e);
        }
    }

    private SanitizedResource sanitizeXml(final File file) throws IOException, SAXException, TransformerException, XPathExpressionException {
        final Document doc = builder.parse(file);
        final Node rootNode = doc.getDocumentElement();

        for (String attribute : ATTRIBUTES_TO_SANITIZE) {
            replaceAttribute(rootNode, attribute);
        }
        for (String paramKey : PARAM_KEYS_TO_SANITIZE) {
            replaceParamValue(rootNode, paramKey);
        }
        for (String tag : TAGS_TO_SANITIZE) {
            replaceTagValue(rootNode, tag);
        }

        Writer output = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(output));
        return new SanitizedResource(output.toString().getBytes());
    }

    private int replaceAttribute(Node rootNode, String attributeName) throws XPathExpressionException {
        NodeList nodesWithAttribute = (NodeList) xPath.evaluate(String.format("//*[@%s]", attributeName), rootNode, XPathConstants.NODESET);

        for (int i = 0; i < nodesWithAttribute.getLength(); i++) {
            nodesWithAttribute.item(i).getAttributes().getNamedItem(attributeName).setNodeValue(SANITIZED_VALUE);
        }

        return nodesWithAttribute.getLength();
    }

    private int replaceParamValue(Node rootNode, String keyName) throws XPathExpressionException {
        NodeList nodesWithAttribute = (NodeList) xPath.evaluate(String.format("//*[@key='%s']", keyName),
                    rootNode, XPathConstants.NODESET);

        for (int i = 0; i < nodesWithAttribute.getLength(); i++) {
            if (nodesWithAttribute.item(i).getAttributes().getNamedItem("value") != null) {
                nodesWithAttribute.item(i).getAttributes().getNamedItem("value").setNodeValue(SANITIZED_VALUE);
            }
        }

        return nodesWithAttribute.getLength();
    }

    private int replaceTagValue(Node rootNode, String tagName) throws XPathExpressionException {
        NodeList nodesWithAttribute = (NodeList) xPath.evaluate(String.format("//%s", tagName),
                    rootNode, XPathConstants.NODESET);

        for (int i = 0; i < nodesWithAttribute.getLength(); i++) {
            nodesWithAttribute.item(i).setTextContent(SANITIZED_VALUE);
        }

        return nodesWithAttribute.getLength();
    }
}

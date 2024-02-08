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
package org.opennms.systemreport.sanitizer;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class XmlFileSanitizer implements ConfigFileSanitizer {

    private final XPath xPath;

    private final DocumentBuilder builder;

    private final Transformer transformer;

    protected final Set<String> ATTRIBUTES_TO_SANITIZE = new LinkedHashSet<>(Arrays.asList("password", "authen-password", "oauth-consumer-secret", "oauth-access-token-secret", "writeCommunity", "auth-passphrase", "privacy-passphrase",
            "read-community", "write-community"));

    protected final Set<String> PARAM_KEYS_TO_SANITIZE = new LinkedHashSet<>(Arrays.asList("password", "j_password", "login:command/password"));

    protected final Set<String> TAGS_TO_SANITIZE = new LinkedHashSet<>(Arrays.asList("password", "login-password"));

    protected final String SANITIZED_VALUE = "***";

    public XmlFileSanitizer() throws ParserConfigurationException, TransformerConfigurationException {
        this.xPath = XPathFactory.newInstance().newXPath();
        this.builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.transformer = TransformerFactory.newInstance().newTransformer();
    }

    @Override
    public String getFileName() {
        return "*.xml";
    }

    @Override
    public Resource getSanitizedResource(final File file) throws FileSanitizationException {
        try {
            final Document doc = builder.parse(file);

            sanitizeXmlDocument(doc);

            Writer output = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(output));
            return new ByteArrayResource(output.toString().getBytes());
        } catch (SAXException e) {
            throw new FileSanitizationException("Could not parse XML file", e);
        } catch (Exception e) {
            throw new FileSanitizationException("Could not sanitize file", e);
        }
    }

    protected void sanitizeXmlDocument(Document doc) throws SAXException, XPathExpressionException {
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
    }

    private void replaceAttribute(Node rootNode, String attributeName) throws XPathExpressionException {
        NodeList nodesWithAttribute = (NodeList) xPath.evaluate(String.format("//*[@%s]", attributeName), rootNode, XPathConstants.NODESET);

        for (int i = 0; i < nodesWithAttribute.getLength(); i++) {
            nodesWithAttribute.item(i).getAttributes().getNamedItem(attributeName).setNodeValue(SANITIZED_VALUE);
        }
    }

    private void replaceParamValue(Node rootNode, String keyName) throws XPathExpressionException {
        NodeList nodesWithAttribute = (NodeList) xPath.evaluate(String.format("//*[@key='%s']", keyName),
                rootNode, XPathConstants.NODESET);

        for (int i = 0; i < nodesWithAttribute.getLength(); i++) {
            if (nodesWithAttribute.item(i).getAttributes().getNamedItem("value") != null) {
                nodesWithAttribute.item(i).getAttributes().getNamedItem("value").setNodeValue(SANITIZED_VALUE);
            }
        }
    }

    private void replaceTagValue(Node rootNode, String tagName) throws XPathExpressionException {
        NodeList nodesWithAttribute = (NodeList) xPath.evaluate(String.format("//%s", tagName),
                rootNode, XPathConstants.NODESET);

        for (int i = 0; i < nodesWithAttribute.getLength(); i++) {
            nodesWithAttribute.item(i).setTextContent(SANITIZED_VALUE);
        }
    }
}

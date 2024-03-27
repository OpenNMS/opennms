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

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtils {

    /**
     * Used to remove a fixed set of known attributes from an XML string.
     *
     * We use this since some of the entity include fields which have no corresponding
     * setters and as a result, cause errors when POSTing to the REST APIs.
     *
     * @param xmlIn xml string
     * @param attributeNames list of attributes to remove
     * @return filtered xml
     */
    protected static String filterAttributesFromXml(String xmlIn, String... attributeNames) {
        try {
            final DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = dbfact.newDocumentBuilder();
            final Document doc = builder.parse(new InputSource(new StringReader(xmlIn)));

            final NodeList nodes = doc.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                final Node node = nodes.item(i);
                final NamedNodeMap attributes = node.getAttributes();
                for (String attributeName : attributeNames) {
                    if (attributes.getNamedItem(attributeName) != null) {
                        attributes.removeNamedItem(attributeName);
                    }
                }
            }

            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            final StringWriter writer = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

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

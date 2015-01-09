/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.w3c.dom.Document;

/**
 * The Mock Document Builder.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockDocumentBuilder {

    /** The XML file name. */
    public static String m_xmlFileName;

    /**
     * Instantiates a new mock document builder.
     */
    private MockDocumentBuilder() {}

    /**
     * Gets the XML document.
     *
     * @return the XML document
     */
    public static Document getXmlDocument() {
        if (m_xmlFileName == null)
            return null;
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setIgnoringComments(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(m_xmlFileName);
            //ugly but necessary hack to deal with documents that have a namespace defined without a prefix
            if(doc.getDocumentElement().getNamespaceURI() != null && doc.getDocumentElement().getPrefix() == null){
                factory.setNamespaceAware(false);
                builder = factory.newDocumentBuilder();
                doc = builder.parse(m_xmlFileName);
            }
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return doc;
    }

    /**
     * Sets the XML file name.
     *
     * @param xmlFileName the new XML file name
     */
    public static void setXmlFileName(String xmlFileName) {
        m_xmlFileName = xmlFileName;
    }
    

}


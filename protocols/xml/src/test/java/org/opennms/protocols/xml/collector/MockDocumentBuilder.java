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


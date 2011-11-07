/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.w3c.dom.Document;

/**
 * The Mock Document Builder.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockDocumentBuilder {
    
    /**
     * Instantiates a new mock document builder.
     */
    private MockDocumentBuilder() {}

    /**
     * Gets the xml document.
     *
     * @param agent the agent
     * @param urlString the url string
     * @return the xml document
     */
    public static Document getXmlDocument(CollectionAgent agent, String urlString) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse("src/test/resources/A20111025.0030-0500-0045-0500_MME00001.xml");
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return doc;
    }

}


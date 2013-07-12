/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlResourceUtils;
import org.w3c.dom.Document;

/**
 * The Mock Class for DefaultXmlCollectionHandler.
 * <p>This file is created in order to avoid calling a real server to retrieve a valid file and  parse a provided sample file through MockDocumentBuilder</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockDefaultXmlCollectionHandler extends DefaultXmlCollectionHandler {

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler#getXmlDocument(java.lang.String, org.opennms.protocols.xml.config.Request)
     */
    @Override
    protected Document getXmlDocument(String urlString, Request request) {
        return MockDocumentBuilder.getXmlDocument();
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler#parseUrl(java.lang.String, org.opennms.netmgt.collectd.CollectionAgent, java.lang.Integer)
     */
    @Override
    protected String parseUrl(String unformattedUrl, CollectionAgent agent, Integer collectionStep) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler#getXmlResourceType(org.opennms.netmgt.collectd.CollectionAgent, java.lang.String)
     */
    @Override
    protected XmlResourceType getXmlResourceType(CollectionAgent agent, String resourceType) {
        return XmlResourceUtils.getXmlResourceType(agent, resourceType);
    }
}


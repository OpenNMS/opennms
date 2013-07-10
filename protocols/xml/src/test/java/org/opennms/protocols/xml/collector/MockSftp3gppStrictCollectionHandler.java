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

import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.PersistAllSelectorStrategy;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * The Mock Class for Sftp3gppXmlCollectionHandler.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockSftp3gppStrictCollectionHandler extends Sftp3gppXmlCollectionHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(MockSftp3gppStrictCollectionHandler.class);


    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler#getXmlDocument(java.lang.String, org.opennms.protocols.xml.config.Request)
     */
    @Override
    protected Document getXmlDocument(String urlString, Request request) {
        return MockDocumentBuilder.getXmlDocument();
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.Sftp3gppXmlCollectionHandler#parseUrl(java.lang.String, org.opennms.netmgt.collectd.CollectionAgent, java.lang.Integer, long)
     */
    @Override
    protected String parseUrl(String unformattedUrl, CollectionAgent agent, Integer collectionStep, long currentTimestamp) throws IllegalArgumentException {
        LOG.info("parseUrl: reference timestamp is {}", new Date(currentTimestamp));
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler#getXmlResourceType(org.opennms.netmgt.collectd.CollectionAgent, java.lang.String)
     */
    @Override
    protected XmlResourceType getXmlResourceType(CollectionAgent agent, String resourceType) {
        ResourceType rt = new ResourceType();
        rt.setName(resourceType);
        rt.setStorageStrategy(new StorageStrategy());
        rt.getStorageStrategy().setClazz(XmlStorageStrategy.class.getName());
        rt.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy());
        rt.getPersistenceSelectorStrategy().setClazz(PersistAllSelectorStrategy.class.getName());
        return new XmlResourceType(agent, rt);
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler#getTimeStamp(org.w3c.dom.Document, javax.xml.xpath.XPath, org.opennms.protocols.xml.config.XmlGroup)
     */
    @Override
    protected Date getTimeStamp(Document doc, XPath xpath, XmlGroup group) throws XPathExpressionException {
        long ts = super.getTimeStamp(doc, xpath, group).getTime();
        long offset = System.currentTimeMillis() - ts;
        return new Date(ts + offset + 900000);
    }

}


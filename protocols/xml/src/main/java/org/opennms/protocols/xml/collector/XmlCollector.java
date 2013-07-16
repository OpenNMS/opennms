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

import java.io.File;
import java.util.Map;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.CollectionInitializationException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class XmlCollector.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollector implements ServiceCollector {
	
	private static final Logger LOG = LoggerFactory.getLogger(XmlCollector.class);


    /** The XML Data Collection DAO. */
    private XmlDataCollectionConfigDao m_xmlCollectionDao;

    /** The XML Collection Handler. */
    private XmlCollectionHandler m_collectionHandler;

    /**
     * Gets the XML Data Collection DAO.
     *
     * @return the XML Data Collection DAO
     */
    public XmlDataCollectionConfigDao getXmlCollectionDao() {
        return m_xmlCollectionDao;
    }

    /**
     * Sets the XML Data Collection DAO.
     *
     * @param xmlCollectionDao the new XML Data Collection DAO
     */
    public void setXmlCollectionDao(XmlDataCollectionConfigDao xmlCollectionDao) {
        m_xmlCollectionDao = xmlCollectionDao;
    }

    /**
     * Log.
     *
     * @return the thread category
     */

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#initialize(java.util.Map)
     */
    @Override
    public void initialize(Map<String, String> parameters) throws CollectionInitializationException {
        LOG.debug("initialize: initializing XML collector");

        // Retrieve the DAO for our configuration file.
        if (m_xmlCollectionDao == null)
            m_xmlCollectionDao = BeanUtils.getBean("daoContext", "xmlDataCollectionConfigDao", XmlDataCollectionConfigDao.class);

        // If the RRD file repository directory does NOT already exist, create it.
        LOG.debug("initialize: Initializing RRD repo from XmlCollector...");
        File f = new File(m_xmlCollectionDao.getConfig().getRrdRepository());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new CollectionInitializationException("Unable to create RRD file repository.  Path doesn't already exist and could not make directory: " + m_xmlCollectionDao.getConfig().getRrdRepository());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#initialize(org.opennms.netmgt.collectd.CollectionAgent, java.util.Map)
     */
    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
        LOG.debug("initialize: initializing XML collection handling using {} for collection agent {}", parameters, agent);
        String serviceName = ParameterMap.getKeyedString(parameters, "SERVICE", "XML");
        String handlerClass = ParameterMap.getKeyedString(parameters, "handler-class", "org.opennms.protocols.xml.collector.DefaultXmlCollectionHandler");
        try {
            LOG.debug("initialize: instantiating XML collection handler {}", handlerClass);
            Class<?> clazz = Class.forName(handlerClass);
            m_collectionHandler = (XmlCollectionHandler) clazz.newInstance();
            m_collectionHandler.setServiceName(serviceName);
        } catch (Exception e) {
            throw new CollectionInitializationException("Unable to instantiate XML Collection Handler " + handlerClass + " because: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#release()
     */
    @Override
    public void release() {
        LOG.debug("release: realeasing XML collection");
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#release(org.opennms.netmgt.collectd.CollectionAgent)
     */
    @Override
    public void release(CollectionAgent agent) {
        LOG.debug("release: realeasing XML collection for agent {}", agent);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#collect(org.opennms.netmgt.collectd.CollectionAgent, org.opennms.netmgt.model.events.EventProxy, java.util.Map)
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) throws CollectionException {
        if (parameters == null) {
            throw new CollectionException("Null parameters is now allowed in XML Collector!");
        }
        try {
            // Getting XML Collection
            String collectionName = ParameterMap.getKeyedString(parameters, "collection", null);
            if (collectionName == null) {
                collectionName = ParameterMap.getKeyedString(parameters, "xml-collection", null);
            }
            if (collectionName == null) {
                throw new CollectionException("Parameter collection is required for the XML Collector!");
            }
            LOG.debug("collect: collecting XML data using collection {}", collectionName);
            XmlDataCollection collection = m_xmlCollectionDao.getDataCollectionByName(collectionName);
            if (collection == null) {
                throw new CollectionException("XML Collection " + collectionName +" does not exist.");
            }

            // Filling XML CollectionSet
            m_collectionHandler.setRrdRepository(getRrdRepository(collectionName));
            return m_collectionHandler.collect(agent, collection, parameters);
        } catch (Exception e) {
            throw new CollectionException("Can't collect XML data because " + e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#getRrdRepository(java.lang.String)
     */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return m_xmlCollectionDao.getConfig().buildRrdRepository(collectionName);
    }

}

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

import java.io.File;
import java.util.Map;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;

/**
 * The Class XmlCollector.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollector implements ServiceCollector {

    /** The XML Data Collection DAO. */
    private XmlDataCollectionConfigDao m_xmlCollectionDao;

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
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#initialize(java.util.Map)
     */
    @Override
    public void initialize(Map<String, String> parameters) {
        log().debug("initialize: Initializing XML Collector.");
        initialize();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#initialize(org.opennms.netmgt.collectd.CollectionAgent, java.util.Map)
     */
    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) {        
        log().debug("initialize: Initializing XML collection for agent: " + agent);
        initialize();
    }

    /**
     * Initialize.
     */
    private void initialize() {
        // Retrieve the DAO for our configuration file.
        if (m_xmlCollectionDao == null)
            m_xmlCollectionDao = BeanUtils.getBean("daoContext", "xmlDataCollectionConfigDao", XmlDataCollectionConfigDao.class);

        // If the RRD file repository directory does NOT already exist, create it.
        log().debug("initializeRrdRepository: Initializing RRD repo from XmlCollector...");
        File f = new File(m_xmlCollectionDao.getConfig().getRrdRepository());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("Unable to create RRD file repository.  Path doesn't already exist and could not make directory: " + m_xmlCollectionDao.getConfig().getRrdRepository());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#release()
     */
    @Override
    public void release() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#release(org.opennms.netmgt.collectd.CollectionAgent)
     */
    @Override
    public void release(CollectionAgent agent) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#collect(org.opennms.netmgt.collectd.CollectionAgent, org.opennms.netmgt.model.events.EventProxy, java.util.Map)
     */
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
            log().debug("collect: collecting XML data using collection " + collectionName);
            XmlDataCollection collection = m_xmlCollectionDao.getDataCollectionByName(collectionName);

            // Filling XML CollectionSet
            String handlerClass = ParameterMap.getKeyedString(parameters, "handler-class", "org.opennms.protocols.xml.collector.DefaultXmlCollectionHandler");
            log().debug("collect: instantiating XML collection handler " + handlerClass);
            Class<?> clazz = Class.forName(handlerClass);
            XmlCollectionHandler handler = (XmlCollectionHandler) clazz.newInstance();
            return handler.collect(agent, collection, parameters);
        } catch (Exception e) {
            throw new CollectionException("Can't collect XML data because " + e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#getRrdRepository(java.lang.String)
     */
    public RrdRepository getRrdRepository(String collectionName) {
        return m_xmlCollectionDao.getConfig().buildRrdRepository(collectionName);
    }

}

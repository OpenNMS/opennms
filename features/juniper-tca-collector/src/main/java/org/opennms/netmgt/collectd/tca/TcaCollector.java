/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.Map;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.Collectd;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.CollectionInitializationException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.collectd.tca.dao.TcaDataCollectionConfigDao;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TcaCollector.
 * 
 * <p>A collector specialized to retrieve special SNMP data from Juniper TCA Devices.</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class TcaCollector implements ServiceCollector {
	private static final Logger LOG = LoggerFactory.getLogger(TcaCollector.class);

	/** The service name. */
	private String m_serviceName;

	/** The TCA Data Collection Configuration DAO. */
	private TcaDataCollectionConfigDao m_configDao;

	/**
	 * Gets the TCA Data Collection Configuration DAO.
	 *
	 * @return the TCA Data Collection Configuration DAO
	 */
	public TcaDataCollectionConfigDao getConfigDao() {
		return m_configDao;
	}

	/**
	 * Sets the TCA Data Collection Configuration DAO.
	 *
	 * @param configDao the new TCA Data Collection Configuration DAO
	 */
	public void setConfigDao(TcaDataCollectionConfigDao configDao) {
		this.m_configDao = configDao;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.ServiceCollector#initialize(java.util.Map)
	 */
	@Override
	public void initialize(Map<String, String> parameters) throws CollectionInitializationException {
		LOG.debug("initialize: initializing TCA collector");

		// Initialize SNMP Factory
		try {
			SnmpPeerFactory.init();
		} catch (IOException e) {
			LOG.error("initSnmpPeerFactory: Failed to load SNMP configuration: {}", e, e);
			throw new UndeclaredThrowableException(e);
		}

		// Retrieve the DAO for our configuration file.
		if (m_configDao == null)
			m_configDao = BeanUtils.getBean("daoContext", "tcaDataCollectionConfigDao", TcaDataCollectionConfigDao.class);

		// If the RRD file repository directory does NOT already exist, create it.
		LOG.debug("initialize: Initializing RRD repo from XmlCollector...");
		File f = new File(m_configDao.getConfig().getRrdRepository());
		if (!f.isDirectory()) {
			if (!f.mkdirs()) {
				throw new CollectionInitializationException("Unable to create RRD file repository.  Path doesn't already exist and could not make directory: " + m_configDao.getConfig().getRrdRepository());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.ServiceCollector#initialize(org.opennms.netmgt.collectd.CollectionAgent, java.util.Map)
	 */
	@Override
	public void initialize(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
		LOG.debug("initialize: initializing TCA collection handling using {} for collection agent {}", parameters, agent);
		m_serviceName = ParameterMap.getKeyedString(parameters, "SERVICE", "TCA");
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.ServiceCollector#release()
	 */
	@Override
	public void release() {
		LOG.debug("release: realeasing TCA collection");
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.ServiceCollector#release(org.opennms.netmgt.collectd.CollectionAgent)
	 */
	@Override
	public void release(CollectionAgent agent) {
		LOG.debug("release: realeasing TCA collection for agent {}", agent);
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.ServiceCollector#collect(org.opennms.netmgt.collectd.CollectionAgent, org.opennms.netmgt.model.events.EventProxy, java.util.Map)
	 */
	@Override
	public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) throws CollectionException {
		try {
			String collectionName = ParameterMap.getKeyedString(parameters, "collection", null);
			if (collectionName == null) {
				collectionName = ParameterMap.getKeyedString(parameters, "tca-collection", null);
			}
			if (collectionName == null) {
				throw new CollectionException("Parameter collection is required for the TCA Collector!");
			}
			Collectd.instrumentation().beginCollectingServiceData(agent.getNodeId(), agent.getHostAddress(), m_serviceName);
			TcaCollectionSet collectionSet = new TcaCollectionSet(agent, getRrdRepository(collectionName));
			collectionSet.setCollectionTimestamp(new Date());
			collectionSet.collect();
			return collectionSet;
		} catch (Throwable t) {
			CollectionException e = new CollectionException("Unexpected error during node TCA collection for: " + agent.getHostAddress() + ": " + t, t);
			Collectd.instrumentation().reportCollectionException(agent.getNodeId(), agent.getHostAddress(), m_serviceName, e);
			throw e;
		} finally {
			Collectd.instrumentation().endCollectingServiceData(agent.getNodeId(), agent.getHostAddress(), m_serviceName);
		}
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.ServiceCollector#getRrdRepository(java.lang.String)
	 */
	@Override
	public RrdRepository getRrdRepository(String collectionName) {
		return m_configDao.getConfig().buildRrdRepository(collectionName);
	}
}

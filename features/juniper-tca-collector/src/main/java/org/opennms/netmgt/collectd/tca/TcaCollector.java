/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collectd.tca.dao.TcaDataCollectionConfigDao;
import org.opennms.netmgt.collection.api.AbstractServiceCollector;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TcaCollector.
 * 
 * <p>A collector specialized to retrieve special SNMP data from Juniper TCA Devices.</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class TcaCollector extends AbstractServiceCollector {
	private static final Logger LOG = LoggerFactory.getLogger(TcaCollector.class);

	/** The TCA Data Collection Configuration DAO. */
	private TcaDataCollectionConfigDao m_configDao;

	private ResourceStorageDao m_resourceStorageDao;

	private ResourceTypesDao m_resourceTypesDao;

	private LocationAwareSnmpClient m_locationAwareSnmpClient;

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.ServiceCollector#initialize(java.util.Map)
	 */
	@Override
	public void initialize() throws CollectionInitializationException {
		LOG.debug("initialize: initializing TCA collector");

		// Retrieve the DAO for our configuration file.
		if (m_configDao == null) {
			m_configDao = BeanUtils.getBean("daoContext", "tcaDataCollectionConfigDao", TcaDataCollectionConfigDao.class);
		}

		if (m_resourceStorageDao == null) {
			m_resourceStorageDao = BeanUtils.getBean("daoContext", "resourceStorageDao", ResourceStorageDao.class);
		}

		if (m_resourceTypesDao == null) {
			m_resourceTypesDao = BeanUtils.getBean("daoContext", "resourceTypesDao", ResourceTypesDao.class);
		}

		if (m_locationAwareSnmpClient == null) {
			m_locationAwareSnmpClient = BeanUtils.getBean("daoContext", "locationAwareSnmpClient", LocationAwareSnmpClient.class);
		}
	}

	@Override
	public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
		try {
			String collectionName = ParameterMap.getKeyedString(parameters, "collection", null);
			if (collectionName == null) {
				collectionName = ParameterMap.getKeyedString(parameters, "tca-collection", null);
			}
			if (collectionName == null) {
				throw new CollectionException("Parameter collection is required for the TCA Collector!");
			}
			TcaCollectionHandler collectionHandler = new TcaCollectionHandler((SnmpCollectionAgent)agent, getRrdRepository(collectionName),
			        m_resourceStorageDao, m_resourceTypesDao, m_locationAwareSnmpClient);
			return collectionHandler.collect();
		} catch (CollectionException e) {
			throw e;
		} catch (Throwable t) {
			LOG.error("Unexpected error during node TCA collection for: {}", agent.getHostAddress(), t);
			throw new CollectionException("Unexpected error during node TCA collection for: " + agent.getHostAddress() + ": " + t, t);
		}
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.ServiceCollector#getRrdRepository(java.lang.String)
	 */
	@Override
	public RrdRepository getRrdRepository(String collectionName) {
		return m_configDao.getConfig().buildRrdRepository(collectionName);
	}

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

    public ResourceStorageDao getResourceStorageDao() {
        return m_resourceStorageDao;
    }

    public void setResourceStorageDao(ResourceStorageDao resourceStorageDao) {
        m_resourceStorageDao = resourceStorageDao;
    }

    public void setResourceTypesDao(ResourceTypesDao resourceTypesDao) {
        m_resourceTypesDao = resourceTypesDao;
    }

    public void setLocationAwareSnmpClient(LocationAwareSnmpClient locationAwareSnmpClient) {
        m_locationAwareSnmpClient = locationAwareSnmpClient;
    }
}

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
package org.opennms.netmgt.collectd.tca;

import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.features.distributed.kvstore.api.BlobStore;
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

	private BlobStore m_blobStore;

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
		if(m_blobStore == null) {
			m_blobStore = BeanUtils.getBean("daoContext", "blobStore", BlobStore.class);
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
			        m_resourceTypesDao, m_locationAwareSnmpClient, m_blobStore);
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

	public void setBlobStore(BlobStore blobStore) {
		m_blobStore = blobStore;
	}
}

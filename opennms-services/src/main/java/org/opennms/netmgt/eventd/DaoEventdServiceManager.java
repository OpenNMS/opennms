/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.dao.api.EventdServiceManager;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * <p>DaoEventdServiceManager class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Transactional(readOnly = true)
public class DaoEventdServiceManager implements InitializingBean, EventdServiceManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(DaoEventdServiceManager.class);
    
    private ServiceTypeDao m_serviceTypeDao;

    /**
     * Cache of service names to service IDs.
     */
    private Map<String, Integer> m_serviceMap = new HashMap<String, Integer>();

    /**
     * <p>Constructor for DaoEventdServiceManager.</p>
     */
    public DaoEventdServiceManager() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventdServiceManager#getServiceId(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public synchronized int getServiceId(String serviceName) throws DataAccessException {
        Assert.notNull(serviceName, "The serviceName argument must not be null");

        if (m_serviceMap.containsKey(serviceName)) {
            return m_serviceMap.get(serviceName).intValue();
        } else {
            LOG.debug("Could not find entry for '{}' in service name cache.  Looking up in database.", serviceName);
            
            OnmsServiceType serviceType = m_serviceTypeDao.findByName(serviceName);
            if (serviceType == null) {
                LOG.debug("Did not find entry for '{}' in database.", serviceName);
                return -1;
            }
            
            LOG.debug("Found entry for '{}' (ID {}) in database.  Adding to service name cache.", serviceName, serviceType.getId());

            m_serviceMap.put(serviceType.getName(), serviceType.getId());
            
            return serviceType.getId();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventdServiceManager#dataSourceSync()
     */
    /**
     * <p>dataSourceSync</p>
     */
    @Override
    public synchronized void dataSourceSync() {
        m_serviceMap.clear();
        
        for (OnmsServiceType serviceType : m_serviceTypeDao.findAll()) {
            m_serviceMap.put(serviceType.getName(), serviceType.getId());
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_serviceTypeDao != null, "property serviceTypeDao must be set");
    }

    /**
     * <p>getServiceTypeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ServiceTypeDao} object.
     */
    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }

    /**
     * <p>setServiceTypeDao</p>
     *
     * @param serviceTypeDao a {@link org.opennms.netmgt.dao.api.ServiceTypeDao} object.
     */
    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }
}

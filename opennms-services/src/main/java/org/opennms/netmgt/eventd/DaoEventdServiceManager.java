/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.eventd;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Transactional(readOnly = true)
public class DaoEventdServiceManager implements InitializingBean, EventdServiceManager {
    private ServiceTypeDao m_serviceTypeDao;

    /**
     * Cache of service names to service IDs.
     */
    private Map<String, Integer> m_serviceMap = new HashMap<String, Integer>();

    public DaoEventdServiceManager() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventdServiceManager#getServiceId(java.lang.String)
     */
    public synchronized int getServiceId(String serviceName) throws DataAccessException {
        Assert.notNull(serviceName, "The serviceName argument must not be null");

        if (m_serviceMap.containsKey(serviceName)) {
            return m_serviceMap.get(serviceName).intValue();
        } else {
            log().debug("Could not find entry for '" + serviceName + "' in service name cache.  Looking up in database.");
            
            OnmsServiceType serviceType = m_serviceTypeDao.findByName(serviceName);
            if (serviceType == null) {
                log().debug("Did not find entry for '" + serviceName + "' in database.");
                return -1;
            }
            
            log().debug("Found entry for '" + serviceName + "' (ID " + serviceType.getId() + " in database.  Adding to service name cache.");

            m_serviceMap.put(serviceType.getName(), serviceType.getId());
            
            return serviceType.getId();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventdServiceManager#dataSourceSync()
     */
    public synchronized void dataSourceSync() {
        m_serviceMap.clear();
        
        for (OnmsServiceType serviceType : m_serviceTypeDao.findAll()) {
            m_serviceMap.put(serviceType.getName(), serviceType.getId());
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_serviceTypeDao != null, "property serviceTypeDao must be set");
    }

    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }

    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }
}

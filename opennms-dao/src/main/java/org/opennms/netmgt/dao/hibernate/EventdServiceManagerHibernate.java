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
package org.opennms.netmgt.dao.hibernate;

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
 * <p>EventdServiceManagerHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Transactional
public class EventdServiceManagerHibernate implements InitializingBean, EventdServiceManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(EventdServiceManagerHibernate.class);
    
    private ServiceTypeDao m_serviceTypeDao;

    /**
     * Cache of service names to service IDs.
     */
    private Map<String, Integer> m_serviceMap = new HashMap<String, Integer>();

    /**
     * <p>Constructor for EventdServiceManagerHibernate.</p>
     */
    public EventdServiceManagerHibernate() {
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

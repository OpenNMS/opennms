/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.internal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.opennms.netmgt.bsm.persistence.api.BusinessService;
import org.opennms.netmgt.bsm.service.BusinessServiceStateChangeHandler;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBusinessServiceStateMachine implements BusinessServiceStateMachine {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBusinessServiceStateMachine.class);

    private ReadWriteLock m_rwLock = new ReentrantReadWriteLock();
    public static final OnmsSeverity DEFAULT_SEVERITY = OnmsSeverity.NORMAL;

    private final List<BusinessServiceStateChangeHandler> m_handlers = Lists.newArrayList();
    private final Map<String, Set<BusinessService>> m_reductionKeys = Maps.newHashMap();
    private final Map<BusinessService, OnmsSeverity> m_businessServiceSeverity = Maps.newHashMap();
    private final Map<String, OnmsSeverity> m_reductionKeyToSeverity = Maps.newHashMap();
    private final Set<Integer> m_ipServiceIds = Sets.newHashSet();

    @Override
    public void setBusinessServices(List<BusinessService> businessServices) {
        Objects.requireNonNull(businessServices, "businessServices cannot be null");

        m_rwLock.writeLock().lock();
        try {
            // Clear previous state
            m_reductionKeys.clear();
            m_businessServiceSeverity.clear();
            m_reductionKeyToSeverity.clear();
            m_ipServiceIds.clear();

            // Rebuild
            for (BusinessService businessService : businessServices) {
                for (OnmsMonitoredService monitoredService : businessService.getIpServices()) {
                    m_ipServiceIds.add(monitoredService.getId());
                }
                m_businessServiceSeverity.put(businessService, DEFAULT_SEVERITY);
                for (String reductionKey : businessService.getAllReductionKeys()) {
                    addReductionKey(reductionKey, businessService);
                }
            }
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    private void addReductionKey(String reductionKey, BusinessService bs) {
        if (m_reductionKeys.containsKey(reductionKey)) {
            m_reductionKeys.get(reductionKey).add(bs);
        } else {
            m_reductionKeys.put(reductionKey, Sets.newHashSet(bs));
        }
    }

    @Override
    public void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
        // The ReadWriteLock doesn't give us the ability to upgrade from a
        // read lock to a write lock, so we acquire a write lock even
        // if we may not need it
        m_rwLock.writeLock().lock();
        try {
            // Are there any business services referencing this alarm?
            Set<BusinessService> affectedBusinessServices = m_reductionKeys.get(alarm.getReductionKey());
            if (affectedBusinessServices == null || affectedBusinessServices.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No Business Service depends on alarm with reduction key: '{}'. "
                            + "Monitored reduction keys include: {}.", alarm.getReductionKey(), m_reductionKeys.keySet());
                }
                return;
            }

            // Maintain the last known severity for the reduction key
            LOG.debug("Alarm with id: {} and reduction key: {} has severity: {}", alarm.getId(), alarm.getReductionKey(), alarm.getSeverity());
            m_reductionKeyToSeverity.put(alarm.getReductionKey(), alarm.getSeverity());

            // Propagate to the affected business services
            for (BusinessService businessService : affectedBusinessServices) {
                // Calculate the new severity
                OnmsSeverity newSeverity = calculateCurrentSeverity(businessService);

                // Did the severity change?
                OnmsSeverity prevSeverity = m_businessServiceSeverity.get(businessService);
                if (newSeverity.equals(prevSeverity)) {
                    // The severity hasn't changed, we're done
                    continue;
                }

                // Update the severity
                LOG.debug("Updating state on {} from {} to {}.", businessService, prevSeverity, newSeverity);
                m_businessServiceSeverity.put(businessService, newSeverity);

                // Notify
                synchronized(m_handlers) {
                    for (BusinessServiceStateChangeHandler handler : m_handlers) {
                        handler.handleBusinessServiceStateChanged(businessService, newSeverity, prevSeverity);
                    }
                }
            }
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    private OnmsSeverity calculateCurrentSeverity(BusinessService businessService) {
        OnmsSeverity maxSeverity = DEFAULT_SEVERITY;
        for (String reductionKey : businessService.getAllReductionKeys()) {
            final OnmsSeverity ipServiceSeverity = m_reductionKeyToSeverity.get(reductionKey);
            if (ipServiceSeverity != null && ipServiceSeverity.isGreaterThan(maxSeverity)) {
                maxSeverity = ipServiceSeverity;
            }
        }
        return maxSeverity;
    }

    @Override
    public OnmsSeverity getOperationalStatus(BusinessService businessService) {
        m_rwLock.readLock().lock();
        try {
            return m_businessServiceSeverity.get(businessService);
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    //TODO how does this method relate to reductionkeys on businessservices and ipservices on businessservices
    @Override
    public OnmsSeverity getOperationalStatus(OnmsMonitoredService ipService) {
        m_rwLock.readLock().lock();
        try {
            // Return null if the IP-Service is not associated with any Business Service
            if (!m_ipServiceIds.contains(ipService.getId())) {
                return null;
            }

            // The IP-Service resolves to multiple reduction keys, we use the one with the highest severity
            OnmsSeverity highestSeverity = DEFAULT_SEVERITY;
            for (String reductionKey : ipService.getReductionKeys()) {
                final OnmsSeverity severity = m_reductionKeyToSeverity.get(reductionKey);
                if (severity != null) {
                    if (highestSeverity == null) {
                        highestSeverity = severity;
                    } else if (severity.isGreaterThan(highestSeverity)) {
                        highestSeverity = severity;
                    }
                }
            }
            return highestSeverity;
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public List<BusinessService> getBusinessServices() {
        m_rwLock.readLock().lock();
        try {
            // Return a shallow copy
            return Lists.newArrayList(m_businessServiceSeverity.keySet());
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public void addHandler(BusinessServiceStateChangeHandler handler, Map<String, String> attributes) {
        m_rwLock.writeLock().lock();
        try {
            m_handlers.add(handler);
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeHandler(BusinessServiceStateChangeHandler handler, Map<String, String> attributes) {
        m_rwLock.writeLock().lock();
        try {
            return m_handlers.remove(handler);
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
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
    public static final OnmsSeverity MIN_SEVERITY = OnmsSeverity.NORMAL;

    private final List<BusinessServiceStateChangeHandler> m_handlers = Lists.newArrayList();
    private final Map<String, Set<BusinessServiceEntity>> m_reductionKeys = Maps.newHashMap();
    private final Map<BusinessServiceEntity, OnmsSeverity> m_businessServiceSeverity = Maps.newHashMap();
    private final Map<String, OnmsSeverity> m_reductionKeyToSeverity = Maps.newHashMap();
    private final Set<Integer> m_ipServiceIds = Sets.newHashSet();
    private final HashMap<Integer, Set<BusinessServiceEntity>> m_levelToBusinessServiceMapping = Maps.newHashMap();

    @Override
    public void setBusinessServices(List<BusinessServiceEntity> businessServices) {
        Objects.requireNonNull(businessServices, "businessServices cannot be null");

        m_rwLock.writeLock().lock();
        try {
            // Clear previous state
            m_reductionKeys.clear();
            m_businessServiceSeverity.clear();
            m_reductionKeyToSeverity.clear();
            m_ipServiceIds.clear();
            m_levelToBusinessServiceMapping.clear();

            // Rebuild the reduction Key set
            for (BusinessServiceEntity businessService : businessServices) {
                m_businessServiceSeverity.put(businessService, DEFAULT_SEVERITY);
                for (String reductionKey : businessService.getAllReductionKeys()) {
                    addReductionKey(reductionKey, businessService);
                }
            }

            // Rebuild the hierarchy
            List<BusinessServiceEntity> roots = getRoots(businessServices);
            determineHierarchyLevel(0, roots);
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    protected void determineHierarchyLevel(int level, List<BusinessServiceEntity> elements) {
        elements.forEach(bs -> {
            // elements can be children of multiple parents, we use the maximum level
            bs.setLevel(Math.max(bs.getLevel() == null ? 0 : bs.getLevel(), level));
            // Afterwards move to next level
            determineHierarchyLevel(level + 1, new ArrayList<>(bs.getChildServices()));
        });
    }

    protected List<BusinessServiceEntity> getRoots(List<BusinessServiceEntity> businessServiceEntities) {
        return businessServiceEntities
                .stream()
                .filter(eachService -> eachService.isRoot())
                .collect(Collectors.toList());
    }

    private void addReductionKey(String reductionKey, BusinessServiceEntity bs) {
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
            Set<BusinessServiceEntity> affectedBusinessServices = m_reductionKeys.get(alarm.getReductionKey());
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

            // Get the maximum level
            Integer maxLevel = affectedBusinessServices.stream().mapToInt(s -> s.getLevel()).max().getAsInt();
            // Propagate to the affected business services
            for (int eachLevel = maxLevel; eachLevel>=0; eachLevel--) {
                calculateStatus(eachLevel);
            }
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    private OnmsSeverity calculateCurrentSeverity(BusinessServiceEntity businessService) {
        OnmsSeverity maxSeverity = DEFAULT_SEVERITY;
        for (OnmsMonitoredService ipService : businessService.getIpServices()) {
            for (String reductionKey : getReductionKeysFor(ipService)) {
                final OnmsSeverity ipServiceSeverity = m_reductionKeyToSeverity.get(reductionKey);
                if (ipServiceSeverity != null && ipServiceSeverity.isGreaterThan(maxSeverity)) {
                    maxSeverity = ipServiceSeverity;
                }
                // Map
                edge.getMapFunction().map(severity).ifPresent(s -> severities.add(s));
            }
        }
        for (BusinessServiceEntity bs : businessService.getChildServices()) {
            final OnmsSeverity bsSeverity = m_businessServiceSeverity.get(bs);
            if (bsSeverity != null && bsSeverity.isGreaterThan(maxSeverity)) {
                maxSeverity = bsSeverity;
            }
        }
        return maxSeverity;
    }

    // calculates the status for all business services on a certain level
    private void calculateStatus(int level) {
        Set<BusinessServiceEntity> businessServiceEntities = m_businessServiceSeverity.keySet().stream().filter(bs -> bs.getLevel() == level).collect(Collectors.toSet());
        for (BusinessServiceEntity eachEntity : businessServiceEntities) {
            doBusinessServiceStatusCalculation(eachEntity);
        }
    }

    private void doBusinessServiceStatusCalculation(BusinessServiceEntity businessServiceEntity) {
        // Calculate the new severity
        OnmsSeverity newSeverity = calculateCurrentSeverity(businessServiceEntity);

        // Did the severity change?
        OnmsSeverity prevSeverity = m_businessServiceSeverity.get(businessServiceEntity);
        if (newSeverity.equals(prevSeverity)) {
            return; // The severity hasn't changed, we're done
        }

        // Update the severity
        LOG.debug("Updating state on {} from {} to {}.", businessServiceEntity, prevSeverity, newSeverity);
        m_businessServiceSeverity.put(businessServiceEntity, newSeverity);

        // Notify
        synchronized(m_handlers) {
            for (BusinessServiceStateChangeHandler handler : m_handlers) {
                handler.handleBusinessServiceStateChanged(businessServiceEntity, newSeverity, prevSeverity);
            }
        }
    }

    @Override
    public OnmsSeverity getOperationalStatus(BusinessServiceEntity businessService) {
        m_rwLock.readLock().lock();
        try {
            return m_businessServiceSeverity.get(businessService);
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

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
            /* TODO: FIXME: HACK: JW, MVR
            for (String reductionKey : getReductionKeysFor(ipService)) {
                final OnmsSeverity severity = m_reductionKeyToSeverity.get(reductionKey);
                if (severity != null && severity.isGreaterThan(highestSeverity)) {
                    highestSeverity = severity;
                }
            }
            */
            return highestSeverity;
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public List<BusinessServiceEntity> getBusinessServices() {
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

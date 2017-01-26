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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.BusinessServiceStateChangeHandler;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.BusinessServiceHierarchy;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DefaultBusinessServiceStateMachine implements BusinessServiceStateMachine {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBusinessServiceStateMachine.class);

    private ReadWriteLock m_rwLock = new ReentrantReadWriteLock();
    public static final Status DEFAULT_SEVERITY = Status.NORMAL;
    public static final Status MIN_SEVERITY = Status.NORMAL;

    private final List<BusinessServiceStateChangeHandler> m_handlers = Lists.newArrayList();
    private final Map<String, Set<BusinessService>> m_reductionKeys = Maps.newHashMap();
    private final Map<BusinessService, Status> m_businessServiceStatus = Maps.newHashMap();
    private final Map<String, Status> m_reductionKeyStatus = Maps.newHashMap();
    private final Set<Integer> m_ipServiceIds = Sets.newHashSet();
    private BusinessServiceHierarchy m_hierarchy = new BusinessServiceHierarchyImpl(Lists.newArrayList());


    @Override
    public void setBusinessServices(List<BusinessService> businessServices) {
        Objects.requireNonNull(businessServices, "businessServices cannot be null");

        m_rwLock.writeLock().lock();
        try {
            // Clear previous state
            m_reductionKeys.clear();
            m_businessServiceStatus.clear();
            m_reductionKeyStatus.clear();
            m_ipServiceIds.clear();

            // Rebuild the reduction Key set
            for (BusinessService businessService : businessServices) {
                for (IpServiceEdge ipServiceEdge : businessService.getIpServiceEdges()) {
                    m_ipServiceIds.add(ipServiceEdge.getIpService().getId());
                }
                m_businessServiceStatus.put(businessService, DEFAULT_SEVERITY);
                for (Edge edge : businessService.getEdges()) {
                    for (String eachRk : edge.getReductionKeys()) {
                        addReductionKey(eachRk, businessService);
                    }
                }
            }

            // Determine Roots
            Set<BusinessService> rootServices = BusinessServiceHierarchyUtils.getRoots(businessServices);
            // Rebuild hierarchy (set level information)
            BusinessServiceHierarchyUtils.updateHierarchyLevel(rootServices);
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
    public void handleNewOrUpdatedAlarm(AlarmWrapper alarmWrapper) {
        // The ReadWriteLock doesn't give us the ability to upgrade from a
        // read lock to a write lock, so we acquire a write lock even
        // if we may not need it
        m_rwLock.writeLock().lock();
        try {
            // Are there any business services referencing this alarm?
            Set<BusinessService> affectedBusinessServices = m_reductionKeys.get(alarmWrapper.getReductionKey());
            if (affectedBusinessServices == null || affectedBusinessServices.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No Business Service depends on alarm with reduction key: '{}'. "
                            + "Monitored reduction keys include: {}.", alarmWrapper.getReductionKey(), m_reductionKeys.keySet());
                }
                return;
            }

            // Maintain the last known status for the reduction key
            m_reductionKeyStatus.put(alarmWrapper.getReductionKey(), alarmWrapper.getStatus());

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

    private Status calculateCurrentStatus(BusinessService businessService) {
        // Map
        final Map<Edge, Status> edgeStatusMap = getStatusMapForReduceFunction(businessService);

        // Reduce
        final Status overallStatus = businessService.getReduceFunction().reduce(edgeStatusMap).orElse(DEFAULT_SEVERITY);

        // Apply lower bound, severity states like INDETERMINATE and CLEARED don't always make sense
        return overallStatus.isLessThan(MIN_SEVERITY) ? MIN_SEVERITY : overallStatus;
    }

    protected Map<Edge, Status> getStatusMapForReduceFunction(BusinessService businessService) {
        final Map<Edge, Status> statusMap = Maps.newHashMap();
        // reduction keys
        for (ReductionKeyEdge reductionKeyEdge : businessService.getReductionKeyEdges()) {
            final Status rkStatus = m_reductionKeyStatus.get(reductionKeyEdge.getReductionKey());
            statusMap.put(reductionKeyEdge, rkStatus);
        }
        // ip services
        for (IpServiceEdge ipServiceEdge : businessService.getIpServiceEdges()) {
            final Status ipServiceStatus = getOperationalStatus(ipServiceEdge.getIpService());
            statusMap.put(ipServiceEdge, ipServiceStatus);
        }
        // business services child edges
        for (ChildEdge childEdge : businessService.getChildEdges()) {
            final Status bsStatus = m_businessServiceStatus.get(childEdge.getChild());
            statusMap.put(childEdge, bsStatus);
        }
        // for now we throw an exception.
        if (statusMap.size() != businessService.getEdges().size()) {
            throw new IllegalStateException("Determining the status map for the reduction function failed. Expected " +
                    businessService.getEdges().size() + " but got " + statusMap.size() + " mappings");
        }
        // map
        for (Edge eachEdge : businessService.getEdges()) {
            Optional<Status> mappedStatus = eachEdge.getMapFunction().map(statusMap.get(eachEdge));
            statusMap.put(eachEdge, mappedStatus.orElse(Status.INDETERMINATE));
        }
        return statusMap;
    }

    // calculates the status for all business services on a certain level
    private void calculateStatus(int level) {
        Set<BusinessService> businessServiceEntities = m_businessServiceStatus.keySet().stream().filter(bs -> bs.getLevel() == level).collect(Collectors.toSet());
        for (BusinessService eachEntity : businessServiceEntities) {
            doBusinessServiceStatusCalculation(eachEntity);
        }
    }

    private void doBusinessServiceStatusCalculation(BusinessService businessService) {
        // Calculate the new status
        Status newStatus = calculateCurrentStatus(businessService);

        // Did the severity change?
        Status prevStatus = m_businessServiceStatus.get(businessService);
        if (newStatus.equals(prevStatus)) {
            return; // The status hasn't changed, we're done
        }

        // Update the severity
        LOG.debug("Updating state on {} from {} to {}.", businessService, prevStatus, newStatus);
        m_businessServiceStatus.put(businessService, newStatus);

        // Notify
        synchronized(m_handlers) {
            for (BusinessServiceStateChangeHandler handler : m_handlers) {
                handler.handleBusinessServiceStateChanged(businessService, newStatus, prevStatus);
            }
        }
    }

    @Override
    public Status getOperationalStatus(BusinessService businessService) {
        m_rwLock.readLock().lock();
        try {
            return m_businessServiceStatus.get(businessService);
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public Status getOperationalStatus(IpService ipService) {
        m_rwLock.readLock().lock();
        try {
            // Return null if the IP-Service is not associated with any Business Service
            if (!m_ipServiceIds.contains(ipService.getId())) {
                return null;
            }

            // The IP-Service resolves to multiple reduction keys, we use the one with the highest severity (Most Critical)
            Status maxStatus = DEFAULT_SEVERITY;
            for (String reductionKey : ipService.getReductionKeys()) {
                final Status rkStatus = m_reductionKeyStatus.get(reductionKey);
                if (rkStatus != null && rkStatus.isGreaterThan(maxStatus)) {
                    maxStatus = rkStatus;
                }
            }
            return maxStatus;
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public Status getOperationalStatus(String reductionKey) {
        m_rwLock.readLock().lock();
        try {
            return m_reductionKeyStatus.get(reductionKey);
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

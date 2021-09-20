/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.dao.api.FilterWatcher;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.dao.api.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

/**
 * Maintains sessions for each service being tracked.
 *
 * Most of the work is delegated to the {@link FilterWatcher}: when the results of a filter change, we re-evaluate
 * the state and issue callback accordingly.
 *
 * @author jwhite
 */
public class DefaultServiceTracker implements ServiceTracker {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultServiceTracker.class);

    @Autowired
    private FilterWatcher filterWatcher;

    private final List<TrackingSession> trackingSessions = new LinkedList<>();

    @Override
    public Closeable trackServiceMatchingFilterRule(String serviceName, String filterRule, ServiceListener listener) {
        if (StringUtils.isEmpty(serviceName)) {
            throw new IllegalArgumentException("Service name is required, but was given: " + serviceName);
        }

        final TrackingSession trackingSession;
        synchronized (trackingSessions) {
            trackingSession = new TrackingSession(serviceName, filterRule, listener);
            trackingSessions.add(trackingSession);
        }

        return trackingSession;
    }

    @Override
    public Closeable trackService(String serviceName, ServiceListener listener) {
        return trackServiceMatchingFilterRule(serviceName, null, listener);
    }

    private class TrackingSession implements Closeable {
        private final String serviceName;
        private final String filterRule;
        private final Closeable filterSession;
        private final ServiceListener listener;

        private final Set<ServiceRef> activeServices = new HashSet<>();

        public TrackingSession(String serviceName, String filterRule, ServiceListener listener) {
            this.serviceName = Objects.requireNonNull(serviceName);
            this.listener = Objects.requireNonNull(listener);
            this.filterRule = filterRule;
            // Ensure this is the last call in the constructor since it may issue the callback immediately
            // from this calling thread
            this.filterSession = filterWatcher.watch(filterRule, this::onFilterChanged);
        }

        private void onFilterChanged(FilterWatcher.FilterResults results) {
            Set<ServiceRef> candidateServices = results.getServicesNamed(serviceName);
            Set<ServiceRef> servicesToAdd = Sets.difference(candidateServices, activeServices);
            Set<ServiceRef> servicesToRemove = Sets.difference(activeServices, candidateServices);

            for (ServiceRef service : servicesToAdd) {
                activeServices.add(service);
                listener.onServiceMatched(service);
            }
            for (ServiceRef service : servicesToRemove) {
                activeServices.remove(service);
                listener.onServiceStoppedMatching(service);
            }
        }

        @Override
        public void close() {
            synchronized (trackingSessions) {
                trackingSessions.remove(this);

                try {
                    filterSession.close();
                } catch (IOException e) {
                    LOG.warn("Error closing session for filter rule: {}. Some resources may not be cleaned up properly.", filterRule);
                }
            }
        }
    }

    public void setFilterWatcher(FilterWatcher filterWatcher) {
        this.filterWatcher = filterWatcher;
    }
}

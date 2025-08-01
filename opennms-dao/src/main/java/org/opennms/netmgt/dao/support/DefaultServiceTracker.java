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

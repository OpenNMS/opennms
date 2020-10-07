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

package org.opennms.netmgt.perspectivepoller;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ServicePerspective;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

@EventListener(name = "PerspectiveServiceTracker")
public class PerspectiveServiceTracker implements DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(PerspectiveServiceTracker.class);

    public static final String REFRESH_RATE_LIMIT_PROPERTY = "org.opennms.netmgt.perspectivepoller.trackerRefreshRateLimit";
    private static final long REFRESH_RATE_LIMIT_MS = SystemProperties.getLong(REFRESH_RATE_LIMIT_PROPERTY, TimeUnit.SECONDS.toMillis(30));

    public interface Listener {
        void onServicePerspectiveAdded(final ServicePerspectiveRef servicePerspective, final ServicePerspective entity);
        void onServicePerspectiveRemoved(final ServicePerspectiveRef servicePerspective);
    }

    public static class ServicePerspectiveRef {
        private final int nodeId;
        private final InetAddress ipAddress;
        private final String serviceName;
        private final String perspectiveLocation;

        public ServicePerspectiveRef(final int nodeId,
                                     final InetAddress ipAddress,
                                     final String serviceName,
                                     final String perspectiveLocation) {
            this.nodeId = Objects.requireNonNull(nodeId);
            this.ipAddress = Objects.requireNonNull(ipAddress);
            this.serviceName = Objects.requireNonNull(serviceName);
            this.perspectiveLocation = Objects.requireNonNull(perspectiveLocation);
        }

        public int getNodeId() {
            return this.nodeId;
        }

        public InetAddress getIpAddress() {
            return this.ipAddress;
        }

        public String getServiceName() {
            return this.serviceName;
        }

        public String getPerspectiveLocation() {
            return this.perspectiveLocation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ServicePerspectiveRef)) {
                return false;
            }
            final ServicePerspectiveRef that = (ServicePerspectiveRef) o;
            return Objects.equals(this.nodeId, that.nodeId) &&
                   Objects.equals(this.ipAddress, that.ipAddress) &&
                   Objects.equals(this.serviceName, that.serviceName) &&
                   Objects.equals(this.perspectiveLocation, that.perspectiveLocation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.nodeId, this.ipAddress, this.serviceName, this.perspectiveLocation);
        }

        public static ServicePerspectiveRef from(final ServicePerspective servicePerspective) {
            return new ServicePerspectiveRef(servicePerspective.getService().getNodeId(),
                                             servicePerspective.getService().getIpAddress(),
                                             servicePerspective.getService().getServiceName(),
                                             servicePerspective.getPerspectiveLocation().getLocationName());
        }
    }

    private final SessionUtils sessionUtils;

    private final ApplicationDao applicationDao;

    private final Set<Session> sessions = Sets.newHashSet();

    private final Timer timer = new Timer("PerspectiveServiceTracker-Timer");

    @Autowired
    public PerspectiveServiceTracker(final SessionUtils sessionUtils,
                                     final ApplicationDao applicationDao) {
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.applicationDao = Objects.requireNonNull(applicationDao);

        final long timerIntervalMs = Math.min(REFRESH_RATE_LIMIT_MS, TimeUnit.SECONDS.toMillis(5));
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                PerspectiveServiceTracker.this.update(false);
            }
        }, timerIntervalMs, timerIntervalMs);
    }

    public AutoCloseable track(final Listener listener) {
        final Session session = new Session(listener);
        session.update(true);

        return session;
    }

    @Override
    public void destroy() throws Exception {
        this.timer.cancel();
    }

    @EventHandler(ueis = {
            EventConstants.NODE_GAINED_SERVICE_EVENT_UEI,
            EventConstants.SERVICE_DELETED_EVENT_UEI,
            EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI,
            EventConstants.NODE_LOCATION_CHANGED_EVENT_UEI,
            EventConstants.NODE_ADDED_EVENT_UEI,
            EventConstants.NODE_DELETED_EVENT_UEI,
            EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI,
            EventConstants.INTERFACE_DELETED_EVENT_UEI,
            EventConstants.INTERFACE_REPARENTED_EVENT_UEI,
            EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI,
            EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI,
            EventConstants.APPLICATION_CHANGED_EVENT_UEI,
            EventConstants.APPLICATION_CREATED_EVENT_UEI,
            EventConstants.APPLICATION_DELETED_EVENT_UEI,
    })
    public void handleEvent(final IEvent event) {
        update(true);
    }

    private void update(final boolean dirty) {
        synchronized (PerspectiveServiceTracker.this.sessions) {
            try {
                PerspectiveServiceTracker.this.sessions.forEach(session -> session.update(dirty));
            } catch (Exception e) {
                LOG.warn("Error refreshing service perspectives.", e);
            }
        }
    }

    private class Session implements AutoCloseable {

        private final Listener listener;

        private final Set<ServicePerspectiveRef> active = Sets.newHashSet();

        private Instant lastRefresh = Instant.MIN;
        private boolean dirty = true;

        public Session(final Listener listener) {
            this.listener = Objects.requireNonNull(listener);

            synchronized (PerspectiveServiceTracker.this.sessions) {
                PerspectiveServiceTracker.this.sessions.add(this);
            }
        }

        @Override
        public void close() throws Exception {
            synchronized (PerspectiveServiceTracker.this.sessions) {
                PerspectiveServiceTracker.this.sessions.remove(this);
            }
        }

        public synchronized void update(final boolean dirty) {
            // Mark as dirty if requested
            this.dirty |= dirty;

            // If still not marked as dirty, there is nothing to do
            if (!this.dirty) {
                return;
            }

            // Check if it's time to refresh
            final Instant now = Instant.now();
            if (this.lastRefresh.isAfter(now.minusMillis(PerspectiveServiceTracker.REFRESH_RATE_LIMIT_MS))) {
                return;
            }

            // Refresh the service list
            PerspectiveServiceTracker.this.sessionUtils.withTransaction(() -> {
                final Map<ServicePerspectiveRef, ServicePerspective> candidates = PerspectiveServiceTracker.this.applicationDao.getServicePerspectives().stream()
                                                                                                                               .collect(Collectors.toMap(ServicePerspectiveRef::from, Function.identity()));

                final Set<ServicePerspectiveRef> current = Sets.newHashSet(this.active);
                final Set<ServicePerspectiveRef> additions = Sets.difference(candidates.keySet(), current);
                final Set<ServicePerspectiveRef> removals = Sets.difference(current, candidates.keySet());

                for (final ServicePerspectiveRef servicePerspective : additions) {
                    try {
                        this.listener.onServicePerspectiveAdded(servicePerspective, candidates.get(servicePerspective));
                        this.active.add(servicePerspective);
                    } catch (final Exception e) {
                        LOG.error("Adding service failed", e);
                    }
                }

                for (final ServicePerspectiveRef servicePerspective : removals) {
                    try {
                        this.listener.onServicePerspectiveRemoved(servicePerspective);
                        this.active.remove(servicePerspective);
                    } catch (final Exception e) {
                        LOG.error("Adding service failed", e);
                    }
                }
            });

            // Not dirty anymore after a successful refresh
            this.lastRefresh = now;
            this.dirty = false;
        }
    }
}

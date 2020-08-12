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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.ServiceTracker;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.MoreObjects;

@EventListener(name = "ServiceTracker")
public class DefaultServiceTracker implements ServiceTracker {

    private static final String MATCH_ALL_FILTER_RULE = "IPADDR != '0.0.0.0'";

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private SessionUtils sessionUtils;

    private final Map<String, List<TrackingSession>> sessionsByServiceName = new HashMap<>();

    @Override
    public Session watchServicesMatchingFilter(String serviceName, String filterRule, NodeInterfaceUpdateListener listener) {
        if (StringUtils.isEmpty(serviceName)) {
            throw new IllegalArgumentException("Service name is required, but was given: " + serviceName);
        }

        String effectiveFilterRule = filterRule;
        if (StringUtils.isEmpty(filterRule)) {
            // No filter rule, match any
            effectiveFilterRule = MATCH_ALL_FILTER_RULE;
        } else {
            filterDao.validateRule(filterRule);
        }

        synchronized (sessionsByServiceName) {
            TrackingSession session = new TrackingSession(serviceName, effectiveFilterRule, listener);
            sessionsByServiceName.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(session);

            sessionUtils.withReadOnlyTransaction(() -> {
                // IP interfaces for the service name
                List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findByServiceType(serviceName);

                if (!ipInterfaces.isEmpty()) {
                    // Evaluate the filter expression
                    Map<Integer, String> nodesMatchingFilter = filterDao.getNodeMap(filterRule);
                    Set<Integer> nodeIdsMatchingFilter = nodesMatchingFilter.keySet();
                    // Only consider IP interfaces for nodes that matched the filter expression
                    ipInterfaces = ipInterfaces.stream()
                            .filter(ipInterface -> nodeIdsMatchingFilter.contains(ipInterface.getNodeId()))
                            .collect(Collectors.toList());
                    // FIXME: We should really be matching *both* the node and interface returned the filter
                }

                for (OnmsIpInterface ipInterface : ipInterfaces) {
                    listener.onInterfaceMatchedFilter(new NodeInterfaceImpl(ipInterface));
                }
                return null;
            });

            return session;
        }
    }

    @Override
    public Session watchServices(String serviceName, NodeInterfaceUpdateListener listener) {
        return watchServicesMatchingFilter(serviceName, MATCH_ALL_FILTER_RULE, listener);
    }

    private List<TrackingSession> getSessionsWatchingService(Service service) {
        return sessionsByServiceName.getOrDefault(service.getServiceName(), Collections.emptyList());
    }

    @EventHandler(ueis = {EventConstants.NODE_GAINED_SERVICE_EVENT_UEI})
    public void nodeGainedServiceHandler(final IEvent event) {
        final Service service = Service.fromEvent(event);
        final List<TrackingSession> sessionsWatchingService = getSessionsWatchingService(service);
        if (sessionsWatchingService.isEmpty()) {
            // The services is not being watched by any of the session, noop
            return;
        }

        for (TrackingSession session : sessionsWatchingService) {
            session.onNewOrUpdatedService(service);
        }
    }

    @EventHandler(ueis = {EventConstants.NODE_LOST_SERVICE_EVENT_UEI})
    public void nodeLostServiceHandler(final IEvent event) {
        final Service service = Service.fromEvent(event);
        final List<TrackingSession> sessionsWatchingService = getSessionsWatchingService(service);
        if (sessionsWatchingService.isEmpty()) {
            // The services is not being watched by any of the session, noop
            return;
        }

        for (TrackingSession session : sessionsWatchingService) {
            session.onDeletedService(service);
        }
    }

    private void onNodeChangeEvent() {
        // Filters can depend on arbitrary node fields & relationships so we need to refresh these periodically

        // Are there any filters being used?

        // Re-evaluate filter immediately if it has been > 1 minute since we last refreshed
        // Otherwise schedule another run in (1 min - X) to have a dampening effect

        // https://github.com/resilience4j/resilience4j#ratelimiter
    }

    private class TrackingSession implements Session {
        private final String serviceName;
        private final String filterRule;
        private final NodeInterfaceUpdateListener listener;

        private final Set<Service> activeServices = new HashSet<>();

        public TrackingSession(String serviceName, String filterRule, NodeInterfaceUpdateListener listener) {
            this.serviceName = serviceName;
            this.filterRule = filterRule;
            this.listener = listener;
        }

        public synchronized void onNewOrUpdatedService(Service service) {
            // Is this service already considered to be active?
            if (activeServices.contains(service)) {
                // The listener already knows about the service, nothing to do
                return;
            }

            // Does this service match the session criteria?
            // FIXME

            activeServices.add(service);
            listener.onInterfaceMatchedFilter(new NodeInterfaceImpl(service));
        }

        public synchronized void onDeletedService(Service service) {
            // Is this service already considered to be active?
            if (!activeServices.remove(service)) {
                // The listener doesn't know about this service, nothing to do
                return;
            }

            listener.onInterfaceStoppedMatchingFilter(new NodeInterfaceImpl(service));
        }

        @Override
        public synchronized void close() {
            synchronized (sessionsByServiceName) {
                sessionsByServiceName.getOrDefault(serviceName, Collections.emptyList()).remove(this);
            }
        }
    }

    private static class NodeInterfaceImpl implements NodeInterface {
        private final int nodeId;
        private final InetAddress ipAddress;

        public NodeInterfaceImpl(OnmsIpInterface ipInterface) {
            this(ipInterface.getNodeId(), ipInterface.getIpAddress());
        }

        public NodeInterfaceImpl(Service service) {
            this(service.nodeId, service.ipAddress);
        }

        public NodeInterfaceImpl(int nodeId, InetAddress ipAddress) {
            this.nodeId = nodeId;
            this.ipAddress = Objects.requireNonNull(ipAddress);
        }

        @Override
        public int getNodeId() {
            return nodeId;
        }

        @Override
        public InetAddress getInterfaceAddress() {
            return ipAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NodeInterfaceImpl)) return false;
            NodeInterfaceImpl that = (NodeInterfaceImpl) o;
            return nodeId == that.nodeId &&
                    Objects.equals(ipAddress, that.ipAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId, ipAddress);
        }
    }


    private static class Node {
        public final int nodeId;

        public Node(final int nodeId) {
            this.nodeId = nodeId;
        }

        public static Node fromEvent(final IEvent event) {
            final int nodeId = event.getNodeid().intValue();

            return new Node(nodeId);
        }

        public Interface iface(final InetAddress ipAddress) {
            return new Interface(this.nodeId, ipAddress);
        }

        public int getNodeId() {
            return this.nodeId;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("nodeId", this.nodeId)
                    .toString();
        }
    }

    private static class Interface {
        public final int nodeId;
        public final InetAddress ipAddress;

        public Interface(final int nodeId, final InetAddress ipAddress) {
            this.nodeId = nodeId;
            this.ipAddress = Objects.requireNonNull(ipAddress);
        }

        public static Interface fromEvent(final IEvent event) {
            final int nodeId = event.getNodeid().intValue();
            final String ipAddress = event.getInterface();

            return new Interface(nodeId, InetAddressUtils.addr(ipAddress));
        }

        public Node node() {
            return new Node(this.nodeId);
        }

        public Service service(final String serviceName) {
            return new Service(this.nodeId, this.ipAddress, serviceName);
        }

        public int getNodeId() {
            return this.nodeId;
        }

        public InetAddress getIpAddress() {
            return this.ipAddress;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("nodeId", this.nodeId)
                    .add("address", this.ipAddress)
                    .toString();
        }
    }

    public static class Service {
        private final int nodeId;
        private final InetAddress ipAddress;
        private final String serviceName;

        public Service(final int nodeId,
                       final InetAddress ipAddress,
                       final String serviceName) {
            this.nodeId = nodeId;
            this.ipAddress = Objects.requireNonNull(ipAddress);
            this.serviceName = Objects.requireNonNull(serviceName);
        }

        public static Service fromEvent(final IEvent event) {
            final int nodeId = event.getNodeid().intValue();
            final String ipAddress = event.getInterface();
            final String serviceName = event.getService();

            return new Service(nodeId, InetAddressUtils.addr(ipAddress), serviceName);
        }

        public Node node() {
            return new Node(this.nodeId);
        }

        public Interface iface() {
            return new Interface(this.nodeId, this.ipAddress);
        }

        public int getNodeId() {
            return this.nodeId;
        }

        public InetAddress getIpAddress() {
            return this.ipAddress;
        }

        public String getServiceName() {
            return serviceName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Service)) {
                return false;
            }

            final Service service = (Service) o;
            return Objects.equals(this.nodeId, service.nodeId) &&
                    Objects.equals(this.ipAddress, service.ipAddress) &&
                    Objects.equals(this.serviceName, service.serviceName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.nodeId, this.ipAddress, this.serviceName);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("nodeId", this.nodeId)
                    .add("address", this.ipAddress)
                    .add("serviceName", this.serviceName)
                    .toString();
        }
    }

    public void setFilterDao(FilterDao filterDao) {
        this.filterDao = filterDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        this.ipInterfaceDao = ipInterfaceDao;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }
}

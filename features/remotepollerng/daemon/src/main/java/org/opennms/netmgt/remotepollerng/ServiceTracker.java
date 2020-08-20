/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.remotepollerng;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.ThreadAwareEventListener;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@EventListener(name = "remotepollerd")
public final class ServiceTracker<E> implements ThreadAwareEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceTracker.class);

    private final PollerConfig config;

    private final QueryManager queryManager;

    private final Network<E> network;

    private final Function<Service, Optional<E>> filterService;

    private final Consumer<ServiceEntry<E>> addService;
    private final Consumer<ServiceEntry<E>> deleteService;

    public ServiceTracker(final PollerConfig config,
                          final QueryManager queryManager,
                          final Function<Service, Optional<E>> filterService,
                          final Consumer<ServiceEntry<E>> addService,
                          final Consumer<ServiceEntry<E>> deleteService) {
        this.config = Objects.requireNonNull(config);
        this.queryManager = Objects.requireNonNull(queryManager);
        this.network = new Network<>();
        this.filterService = Objects.requireNonNull(filterService);
        this.addService = Objects.requireNonNull(addService);
        this.deleteService = Objects.requireNonNull(deleteService);
    }

    public void start() {
        // Build initial network
        for (final Service service : this.queryManager.findServices()) {
            final Optional<E> element = this.filterService.apply(service);
            if (!element.isPresent()) { // TODO fooker: this smells
                continue;
            }

            if (this.network.add(service, element.get())) {
                this.addService.accept(new ServiceEntry<>(service, element.get())); // TODO fooker: this smells
            } else {
                LOG.debug("Service already known: {}", service);
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public int getNumThreads() {
        // TODO fooker: Make this configurable as in Alarmd?
        return 1;
    }

    private void rescheduleNodeServices(final IEvent event, final boolean rescheduleExisting) {
        final Node node = Node.fromEvent(event);

        this.config.rebuildPackageIpListMap();
        this.serviceReschedule(node, rescheduleExisting);
    }

    public void rescheduleAllServices() {
        this.config.rebuildPackageIpListMap();

        this.network.nodes().forEach(node -> this.serviceReschedule(node, true));
    }

    public void rescheduleService(final Service service) {
        this.network.remove(service)
                    .ifPresent(this.deleteService);

        this.filterService.apply(service).ifPresent(element -> {
            if (this.network.add(service, element)) {
                this.addService.accept(new ServiceEntry<>(service, element));
            }
        });
    }

    private void serviceReschedule(final Node node,
                                   final boolean rescheduleExisting) {
        final Set<Service> databaseServices = Sets.newHashSet(this.queryManager.findServicesByNode(node));
        final Set<Service> trackedServices = this.network.findByNode(node).map(ServiceEntry::getService).collect(Collectors.toSet());

        // Remove the services being tracked but not in database
        for (final Service service : Sets.difference(trackedServices, databaseServices)) {
            this.network.remove(service)
                        .ifPresent(this.deleteService);
        }

        // Remove remaining services if existing services should be rescheduled
        if (rescheduleExisting) {
            for (final Service service : trackedServices) {
                this.network.remove(service)
                            .ifPresent(this.deleteService);
            }
        }

        // Add all services from database back to scheduling
        for (final Service service : databaseServices) {
            final Optional<E> element = this.filterService.apply(service);
            if (!element.isPresent()) { // TODO fooker: this smells
                continue;
            }

            if (this.network.add(service, element.get())) {
                this.addService.accept(new ServiceEntry<>(service, element.get())); // TODO fooker: this smells
            }
        }
    }

    @EventHandler(ueis = {EventConstants.NODE_GAINED_SERVICE_EVENT_UEI,
                          EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI})
    public void nodeGainedServiceHandler(final IEvent event) {
        final Service service = Service.fromEvent(event);

        final Optional<E> element = this.filterService.apply(service);
        if (!element.isPresent()) { // TODO fooker: this smells
            return;
        }

        if (!this.network.add(service, element.get())) {
            LOG.debug("Service already known: {}", service);
            return;
        }

        this.addService.accept(new ServiceEntry<>(service, element.get())); // TODO fooker: this smells
    }

    @EventHandler(uei = EventConstants.INTERFACE_REPARENTED_EVENT_UEI)
    public void interfaceReparentedHandler(final IEvent event) {
        final Node oldNode = new Node(Integer.parseInt(event.getParm(EventConstants.PARM_OLD_NODEID).getValue().getContent()));
        final Node newNode = new Node(Integer.parseInt(event.getParm(EventConstants.PARM_NEW_NODEID).getValue().getContent()));

        final InetAddress ipAddress = InetAddressUtils.addr(event.getInterface());

        final Interface oldInterface = oldNode.iface(ipAddress);
        final Interface newInterface = newNode.iface(ipAddress);

        // Remove interface from old node and add to new one
        this.network.remove(oldInterface).forEach(this.deleteService.andThen(oldEntry -> {
            final Service newService = newInterface.service(oldEntry.service.serviceName);

            final Optional<E> newElement = this.filterService.apply(newService);
            if (!newElement.isPresent()) { // TODO fooker: this smells
                return;
            }

            if (this.network.add(newService, newElement.get())) {
                LOG.debug("Service already known: {}", newService);
                return;
            }

            this.addService.accept(new ServiceEntry<>(newService, newElement.get())); // TODO fooker: this smells
        }));
    }

    @EventHandler(uei = EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI)
    public void nodeRemovePollableServiceHandler(final IEvent event) {
        final Service service = Service.fromEvent(event);

        final Optional<ServiceEntry<E>> entry = this.network.remove(service);
        if (!entry.isPresent()) { // TODO fooker: this smells
            LOG.debug("Service not know: {}", service);
            return;
        }

        entry.ifPresent(this.deleteService);

    }

    @EventHandler(ueis = {EventConstants.NODE_DELETED_EVENT_UEI,
                          EventConstants.DUP_NODE_DELETED_EVENT_UEI})
    public void nodeDeletedHandler(final IEvent event) {
        final Node node = Node.fromEvent(event);

        this.network.remove(node)
                    .forEach(this.deleteService);
    }

    @EventHandler(uei = EventConstants.INTERFACE_DELETED_EVENT_UEI)
    public void interfaceDeletedHandler(final IEvent event) {
        final Interface iface = Interface.fromEvent(event);

        this.network.remove(iface)
                    .forEach(this.deleteService);
    }

    @EventHandler(uei = EventConstants.SERVICE_DELETED_EVENT_UEI)
    public void serviceDeletedHandler(final IEvent event) {
        final Service service = Service.fromEvent(event);

        final Optional<ServiceEntry<E>> entry = this.network.remove(service);
        if (!entry.isPresent()) { // TODO fooker: this smells
            LOG.debug("Service not know: {}", service);
            return;
        }

        entry.ifPresent(this.deleteService);
    }

    // TODO fooker: do we care about scheduled outages?
//    @EventHandler(uei = EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI)
//    public void scheduledOutagesChangeHandler(final IEvent event) {
//        try {
//            this.config.update();
//            getPoller().getPollOutagesDao().reload();
//        } catch (Throwable e) {
//            LOG.error("Failed to reload PollerConfigFactory", e);
//        }
//        getPoller().refreshServicePackages();
//    }

    @EventHandler(uei = EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI)
    public void nodeCategoryMembershipChangedHandler(final IEvent event) {
        this.rescheduleNodeServices(event, false);
    }

    @EventHandler(uei = EventConstants.NODE_LOCATION_CHANGED_EVENT_UEI)
    public void nodeLocationChangedHandler(final IEvent event) {
        this.rescheduleNodeServices(event, true);
    }

    @EventHandler(uei = EventConstants.ASSET_INFO_CHANGED_EVENT_UEI)
    public void assetInfoChangedHandler(final IEvent event) {
        this.rescheduleNodeServices(event, false);
    }

    public interface QueryManager {
        List<Service> findServices();
        List<Service> findServicesByNode(final Node node);
    }

    private static class Network<E> {
        private final Map<Integer, Map<InetAddress, Map<String, E>>> services;

        private Network() {
            this.services = Maps.newConcurrentMap();
        }

        private static <E> Stream<ServiceEntry<E>> flatten(final Map<InetAddress, Map<String, E>> services, final Node node) {
            return services.entrySet().stream()
                           .flatMap(e -> flatten(e.getValue(), node.iface(e.getKey())));
        }

        private static <E> Stream<ServiceEntry<E>> flatten(final Map<String, E> services, final Interface iface) {
            return services.entrySet().stream()
                           .map(e -> new ServiceEntry<>(iface.service(e.getKey()), e.getValue()));
        }

        private Map<InetAddress, Map<String, E>> node(final Node node) {
            return this.services.computeIfAbsent(node.nodeId, key -> Maps.newConcurrentMap());
        }

        private Map<String, E> iface(final Interface iface) {
            return this.node(iface.node())
                       .computeIfAbsent(iface.ipAddress, key -> Maps.newConcurrentMap());
        }

        public Stream<Node> nodes() {
            return this.services.keySet().stream()
                                .map(Node::new);
        }

        public boolean add(final Service service, final E element) {
            return this.iface(service.iface())
                       .put(service.serviceName, element) == null;
        }

        public Stream<ServiceEntry<E>> remove(final Node node) {
            return flatten(this.services.remove(node.nodeId), node);
        }

        public Stream<ServiceEntry<E>> remove(final Interface iface) {
            return flatten(this.node(iface.node())
                               .remove(iface.ipAddress), iface);
        }

        public Optional<ServiceEntry<E>> remove(final Service service) {
            return Optional.ofNullable(this.iface(service.iface())
                                           .remove(service.serviceName))
                    .map(element -> new ServiceEntry<>(service, element));
        }

        public Stream<ServiceEntry<E>> findByNode(final Node node) {
            return flatten(this.node(node), node);
        }

        public Stream<ServiceEntry<E>> findByInterface(final Interface iface) {
            return flatten(this.iface(iface), iface);
        }
    }

    public static class Node {
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

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                              .add("nodeId", this.nodeId)
                              .toString();
        }
    }

    public static class Interface {
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

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                              .add("nodeId", this.nodeId)
                              .add("address", this.ipAddress)
                              .toString();
        }
    }

    public static class Service {
        public final int nodeId;
        public final InetAddress ipAddress;
        public final String serviceName;

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

    public static class ServiceEntry<E> {
        private final Service service;
        private final E element;

        public ServiceEntry(final Service service, final E element) {
            this.service = Objects.requireNonNull(service);
            this.element = Objects.requireNonNull(element);
        }

        public Service getService() {
            return this.service;
        }

        public E getElement() {
            return this.element;
        }
    }
}

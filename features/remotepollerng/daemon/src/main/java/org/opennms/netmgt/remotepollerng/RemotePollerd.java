/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.RemoteLatencyResource;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.daemon.DaemonTools;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.opennms.netmgt.poller.support.DefaultServiceMonitorRegistry;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.xml.event.Event;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.SchedulerListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@EventListener(name=RemotePollerd.NAME, logPrefix=RemotePollerd.LOG_PREFIX)
public class RemotePollerd implements SpringServiceDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(RemotePollerd.class);

    public static final String NAME = "RemotePollerNG";

    public static final String LOG_PREFIX = "remotepollerd";

    private static final ServiceMonitorRegistry serviceMonitorRegistry = new DefaultServiceMonitorRegistry();

    private final SessionUtils sessionUtils;
    private final MonitoringLocationDao monitoringLocationDao;
    private final PollerConfig pollerConfig;
    private final MonitoredServiceDao monitoredServiceDao;
    private final LocationAwarePollerClient locationAwarePollerClient;
    private final ApplicationDao applicationDao;
    private final CollectionAgentFactory collectionAgentFactory;
    private final PersisterFactory persisterFactory;
    private final EventForwarder eventForwarder;
    private final ThresholdingService thresholdingService;
    private final EventDao eventDao;
    private final OutageDao outageDao;
    private final TracerRegistry tracerRegistry;

    private final ServiceTracker<Set<RemotePolledService>> serviceTracker;

    @VisibleForTesting
    final Scheduler scheduler;

    public RemotePollerd(final SessionUtils sessionUtils,
                         final MonitoringLocationDao monitoringLocationDao,
                         final PollerConfig pollerConfig,
                         final MonitoredServiceDao monitoredServiceDao,
                         final LocationAwarePollerClient locationAwarePollerClient,
                         final ApplicationDao applicationDao,
                         final CollectionAgentFactory collectionAgentFactory,
                         final PersisterFactory persisterFactory,
                         final EventForwarder eventForwarder,
                         final ThresholdingService thresholdingService,
                         final EventDao eventDao,
                         final OutageDao outageDao,
                         final TracerRegistry tracerRegistry) throws SchedulerException {
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.monitoringLocationDao = Objects.requireNonNull(monitoringLocationDao);
        this.pollerConfig = Objects.requireNonNull(pollerConfig);
        this.monitoredServiceDao = Objects.requireNonNull(monitoredServiceDao);
        this.locationAwarePollerClient = Objects.requireNonNull(locationAwarePollerClient);
        this.applicationDao = Objects.requireNonNull(applicationDao);
        this.collectionAgentFactory = Objects.requireNonNull(collectionAgentFactory);
        this.persisterFactory = Objects.requireNonNull(persisterFactory);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
        this.thresholdingService = Objects.requireNonNull(thresholdingService);
        this.eventDao = Objects.requireNonNull(eventDao);
        this.outageDao = Objects.requireNonNull(outageDao);

        this.tracerRegistry = Objects.requireNonNull(tracerRegistry);
        this.tracerRegistry.init(SystemInfoUtils.getInstanceId());

        this.scheduler = new StdSchedulerFactory().getScheduler();

        this.serviceTracker = new ServiceTracker<>(pollerConfig,
                                                   new QueryManager(this.monitoredServiceDao),
                                                   this::filterService,
                                                   this::addService,
                                                   this::deleteService);
    }

    private Optional<Set<RemotePolledService>> filterService(final ServiceTracker.Service service) {
        return this.sessionUtils.withReadOnlyTransaction(() -> {
            // Get the monitored service entity
            final OnmsMonitoredService monitoredService = this.monitoredServiceDao.get(service.nodeId, service.ipAddress, service.serviceName);
            if (monitoredService == null) {
                return Optional.empty();
            }

            final OnmsIpInterface ipInterface = monitoredService.getIpInterface();
            final OnmsNode node = ipInterface.getNode();

            // Get all perspective locations from which the service is monitored via its assigned applications
            final List<OnmsMonitoringLocation> perspectiveLocations = this.applicationDao.getPerspectiveLocationsForService(service.nodeId, service.ipAddress, service.serviceName);
            if (perspectiveLocations.isEmpty()) {
                return Optional.empty();
            }

            // Get the polling package for the service
            this.pollerConfig.rebuildPackageIpListMap();

            final Package pkg = this.pollerConfig.getPackages().stream()
                                                 .filter(p -> this.pollerConfig.isInterfaceInPackage(InetAddressUtils.str(service.ipAddress), p) &&
                                                              this.pollerConfig.isServiceInPackageAndEnabled(service.serviceName, p))
                                                 .reduce((prev, curr) -> curr) // Take the last filtered element
                                                 .orElse(null);
            if (pkg == null) {
                return Optional.empty();
            }

            // Find the service (and the pattern parameters) for the service name
            final Optional<Package.ServiceMatch> serviceMatch = pkg.findService(service.serviceName);
            if (!serviceMatch.isPresent()) {
                return Optional.empty();
            }

            // Find the monitor implementation for the service name
            final ServiceMonitor serviceMonitor = this.pollerConfig.getServiceMonitor(serviceMatch.get().service.getName());
            if (serviceMonitor == null) {
                return Optional.empty();
            }

            final RrdRepository rrdRepository = new RrdRepository();
            rrdRepository.setStep(this.pollerConfig.getStep(pkg));
            rrdRepository.setHeartBeat(rrdRepository.getStep() * 2);
            rrdRepository.setRraList(this.pollerConfig.getRRAList(pkg));

            final String rrdRepositoryDir = getServiceParameter(serviceMatch.get().service, "rrd-repository");
            rrdRepository.setRrdBaseDir(new File(rrdRepositoryDir));

            // Create the thresholding session for this poller
            final ThresholdingSession thresholdingSession;
            try {
                thresholdingSession = this.thresholdingService.createSession(service.nodeId,
                                                                             InetAddressUtils.str(service.ipAddress),
                                                                             service.serviceName,
                                                                             rrdRepository,
                                                                             new ServiceParameters(Collections.emptyMap()));
            } catch (final ThresholdInitializationException e) {
                LOG.error("Failed to create thresholding session", e);
                return Optional.empty();
            }

            // Build remote polled services for each location
            return Optional.of(perspectiveLocations.stream()
                                                   .map(OnmsMonitoringLocation::getLocationName)
                                                   .map(perspectiveLocation -> new RemotePolledService(service,
                                                                                                       node.getForeignSource(),
                                                                                                       node.getForeignId(),
                                                                                                       node.getLabel(),
                                                                                                       pkg,
                                                                                                       serviceMatch.get(),
                                                                                                       serviceMonitor,
                                                                                                       perspectiveLocation,
                                                                                                       node.getLocation().getLocationName(),
                                                                                                       rrdRepository,
                                                                                                       thresholdingSession))
                                                   .collect(Collectors.toSet()));
        });
    }

    private void addService(final ServiceTracker.ServiceEntry<Set<RemotePolledService>> entry) {
        entry.getElement().forEach(remotePolledService -> {
            final JobKey key = buildJobKey(remotePolledService);

            final JobDetail job = JobBuilder
                    .newJob(RemotePollJob.class)
                    .withIdentity(key)
                    .setJobData(new JobDataMap(ImmutableMap.builder()
                                                           .put(RemotePollJob.POLLED_SERVICE, remotePolledService)
                                                           .put(RemotePollJob.REMOTE_POLLER_BACKEND, this)
                                                           .put(RemotePollJob.TRACER, this.tracerRegistry.getTracer())
                                                           .build()))
                    .build();

            final Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                                       .withIntervalInMilliseconds(remotePolledService.getServiceConfig().getInterval())
                                                       .repeatForever())
                    .build();

            LOG.debug("Scheduling service named {} at location {} with interval {}ms", remotePolledService.getServiceName(),
                      remotePolledService.getPerspectiveLocation(), remotePolledService.getServiceConfig().getInterval());

            try {
                this.scheduler.scheduleJob(job, trigger);
            } catch (final SchedulerException e) {
                LOG.error("Failed to schedule {} ({}).", remotePolledService, key, e);
            }
        });
    }

    private void deleteService(final ServiceTracker.ServiceEntry<Set<RemotePolledService>> entry) {
        entry.getElement().forEach(remotePolledService -> {
            final JobKey key = buildJobKey(remotePolledService);

            try {
                this.scheduler.deleteJob(key);
            } catch (final SchedulerException e) {
                LOG.error("Failed to un-schedule {} ({}).", remotePolledService, key, e);
            }
        });
    }

    @Override
    public void start() throws Exception {
        this.serviceTracker.start();

        this.scheduler.start();
        this.scheduler.getListenerManager().addSchedulerListener(new SchedulerListenerSupport() {
            @Override
            public void schedulerError(String msg, SchedulerException cause) {
                LOG.error("Unexpected error during poll: {}", msg, cause);
            }
        });

    }

    @Override
    public void destroy() throws Exception {
        if (this.scheduler != null) {
            this.scheduler.shutdown();
        }
    }

    public static JobKey buildJobKey(RemotePolledService remotePolledService) {
        return buildJobKey(remotePolledService.getNodeId(),
                           remotePolledService.getIpAddress(),
                           remotePolledService.getServiceName(),
                           remotePolledService.getPerspectiveLocation());
    }

    public static JobKey buildJobKey(final int nodeId, final InetAddress ipAddress, final String serviceName, final String perspectiveLocation) {
        final String name = String.format("%s-%s-%s", nodeId, InetAddressUtils.str(ipAddress), serviceName);
        return new JobKey(name, perspectiveLocation);
    }

    public LocationAwarePollerClient getLocationAwarePollerClient() {
        return locationAwarePollerClient;
    }

    protected void reportResult(final RemotePolledService polledService, final PollStatus pollResult) {
        // Update the status in the polled service
        if (!polledService.updateStatus(pollResult)) {
            // Nothing to do if status has not changed
            return;
        }

        final String uei = pollResult.isAvailable() ? EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI : EventConstants.REMOTE_NODE_LOST_SERVICE_UEI;

        final EventBuilder builder = new EventBuilder(uei, RemotePollerd.NAME);
        builder.addParam(EventConstants.PARM_LOCATION, polledService.getPerspectiveLocation());
        builder.setNodeid(polledService.getNodeId());
        builder.setInterface(polledService.getIpAddress());
        builder.setService(polledService.getServiceName());
        builder.addParam("perspective", polledService.getPerspectiveLocation());

        if (!pollResult.isAvailable() && pollResult.getReason() != null) {
            builder.addParam(EventConstants.PARM_LOSTSERVICE_REASON, pollResult.getReason());
        }

        this.eventForwarder.sendNow(builder.getEvent());
    }

    public void persistResponseTimeData(final RemotePolledService polledService, final PollStatus pollStatus) {
        String dsName = getServiceParameter(polledService.getServiceConfig(), "ds-name");
        if (dsName == null) {
            dsName = PollStatus.PROPERTY_RESPONSE_TIME;
        }

        String rrdBaseName = getServiceParameter(polledService.getServiceConfig(), "rrd-base-name");
        if (rrdBaseName == null) {
            rrdBaseName = dsName;
        }

        // Prefer ds-name over "response-time" for primary response-time value
        final Map<String, Number> properties = Maps.newHashMap(pollStatus.getProperties());
        if (!properties.containsKey(dsName) && properties.containsKey(PollStatus.PROPERTY_RESPONSE_TIME)) {
            properties.put(dsName, properties.get(PollStatus.PROPERTY_RESPONSE_TIME));
            properties.remove(PollStatus.PROPERTY_RESPONSE_TIME);
        }

        // Build collection agent
        final CollectionAgentDTO agent = new CollectionAgentDTO();
        agent.setAddress(polledService.getIpAddress());
        agent.setForeignId(polledService.getForeignId());
        agent.setForeignSource(polledService.getForeignSource());
        agent.setNodeId(polledService.getNodeId());
        agent.setNodeLabel(polledService.getNodeLabel());
        agent.setLocationName(polledService.getPerspectiveLocation());
        agent.setStorageResourcePath(ResourcePath.get(LocationUtils.isDefaultLocationName(polledService.getResidentLocation())
                                                      ? ResourcePath.get()
                                                      : ResourcePath.get(ResourcePath.sanitize(polledService.getResidentLocation())),
                                                      InetAddressUtils.str(polledService.getIpAddress())));
        agent.setStoreByForeignSource(false);

        // Create collection set from response times as gauges and persist
        final CollectionSetBuilder collectionSetBuilder = new CollectionSetBuilder(agent);
        final RemoteLatencyResource resource = new RemoteLatencyResource(polledService.getPerspectiveLocation(), InetAddressUtils.str(polledService.getIpAddress()), polledService.getServiceName());
        for (final Map.Entry<String, Number> e: properties.entrySet()) {
            final String key = PollStatus.PROPERTY_RESPONSE_TIME.equals(e.getKey())
                               ? dsName
                               : e.getKey();

            collectionSetBuilder.withGauge(resource, rrdBaseName, key, e.getValue());
        }

        final CollectionSetDTO collectionSetDTO = collectionSetBuilder.build();

        collectionSetDTO.visit(this.persisterFactory.createPersister(new ServiceParameters(Collections.emptyMap()),
                                                                         polledService.getRrdRepository(),
                                                                         false,
                                                                         true,
                                                                         true));

        try {
            polledService.getThresholdingSession().accept(collectionSetDTO);
        } catch (final Throwable e) {
            LOG.error("Failed to threshold on {} for {} because of an exception", polledService, dsName, e);
        }
    }

    private String getServiceParameter(final Service service, final String key) {
        for(final Parameter parm : this.pollerConfig.parameters(service)) {
            if (key.equals(parm.getKey())) {
                if (parm.getValue() != null) {
                    return parm.getValue();
                } else if (parm.getAnyObject() != null) {
                    return parm.getAnyObject().toString();
                }
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public ServiceTracker<?> getServiceTracker() {
        return this.serviceTracker;
    }

    private static class QueryManager implements ServiceTracker.QueryManager {
        private final MonitoredServiceDao monitoredServiceDao;

        private QueryManager(final MonitoredServiceDao monitoredServiceDao) {
            this.monitoredServiceDao = Objects.requireNonNull(monitoredServiceDao);
        }

        @Override
        public List<ServiceTracker.Service> findServices() {
            return this.monitoredServiceDao.findAllServices().stream()
                                           .map(QueryManager::asService)
                                           .collect(Collectors.toList());
        }

        @Override
        public List<ServiceTracker.Service> findServicesByNode(final ServiceTracker.Node node) {
            return this.monitoredServiceDao.findByNode(node.nodeId).stream()
                                           .map(QueryManager::asService)
                                           .collect(Collectors.toList());
        }

        private static ServiceTracker.Service asService(final OnmsMonitoredService service) {
            return new ServiceTracker.Service(service.getNodeId(),
                                              service.getIpAddress(),
                                              service.getServiceName());
        }
    }

    @EventHandler(ueis = { EventConstants.APPLICATION_CREATED_EVENT_UEI,
                           EventConstants.APPLICATION_CHANGED_EVENT_UEI,
                           EventConstants.APPLICATION_DELETED_EVENT_UEI })
    public void applicationEventHandler(final IEvent event) {
        final int applicationId = EventUtils.getIntParm(event, EventConstants.PARM_APPLICATION_ID, -1);
        if (applicationId == -1) {
            LOG.error("application ID missing in event: {}", event);
            return;
        }

        this.sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsApplication application = this.applicationDao.get(applicationId);
            if (application != null) {
                for (final OnmsMonitoredService service : application.getMonitoredServices()) {
                    this.serviceTracker.rescheduleService(new ServiceTracker.Service(service.getNodeId(), service.getIpAddress(), service.getServiceName()));
                }
            }

            // Reschedule everything in case it was removed from application
            this.serviceTracker.rescheduleAllServices();

            return null;
        });
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void reloadConfigHandler(final IEvent event) {
        DaemonTools.handleReloadEvent(event, RemotePollerd.NAME, (ev) ->  {
            try {
                this.pollerConfig.update();
            } catch (final IOException e) {
                LOG.error("Failed to load poller configuration", e);
            }

            this.serviceTracker.rescheduleAllServices();
        });
    }

    @EventHandler(uei = EventConstants.REMOTE_NODE_LOST_SERVICE_UEI)
    public void handleRemoteNodeLostService(final IEvent e) {
        if (e.hasNodeid() && e.getInterfaceAddress() != null && e.getService() != null && e.getParm("perspective") != null) {
            final OnmsEvent onmsEvent = eventDao.get(e.getDbid());
            final OnmsMonitoredService service = this.monitoredServiceDao.get(onmsEvent.getNodeId(), onmsEvent.getIpAddr(), onmsEvent.getServiceType().getId());
            final OnmsMonitoringLocation perspective = monitoringLocationDao.get(e.getParm("perspective").getValue().getContent());
            final OnmsOutage onmsOutage = new OnmsOutage(onmsEvent.getEventCreateTime(), onmsEvent, service);
            onmsOutage.setPerspective(perspective);
            outageDao.save(onmsOutage);

            final Event outageEvent = new EventBuilder(EventConstants.OUTAGE_CREATED_EVENT_UEI, "RemotePollerd")
                    .setNodeid(onmsEvent.getNodeId())
                    .setInterface(onmsEvent.getIpAddr())
                    .setService(service.getServiceName())
                    .setTime(onmsEvent.getEventCreateTime())
                    .setParam("perspective", perspective.getLocationName())
                    .getEvent();
            eventForwarder.sendNow(outageEvent);
        } else {
            LOG.warn("Received incomplete {} event: {}", EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, e);
        }
    }

    @EventHandler(uei = EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI)
    public void handleRemoteNodeGainedService(final IEvent e) {
        if (e.hasNodeid() && e.getInterfaceAddress() != null && e.getService() != null && e.getParm("perspective") != null) {
            final OnmsEvent onmsEvent = eventDao.get(e.getDbid());
            final OnmsMonitoredService service = this.monitoredServiceDao.get(onmsEvent.getNodeId(), onmsEvent.getIpAddr(), onmsEvent.getServiceType().getId());
            final OnmsMonitoringLocation perspective = monitoringLocationDao.get(e.getParm("perspective").getValue().getContent());

            final Criteria criteria = new CriteriaBuilder(OnmsOutage.class)
                    .eq("perspective", perspective)
                    .isNull("serviceRegainedEvent")
                    .isNull("ifRegainedService")
                    .eq("monitoredService", service).toCriteria();

            final List<OnmsOutage> onmsOutages = outageDao.findMatching(criteria);

            if (onmsOutages.size() == 1) {
                final OnmsOutage onmsOutage = onmsOutages.get(0);
                onmsOutage.setIfRegainedService(onmsEvent.getEventCreateTime());
                onmsOutage.setServiceRegainedEvent(onmsEvent);
                outageDao.update(onmsOutage);

                final Event outageEvent = new EventBuilder(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, "RemotePollerd")
                        .setNodeid(onmsEvent.getNodeId())
                        .setInterface(onmsEvent.getIpAddr())
                        .setService(service.getServiceName())
                        .setTime(onmsEvent.getEventCreateTime())
                        .setParam("perspective", perspective.getLocationName())
                        .getEvent();
                eventForwarder.sendNow(outageEvent);
            } else {
                LOG.warn("Found more than one outages for {} event: {}", EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, e);
            }
        } else {
            LOG.warn("Received incomplete {} event: {}", EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, e);
        }
    }
}

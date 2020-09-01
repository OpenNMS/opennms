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
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import org.opennms.netmgt.collection.support.builder.PerspectiveResponseTimeResource;
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
import org.opennms.netmgt.dao.api.ServicePerspective;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.events.EventBuilder;
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
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@EventListener(name=RemotePollerd.NAME, logPrefix=RemotePollerd.LOG_PREFIX)
public class RemotePollerd implements SpringServiceDaemon, PerspectiveServiceTracker.Listener {
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

    private final PerspectiveServiceTracker tracker;

    @VisibleForTesting
    Scheduler scheduler;

    private AutoCloseable trackerSession;

    @Autowired
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
                         final TracerRegistry tracerRegistry,
                         final PerspectiveServiceTracker tracker) throws SchedulerException {
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

        this.tracker = Objects.requireNonNull(tracker);
    }

    @Override
    public void start() throws Exception {
        this.scheduler = new StdSchedulerFactory().getScheduler();
        this.scheduler.start();
        this.scheduler.getListenerManager().addSchedulerListener(new SchedulerListenerSupport() {
            @Override
            public void schedulerError(String msg, SchedulerException cause) {
                LOG.error("Unexpected error during poll: {}", msg, cause);
            }
        });

        this.trackerSession = this.tracker.track(this);
    }

    @Override
    public void destroy() throws Exception {
        this.trackerSession.close();
        this.trackerSession = null;

        this.scheduler.shutdown();
        this.scheduler = null;
    }

    @Override
    public void onServicePerspectiveAdded(final PerspectiveServiceTracker.ServicePerspectiveRef servicePerspective, final ServicePerspective entity) {
        final JobKey key = buildJobKey(servicePerspective);

        final OnmsMonitoredService service = entity.getService();
        final OnmsIpInterface ipInterface = service.getIpInterface();
        final OnmsNode node = ipInterface.getNode();

        // Get the polling package for the service
        this.pollerConfig.rebuildPackageIpListMap();

        final Package pkg = this.pollerConfig.getPackages().stream()
                                             .filter(p -> this.pollerConfig.isInterfaceInPackage(InetAddressUtils.str(service.getIpAddress()), p) &&
                                                          this.pollerConfig.isServiceInPackageAndEnabled(service.getServiceName(), p))
                                             .reduce((prev, curr) -> curr) // Take the last filtered element
                                             .orElse(null);
        if (pkg == null) {
            return;
        }

        // Find the service (and the pattern parameters) for the service name
        final Optional<Package.ServiceMatch> serviceMatch = pkg.findService(service.getServiceName());
        if (!serviceMatch.isPresent()) {
            return;
        }

        // Find the monitor implementation for the service name
        final ServiceMonitor serviceMonitor = this.pollerConfig.getServiceMonitor(serviceMatch.get().service.getName());
        if (serviceMonitor == null) {
            return;
        }

        final Optional<String> rrdRepositoryDir = Optional.ofNullable(getServiceParameter(serviceMatch.get().service, "rrd-repository"));
        final Optional<RrdRepository> rrdRepository = rrdRepositoryDir.map(directory -> {
            final RrdRepository rrdRepositoryInstance = new RrdRepository();
            rrdRepositoryInstance.setStep(this.pollerConfig.getStep(pkg));
            rrdRepositoryInstance.setHeartBeat(rrdRepositoryInstance.getStep() * 2);
            rrdRepositoryInstance.setRraList(this.pollerConfig.getRRAList(pkg));
            rrdRepositoryInstance.setRrdBaseDir(new File(directory));
            return rrdRepositoryInstance;
        });

        // Create the thresholding session for this poller
        final Optional<ThresholdingSession> thresholdingSession = rrdRepository.flatMap(repository -> {
            try {
                return Optional.of(this.thresholdingService.createSession(service.getNodeId(),
                        InetAddressUtils.str(service.getIpAddress()),
                        service.getServiceName(),
                        repository,
                        new ServiceParameters(Collections.emptyMap())));
            } catch (final ThresholdInitializationException ex) {
                LOG.error("Failed to create thresholding session", ex);
                return Optional.empty();
            }
        });

        // Build remote polled services
        final RemotePolledService remotePolledService = new RemotePolledService(service.getNodeId(),
                                                                                service.getIpAddress(),
                                                                                service.getServiceName(),
                                                                                node.getForeignSource(),
                                                                                node.getForeignId(),
                                                                                node.getLabel(),
                                                                                pkg,
                                                                                serviceMatch.get(),
                                                                                serviceMonitor,
                                                                                servicePerspective.getPerspectiveLocation(),
                                                                                node.getLocation().getLocationName(),
                                                                                rrdRepository.orElse(null),
                                                                                thresholdingSession.orElse(null));

        // Build job for scheduler
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
    }

    @Override
    public void onServicePerspectiveRemoved(final PerspectiveServiceTracker.ServicePerspectiveRef servicePerspective) {
        final JobKey key = buildJobKey(servicePerspective);

        try {
            this.scheduler.deleteJob(key);
        } catch (final SchedulerException e) {
            LOG.error("Failed to un-schedule {} ({}).", servicePerspective, key, e);
        }
    }

    public static JobKey buildJobKey(final PerspectiveServiceTracker.ServicePerspectiveRef servicePerspective) {
        return buildJobKey(servicePerspective.getNodeId(),
                           servicePerspective.getIpAddress(),
                           servicePerspective.getServiceName(),
                           servicePerspective.getPerspectiveLocation());
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
        if (polledService.getRrdRepository() == null) {
            return;
        }

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
        final PerspectiveResponseTimeResource resource = new PerspectiveResponseTimeResource(polledService.getPerspectiveLocation(), InetAddressUtils.str(polledService.getIpAddress()), polledService.getServiceName());
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
            if (polledService.getThresholdingSession() != null) {
                polledService.getThresholdingSession().accept(collectionSetDTO);
            }
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

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadDaemonConfig(final IEvent event) {
        DaemonTools.handleReloadEvent(event, RemotePollerd.NAME, (ev) ->  {
            try {
                this.pollerConfig.update();

                this.destroy();
                this.start();
            } catch (final Exception e) {
                LOG.error("Failed to reload poller configuration", e);
            }
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

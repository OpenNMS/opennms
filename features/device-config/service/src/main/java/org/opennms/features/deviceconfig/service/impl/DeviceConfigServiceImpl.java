/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.service.impl;

import static org.opennms.netmgt.poller.support.AbstractServiceMonitor.getKeyedString;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.service.DeviceConfigConstants;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.ReadOnlyPollerConfigManager;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.DeviceConfig;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.netmgt.poller.support.SimpleMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DeviceConfigServiceImpl implements DeviceConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigServiceImpl.class);

    private static final String DEVICE_CONFIG_SERVICE_CLASS_NAME = "org.opennms.features.deviceconfig.monitors.DeviceConfigMonitor";

    @Autowired
    private LocationAwarePollerClient locationAwarePollerClient;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Qualifier("deviceConfigMonitorAdaptor")
    private ServiceMonitorAdaptor serviceMonitorAdaptor;

    @Autowired
    private PollerConfig pollerConfig;

    @Override
    public void triggerConfigBackup(String ipAddress, String location, String service) throws IOException {
        try {
            InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            LOG.error("Unknown/Invalid IpAddress {}", ipAddress);
            throw new IllegalArgumentException("Unknown/Invalid IpAddress " + ipAddress);
        }

        CompletableFuture<PollerResponse> future = pollDeviceConfig(ipAddress, location, service);
        future.whenComplete(((pollerResponse, throwable) -> {
            if (throwable != null) {
                LOG.info("Error while manually triggering config backup for IpAddress {} at location {} for service {}",
                        ipAddress, location, service, throwable);
            }
        }));
    }

    @Override
    public CompletableFuture<DeviceConfig> getDeviceConfig(String ipAddress, String location, String service, int timeout) throws IOException {
        return pollDeviceConfig(ipAddress, location, service)
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .thenApply(resp -> {
                    if (resp.getPollStatus().isAvailable()) {
                        return resp.getPollStatus().getDeviceConfig();
                    } else {
                        throw new RuntimeException("Requesting backup failed: " + resp.getPollStatus().getReason());
                    }
                })
                .whenComplete((config, throwable) -> {
                    if (throwable != null) {
                        LOG.error("Error while getting device config for IpAddress {} at location {}", ipAddress, location, throwable);
                    }
                });
    }

    @Override
    public List<RetrievalDefinition> getRetrievalDefinitions(final String ipAddress, final String location) {
        final var iface = this.ipInterfaceDao.findByIpAddressAndLocation(ipAddress, location);
        PollerConfig pollerConfig;
        try {
            pollerConfig = this.getPollerConfig();
        } catch (IOException e) {
            LOG.error("Exception while retrieving pollerConfig", e);
            return new ArrayList<>();
        }
        return iface
                // Get all device config services defined for this interface
                .getMonitoredServices().stream()

                // Resolve the service name into service config
                .flatMap(svc -> pollerConfig.findService(InetAddressUtils.str(svc.getIpAddress()), svc.getServiceName()).stream())

                // Filter for the device config monitor
                .filter(match -> {
                    ServiceMonitor serviceMonitor = pollerConfig.getServiceMonitor(match.service.getName());
                    if (serviceMonitor != null) {
                        return serviceMonitor.getClass().getCanonicalName().equals(DEVICE_CONFIG_SERVICE_CLASS_NAME);
                    }
                    return false;
                })

                // Resolve the parameters
                .map(match -> {
                    final var serviceName = match.serviceName;

                    final var pollerParameters = locationAwarePollerClient.poll()
                            .withService(new SimpleMonitoredService(InetAddressUtils.addr(ipAddress),
                                    iface.getNode().getId(),
                                    iface.getNode().getLabel(),
                                    match.serviceName,
                                    location))
                            .withAttributes(match.service.getParameterMap())
                            .withPatternVariables(match.patternVariables)
                            .getInterpolatedAttributes();

                    return new RetrievalDefinition() {
                        @Override
                        public String getServiceName() {
                            return serviceName;
                        }

                        @Override
                        public String getConfigType() {
                            return getKeyedString(pollerParameters, DeviceConfigConstants.CONFIG_TYPE, ConfigType.Default);
                        }

                        @Override
                        public String getSchedule() {
                            return getKeyedString(pollerParameters, DeviceConfigConstants.SCHEDULE, DeviceConfigConstants.DEFAULT_CRON_SCHEDULE);
                        }
                    };
                })
                // Collect to resulting map
                .collect(Collectors.toList());
    }

    private CompletableFuture<PollerResponse> pollDeviceConfig(String ipAddress, String location, String serviceName) throws IOException {
        final var match = getPollerConfig().findService(ipAddress, serviceName)
                .orElseThrow(IllegalArgumentException::new);

        final var monitor = getPollerConfig().getServiceMonitor(match.service.getName());

        final AbstractMap.SimpleImmutableEntry<Boolean, MonitoredService> boundServicePair = sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsIpInterface ipInterface = ipInterfaceDao.findByIpAddressAndLocation(ipAddress, location);
            if (ipInterface == null) {
                return null;
            }

            final boolean bound = ipInterface.getMonitoredServices().stream()
                    .map(OnmsMonitoredService::getServiceName).anyMatch(s -> serviceName.equals(s));

            final OnmsNode node = ipInterface.getNode();

            return new AbstractMap.SimpleImmutableEntry(bound, new SimpleMonitoredService(ipInterface.getIpAddress(), node.getId(), node.getLabel(), match.serviceName, location));
        });

        if (boundServicePair == null) {
            throw new IllegalArgumentException("No interface found with ipAddress " + ipAddress + " at location " + location);
        }

        final Boolean serviceBound = boundServicePair.getKey();
        final MonitoredService service = boundServicePair.getValue();

        if (!serviceBound) {
            throw new IllegalArgumentException("Service " + serviceName + " not bound to interface with ipAddress " + ipAddress + " at location " + location);
        }

        // All the service parameters should be loaded from metadata in PollerRequestBuilderImpl
        // Persistence will be performed in DeviceConfigMonitorAdaptor.
        return locationAwarePollerClient.poll()
                .withService(service)
                .withAdaptor(serviceMonitorAdaptor)
                .withMonitor(monitor)
                .withPatternVariables(match.patternVariables)
                .withAttributes(match.service.getParameterMap())
                .withAttribute(DeviceConfigConstants.TRIGGERED_POLL, "true")
                .execute();
    }

    public void setLocationAwarePollerClient(LocationAwarePollerClient locationAwarePollerClient) {
        this.locationAwarePollerClient = locationAwarePollerClient;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        this.ipInterfaceDao = ipInterfaceDao;
    }

    public void setServiceMonitorAdaptor(ServiceMonitorAdaptor serviceMonitorAdaptor) {
        this.serviceMonitorAdaptor = serviceMonitorAdaptor;
    }

    public PollerConfig getPollerConfig() throws IOException {
        if (this.pollerConfig == null) {
            this.pollerConfig = ReadOnlyPollerConfigManager.create();
        }
        return this.pollerConfig;
    }
}

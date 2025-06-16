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
package org.opennms.features.deviceconfig.service.impl;

import static org.opennms.netmgt.poller.support.AbstractServiceMonitor.getKeyedString;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
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
import org.opennms.netmgt.poller.ServiceMonitorLocator;
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
    public CompletableFuture<DeviceConfigBackupResponse> triggerConfigBackup(String ipAddress, String location, String service, boolean persist) throws IOException {
        try {
            InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            LOG.error("Unknown/Invalid IpAddress {}", ipAddress);
            throw new IllegalArgumentException("Unknown/Invalid IpAddress " + ipAddress);
        }

        return pollDeviceConfig(ipAddress, location, service, persist)
                .thenApply(response -> {
                    return new DeviceConfigBackupResponse(response.getPollStatus().getReason(), response.getPollStatus().getDeviceConfig().getScriptOutput());
                }).whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        LOG.error("Error while getting device config for IpAddress {} at location {}", ipAddress, location, throwable);
                    }
                });
    }

    @Override
    public CompletableFuture<DeviceConfig> getDeviceConfig(String ipAddress, String location, String service, boolean persist, int timeout) throws IOException {
        return pollDeviceConfig(ipAddress, location, service, persist)
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
        final var iface = findMatchingInterface(ipAddress, location, null);

        if (iface == null) {
            return Collections.emptyList();
        }

        return iface
                // Get all device config services defined for this interface
                .getMonitoredServices().stream()

                // Resolve the service name into service config
                .flatMap(svc -> pollerConfig.findService(InetAddressUtils.str(svc.getIpAddress()), svc.getServiceName()).stream())

                // Filter for the device config monitor
                .filter(match -> pollerConfig.getServiceMonitorLocator(match.service.getName())
                                             .map(loc -> Objects.equals(loc.getServiceLocatorKey(), DEVICE_CONFIG_SERVICE_CLASS_NAME))
                                             .orElse(false))

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

    private CompletableFuture<PollerResponse> pollDeviceConfig(String ipAddress, String location, String serviceName, boolean persist) throws IOException {
        final var match = getPollerConfig().findService(ipAddress, serviceName)
                .orElseThrow(IllegalArgumentException::new);

        final var monitor = getPollerConfig().getServiceMonitorLocator(match.service.getName())
                .orElseThrow(IllegalArgumentException::new);

        final AbstractMap.SimpleImmutableEntry<Boolean, MonitoredService> boundServicePair = sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsIpInterface ipInterface = findMatchingInterface(ipAddress, location, serviceName);
            if (ipInterface == null) {
                return null;
            }

            final boolean bound = ipInterface.getMonitoredServices().stream()
                    .map(OnmsMonitoredService::getServiceName).anyMatch(s -> serviceName.equals(s));

            final OnmsNode node = ipInterface.getNode();

            return new AbstractMap.SimpleImmutableEntry<>(bound, new SimpleMonitoredService(ipInterface.getIpAddress(), node.getId(), node.getLabel(), match.serviceName, location));
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
        if (persist) {
            // Persistence will be performed in DeviceConfigMonitorAdaptor.
            return locationAwarePollerClient.poll()
                    .withService(service)
                    .withAdaptor(serviceMonitorAdaptor)
                    .withMonitorLocator(monitor)
                    .withPatternVariables(match.patternVariables)
                    .withAttributes(match.service.getParameterMap())
                    .withAttribute(DeviceConfigConstants.TRIGGERED_POLL, "true")
                    .execute();
        } else {
            // No persistence of config.
            return locationAwarePollerClient.poll()
                    .withService(service)
                    .withMonitorLocator(monitor)
                    .withPatternVariables(match.patternVariables)
                    .withAttributes(match.service.getParameterMap())
                    .withAttribute(DeviceConfigConstants.TRIGGERED_POLL, "true")
                    .execute();
        }
    }

    private OnmsIpInterface findMatchingInterface(final String ipAddress, final String location, String serviceName) {
        var ipInterfaces = this.ipInterfaceDao.findByIpAddressAndLocation(ipAddress, location);
        OnmsIpInterface iface = ipInterfaces.size() > 0 ? ipInterfaces.get(0) : null;
        if (ipInterfaces.size() > 1) {
            var optionalInterface = ipInterfaces
                    .stream().filter(ipInterface ->
                            ipInterface.getMonitoredServices().stream().anyMatch(monitoredService -> {
                                if (Strings.isNullOrEmpty(serviceName)) {
                                    return monitoredService.getServiceName().startsWith(DEVICE_CONFIG_PREFIX);
                                }
                                return monitoredService.getServiceName().equals(serviceName);
                            })).findFirst();
            iface = optionalInterface.orElseGet(() -> ipInterfaces.stream().findFirst().orElse(null));
        }
        return iface;
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

    public void setPollerConfig(PollerConfig pollerConfig) {
        this.pollerConfig = pollerConfig;
    }

    public PollerConfig getPollerConfig() {
        return pollerConfig;
    }
}

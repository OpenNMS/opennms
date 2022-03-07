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

import com.google.common.base.Strings;
import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.ReadOnlyPollerConfigManager;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.DeviceConfig;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.netmgt.poller.support.SimpleMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DeviceConfigServiceImpl implements DeviceConfigService {

    private static final String DEVICE_CONFIG_SERVICE_NAME_PREFIX = "DeviceConfig-";
    private static final String DEVICE_CONFIG_MONITOR_CLASS_NAME = "org.opennms.features.deviceconfig.monitors.DeviceConfigMonitor";
    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigServiceImpl.class);
    public static final String TRIGGERED_POLL = "dcbTriggeredPoll";

    @Autowired
    private LocationAwarePollerClient locationAwarePollerClient;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Qualifier("deviceConfigMonitorAdaptor")
    private ServiceMonitorAdaptor serviceMonitorAdaptor;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;


    @Override
    public void triggerConfigBackup(String ipAddress, String location, String configType) throws IOException {

        CompletableFuture<PollerResponse> future = pollDeviceConfig(ipAddress, location, configType);
        future.whenComplete(((pollerResponse, throwable) -> {
            if (throwable != null) {
                LOG.info("Error while manually triggering config backup for IpAddress {} at location {} for config-type {}",
                        ipAddress, location, configType, throwable);
            }
        }));
    }

    @Override
    public CompletableFuture<DeviceConfig> getDeviceConfig(String ipAddress, String location, String configType, int timeout) throws IOException {
        return pollDeviceConfig(ipAddress, location, configType)
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .thenApply(resp -> resp.getPollStatus().getDeviceConfig())
                .whenComplete((config, throwable) -> {
                    if (throwable != null) {
                        LOG.error("Error while getting device config for IpAddress {} at location {}", ipAddress, location, throwable);
                    }
                });
    }

    private CompletableFuture<PollerResponse> pollDeviceConfig(String ipAddress, String location, String configType) throws IOException {
        if (Strings.isNullOrEmpty(configType)) {
            configType = ConfigType.Default;
        }
        String serviceName = DEVICE_CONFIG_SERVICE_NAME_PREFIX + configType;
        final String configTypeName = configType;
        MonitoredService service = sessionUtils.withReadOnlyTransaction(() -> {
            OnmsIpInterface ipInterface = ipInterfaceDao.findByIpAddressAndLocation(ipAddress, location);
            if (ipInterface == null) {
                return null;
            }
            OnmsNode node = ipInterface.getNode();
            List<OnmsMonitoredService> services =
                    monitoredServiceDao.findSimilarServicesOnInterface(node.getId(), ipInterface.getIpAddress(), DEVICE_CONFIG_SERVICE_NAME_PREFIX);
            if (!services.isEmpty()) {
                Optional<OnmsMonitoredService> optional =
                        services.stream().filter(onmsMonitoredService -> onmsMonitoredService.getServiceName().contains(configTypeName)).findFirst();
                if (optional.isPresent()) {
                    return new SimpleMonitoredService(ipInterface.getIpAddress(), node.getId(), node.getLabel(), optional.get().getServiceName(), location);
                }
            }
            return new SimpleMonitoredService(ipInterface.getIpAddress(), node.getId(), node.getLabel(), serviceName, location);
        });

        if (service == null) {
            throw new IllegalArgumentException("No interface found with ipAddress " + ipAddress + " at location " + location);
        }
        List<Parameter> parameters = getServiceParamsFromPollerConfig(service);
        Map<String, Object> serviceAttributes = convertParamsToAttributes(parameters);
        // All the service parameters should be loaded from metadata in PollerRequestBuilderImpl
        // Persistence will be performed in DeviceConfigMonitorAdaptor.
        return locationAwarePollerClient.poll()
                .withService(service)
                .withAdaptor(serviceMonitorAdaptor)
                .withMonitorClassName(DEVICE_CONFIG_MONITOR_CLASS_NAME)
                .withAttributes(serviceAttributes)
                .withAttribute(TRIGGERED_POLL, "true")
                .execute();
    }

    private List<Parameter> getServiceParamsFromPollerConfig(MonitoredService service) throws IOException {
        final PollerConfig pollerConfig = ReadOnlyPollerConfigManager.create();
        List<OnmsMetaData> metaData = sessionUtils.withReadOnlyTransaction(() -> {
            OnmsMonitoredService monitoredService = monitoredServiceDao.get(service.getNodeId(), service.getAddress(), service.getSvcName());
            List<OnmsMetaData> metaDataList = monitoredService.getMetaData();
            return metaDataList.stream().filter(onmsMetaData -> onmsMetaData.getContext().equals("requisition")).collect(Collectors.toList());
        });
        Optional<OnmsMetaData> pkgFromMetadata = metaData.stream().filter(onmsMetaData -> onmsMetaData.getKey().equals("package")).findFirst();
        List<Parameter> parameters = new ArrayList<>();
        if(pkgFromMetadata.isPresent()) {
            org.opennms.netmgt.config.poller.Package pkg = pollerConfig.getPackage(pkgFromMetadata.get().getValue());
            parameters.addAll(pkg.getService(service.getSvcName()).getParameters());
        } else {
            List<String> pkgNames = pollerConfig.getAllPackageMatches(service.getIpAddr());
            List<org.opennms.netmgt.config.poller.Package> packages =
                    pollerConfig.getPackages().stream().filter(pollerPackage ->
                            pollerConfig.isServiceInPackageAndEnabled(service.getSvcName(), pollerPackage)).collect(Collectors.toList());
            Optional<org.opennms.netmgt.config.poller.Package> pkg = packages.stream().filter(pollerPackage ->
                    pkgNames.contains(pollerPackage.getName())).findFirst();
            pkg.ifPresent(aPackage -> parameters.addAll(aPackage.getService(service.getSvcName()).getParameters()));
        }
        return parameters;
    }

    private Map<String, Object> convertParamsToAttributes(List<Parameter> parameters) {
        Map<String, Object> attributes = new TreeMap<>();
        if (parameters.isEmpty()) {
            // If we couldn't load any specific pakage, use dummy parameters
            // so that they get overwritten by metadata.
            attributes.put("username", "admin");
            attributes.put("password", "password");
            attributes.put("script", "script");
        }
        for (final Parameter parameter : parameters) {
            String value = parameter.getValue();
            if (value != null) {
                attributes.put(parameter.getKey(), value);
            }
        }
        return attributes;
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

    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        this.monitoredServiceDao = monitoredServiceDao;
    }
}

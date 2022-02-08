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

package org.opennms.features.deviceconfig.service;

import joptsimple.internal.Strings;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.ReadOnlyPollerConfigManager;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
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

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DeviceConfigServiceImpl implements DeviceConfigService {

    private static final String DEVICE_CONFIG_PACKAGE_NAME = "device-config";
    private static final String DEVICE_CONFIG_SERVICE_NAME = "DeviceConfig";
    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigServiceImpl.class);

    @Autowired
    private LocationAwarePollerClient locationAwarePollerClient;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Qualifier("deviceConfigMonitorAdaptor")
    private ServiceMonitorAdaptor serviceMonitorAdaptor;


    @Override
    public void triggerConfigBackup(String ipAddress, String location, String serviceName) throws IOException {

        String className = retrieveClassName(serviceName);

        MonitoredService service = sessionUtils.withReadOnlyTransaction(() -> {

            Optional<OnmsIpInterface> ipInterfaceOptional = ipInterfaceDao.findByIpAddress(ipAddress).stream().filter(ipInterface ->
                    ipInterface.getNode().getLocation().getLocationName().equals(location)).findFirst();
            if (ipInterfaceOptional.isEmpty()) {
                return null;
            }
            OnmsIpInterface ipInterface = ipInterfaceOptional.get();
            OnmsNode node = ipInterface.getNode();

            return new SimpleMonitoredService(ipInterface.getIpAddress(), node.getId(), node.getLabel(), serviceName, location);
        });

        if (service == null) {
            throw new IllegalArgumentException("No interface found with ipAddress " + ipAddress + " at location " + location);
        }
        // All the service parameters should be loaded from metadata in PollerRequestBuilderImpl
        // Persistence will be performed in DeviceConfigMonitorAdaptor.
        final CompletableFuture<PollerResponse> future = locationAwarePollerClient.poll()
                .withService(service)
                .withAdaptor(serviceMonitorAdaptor)
                .withMonitorClassName(className)
                .execute();
        future.whenComplete(((pollerResponse, throwable) -> {
            if (throwable != null) {
                LOG.info("Error while manually triggering config backup for IpAddress {} at location {} for service {}", ipAddress, location, service);
            }
        }));
    }

    private String retrieveClassName(String serviceName) throws IOException {
        final PollerConfig pollerConfig = ReadOnlyPollerConfigManager.create();
        org.opennms.netmgt.config.poller.Package pkg = pollerConfig.getPackage(DEVICE_CONFIG_PACKAGE_NAME);
        if (pkg == null) {
            LOG.error("Couldn't find package {} in poller-config", pkg);
            throw new IllegalArgumentException("Couldn't find package " + DEVICE_CONFIG_PACKAGE_NAME);
        }
        if (Strings.isNullOrEmpty(serviceName)) {
            serviceName = DEVICE_CONFIG_SERVICE_NAME;
        }
        final org.opennms.netmgt.config.poller.Service svc = pollerConfig.getServiceInPackage(serviceName, pkg);
        if (svc == null) {
            LOG.error("Couldn't find {} service in package {}", serviceName, pkg);
            throw new IllegalArgumentException("Couldn't find service " + serviceName + " in the package " + DEVICE_CONFIG_PACKAGE_NAME);
        }
        final Optional<Package.ServiceMatch> service = pkg.findService(serviceName);
        if (service.isEmpty()) {
            LOG.error("Couldn't find {} service", serviceName);
            throw new IllegalArgumentException("Couldn't find service " + serviceName + " in the package " + DEVICE_CONFIG_PACKAGE_NAME);
        }
        final ServiceMonitor monitor = pollerConfig.getServiceMonitor(service.get().service.getName());
        if (monitor == null) {
            LOG.error("Service {} doesn't have a monitor class defined", serviceName);
            throw new IllegalArgumentException("Service " + serviceName + " doesn't have a monitor class defined");
        }
        return monitor.getClass().getName();
    }
}

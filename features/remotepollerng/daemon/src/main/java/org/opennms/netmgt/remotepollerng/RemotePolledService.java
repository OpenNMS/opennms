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

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;

import com.google.common.base.MoreObjects;

public class RemotePolledService {

    private final OnmsMonitoredService monSvc;

    private final Package pkg;
    private final Service service;

    private final ServiceMonitor serviceMonitor;

    private final MonitoredService monitoredService;

    public RemotePolledService(final OnmsMonitoredService monSvc,
                               final Package pkg,
                               final Service service,
                               final ServiceMonitor serviceMonitor) {
        this.monSvc = Objects.requireNonNull(monSvc);
        this.pkg = Objects.requireNonNull(pkg);
        this.service = Objects.requireNonNull(service);
        this.serviceMonitor = Objects.requireNonNull(serviceMonitor);

        this.monitoredService = new MonitoredService() {
            @Override
            public String getSvcName() {
                return service.getName();
            }

            @Override
            public String getIpAddr() {
                return InetAddressUtils.str(monSvc.getIpAddress());
            }

            @Override
            public int getNodeId() {
                return monSvc.getNodeId();
            }

            @Override
            public String getNodeLabel() {
                return null;
            }

            @Override
            public String getNodeLocation() {
                return null;
            }

            @Override
            public InetAddress getAddress() {
                return monSvc.getIpAddress();
            }
        };
    }

    public OnmsMonitoredService getMonSvc() {
        return monSvc;
    }

    public Package getPkg() {
        return pkg;
    }

    public Service getService() {
        return service;
    }

    public ServiceMonitor getServiceMonitor() {
        return serviceMonitor;
    }

    public MonitoredService getMonitoredService() {
        return monitoredService;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(RemotePolledService.class)
                .add("package", pkg.getName())
                .add("service", service.getName())
                .add("ipAddress", InetAddressUtils.str(monSvc.getIpAddress()))
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemotePolledService that = (RemotePolledService) o;
        return Objects.equals(monSvc.getId(), that.monSvc.getId()) &&
                Objects.equals(pkg, that.pkg) &&
                Objects.equals(service, that.service);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monSvc.getId(), pkg, service);
    }
}

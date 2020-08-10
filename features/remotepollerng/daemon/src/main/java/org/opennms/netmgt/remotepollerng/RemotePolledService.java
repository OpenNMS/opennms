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
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

public class RemotePolledService {
    private static final Logger LOG = LoggerFactory.getLogger(RemotePolledService.class);
    private static final ServiceParameters EMPTY_SERVICE_PARAMS = new ServiceParameters(Collections.emptyMap());

    private final ServiceTracker.Service service;

    private final Package pkg;
    private final Package.ServiceMatch serviceMatch;

    private final ServiceMonitor serviceMonitor;

    private final MonitoredService monitoredService;

    private final String perspectiveLocation;

    private ThresholdingSession thresholdingSession;

    private PollStatus lastStatus;

    public RemotePolledService(ServiceTracker.Service service,
                               final Package pkg,
                               final Package.ServiceMatch serviceMatch,
                               final ServiceMonitor serviceMonitor,
                               final String perspectiveLocation) {
        this.service = Objects.requireNonNull(service);
        this.pkg = Objects.requireNonNull(pkg);
        this.serviceMatch = Objects.requireNonNull(serviceMatch);
        this.serviceMonitor = Objects.requireNonNull(serviceMonitor);
        this.perspectiveLocation = Objects.requireNonNull(perspectiveLocation);

        this.monitoredService = new MonitoredService() {
            @Override
            public String getSvcName() {
                return service.serviceName;
            }

            @Override
            public String getIpAddr() {
                return InetAddressUtils.str(service.ipAddress);
            }

            @Override
            public int getNodeId() {
                return service.nodeId;
            }

            @Override
            public String getNodeLabel() {
                // TODO fooker: do we need this label?
                return null;
            }

            @Override
            public String getNodeLocation() {
                // This returns the perspective location instead of the node location as the poll should be executed
                // from the perspective location
                return perspectiveLocation;
            }

            @Override
            public InetAddress getAddress() {
                return service.ipAddress;
            }
        };
    }

    public boolean updateStatus(final PollStatus status) {
        if (!Objects.equals(this.lastStatus, status)) {
            this.lastStatus = status;
            return true;
        } else {
            return false;
        }
    }

    public ServiceTracker.Service getService() {
        return this.service;
    }

    public Package getPkg() {
        return pkg;
    }

    public Service getServiceConfig() {
        return serviceMatch.service;
    }

    public Map<String, String> getPatternVariables() {
        return this.serviceMatch.patternVariables;
    }

    public ServiceMonitor getServiceMonitor() {
        return serviceMonitor;
    }

    public MonitoredService getMonitoredService() {
        return monitoredService;
    }

    public String getPerspectiveLocation() {
        return this.perspectiveLocation;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("service", this.service)
                          .add("pkg", this.pkg)
                          .add("serviceMatch", this.serviceMatch)
                          .add("serviceMonitor", this.serviceMonitor)
                          .add("monitoredService", this.monitoredService)
                          .add("perspectiveLocation", this.perspectiveLocation)
                          .add("thresholdingSession", this.thresholdingSession)
                          .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RemotePolledService)) {
            return false;
        }
        final RemotePolledService that = (RemotePolledService) o;
        return Objects.equals(this.service, that.service) &&
               Objects.equals(this.pkg, that.pkg) &&
               Objects.equals(this.serviceMatch, that.serviceMatch) &&
               Objects.equals(this.serviceMonitor, that.serviceMonitor) &&
               Objects.equals(this.monitoredService, that.monitoredService) &&
               Objects.equals(this.perspectiveLocation, that.perspectiveLocation) &&
               Objects.equals(this.thresholdingSession, that.thresholdingSession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.service, this.pkg, this.serviceMatch, this.serviceMonitor, this.monitoredService, this.perspectiveLocation, this.thresholdingSession);
    }

    public void applyThresholds(final ThresholdingService thresholdingService, final CollectionSet collectionSet, final MonitoredService service, final String dsName, final RrdRepository repository) {
        try {
            if (this.thresholdingSession == null) {
                this.thresholdingSession = thresholdingService.createSession(service.getNodeId(),
                                                                             service.getIpAddr(),
                                                                             service.getSvcName(),
                                                                             repository,
                                                                             EMPTY_SERVICE_PARAMS);
            }
            this.thresholdingSession.accept(collectionSet);
        } catch (Throwable e) {
            LOG.error("Failed to threshold on {} for {} because of an exception", service, dsName, e);
        }
    }
}

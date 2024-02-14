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
package org.opennms.netmgt.perspectivepoller;

import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdingSession;

import com.google.common.base.MoreObjects;

public class PerspectivePolledService {
    private final int nodeId;
    private final InetAddress ipAddress;
    private final String serviceName;

    private final String foreignSource;
    private final String foreignId;

    private final String nodeLabel;

    private final Package pkg;

    private final Package.ServiceMatch serviceMatch;

    private final ServiceMonitorLocator serviceMonitorLocator;

    private final String perspectiveLocation;
    private final String residentLocation;

    private final RrdRepository rrdRepository;

    private final ThresholdingSession thresholdingSession;

    private final MonitoredService monitoredService;

    private PollStatus lastStatus;

    public PerspectivePolledService(final int nodeId,
                                    final InetAddress ipAddress,
                                    final String serviceName,
                                    final String foreignSource,
                                    final String foreignId,
                                    final String nodeLabel,
                                    final Package pkg,
                                    final Package.ServiceMatch serviceMatch,
                                    final ServiceMonitorLocator serviceMonitorLocator,
                                    final String perspectiveLocation,
                                    final String residentLocation,
                                    final RrdRepository rrdRepository,
                                    final ThresholdingSession thresholdingSession) {
        this.nodeId = Objects.requireNonNull(nodeId);
        this.ipAddress = Objects.requireNonNull(ipAddress);
        this.serviceName = Objects.requireNonNull(serviceName);
        this.foreignSource = Objects.requireNonNull(foreignSource);
        this.foreignId = Objects.requireNonNull(foreignId);
        this.nodeLabel = Objects.requireNonNull(nodeLabel);
        this.pkg = Objects.requireNonNull(pkg);
        this.serviceMatch = Objects.requireNonNull(serviceMatch);
        this.serviceMonitorLocator = Objects.requireNonNull(serviceMonitorLocator);
        this.perspectiveLocation = Objects.requireNonNull(perspectiveLocation);
        this.residentLocation = Objects.requireNonNull(residentLocation);
        this.rrdRepository = rrdRepository;
        this.thresholdingSession = thresholdingSession;

        this.monitoredService = new MonitoredService() {
            @Override
            public String getSvcName() {
                return serviceName;
            }

            @Override
            public String getIpAddr() {
                return InetAddressUtils.str(ipAddress);
            }

            @Override
            public int getNodeId() {
                return nodeId;
            }

            @Override
            public String getNodeLabel() {
                return nodeLabel;
            }

            @Override
            public String getNodeLocation() {
                // This returns the perspective location instead of the node location as the poll should be executed
                // from the perspective location
                return perspectiveLocation;
            }

            @Override
            public InetAddress getAddress() {
                return ipAddress;
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

    public int getNodeId() {
        return this.nodeId;
    }

    public InetAddress getIpAddress() {
        return this.ipAddress;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getForeignSource() {
        return this.foreignSource;
    }

    public String getForeignId() {
        return this.foreignId;
    }

    public String getNodeLabel() {
        return this.nodeLabel;
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

    public ServiceMonitorLocator getServiceMonitorLocator() {
        return serviceMonitorLocator;
    }

    public MonitoredService getMonitoredService() {
        return monitoredService;
    }

    public String getPerspectiveLocation() {
        return this.perspectiveLocation;
    }

    public String getResidentLocation() {
        return this.residentLocation;
    }

    public RrdRepository getRrdRepository() {
        return this.rrdRepository;
    }

    public ThresholdingSession getThresholdingSession() {
        return this.thresholdingSession;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("nodeId", this.nodeId)
                          .add("ipAddress", this.ipAddress)
                          .add("serviceName", this.serviceName)
                          .add("pkg", this.pkg)
                          .add("serviceMatch", this.serviceMatch)
                          .add("serviceMonitorLocator", this.serviceMonitorLocator)
                          .add("monitoredService", this.monitoredService)
                          .add("perspectiveLocation", this.perspectiveLocation)
                          .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PerspectivePolledService)) {
            return false;
        }
        final PerspectivePolledService that = (PerspectivePolledService) o;
        return Objects.equals(this.nodeId, that.nodeId) &&
               Objects.equals(this.ipAddress, that.ipAddress) &&
               Objects.equals(this.serviceName, that.serviceName) &&
               Objects.equals(this.pkg, that.pkg) &&
               Objects.equals(this.serviceMatch, that.serviceMatch) &&
               Objects.equals(this.serviceMonitorLocator, that.serviceMonitorLocator) &&
               Objects.equals(this.monitoredService, that.monitoredService) &&
               Objects.equals(this.perspectiveLocation, that.perspectiveLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nodeId, this.ipAddress, this.serviceName, this.pkg, this.serviceMatch, this.serviceMonitorLocator, this.monitoredService, this.perspectiveLocation);
    }
}

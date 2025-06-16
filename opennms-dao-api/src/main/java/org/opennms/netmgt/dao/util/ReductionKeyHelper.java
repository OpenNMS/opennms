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
package org.opennms.netmgt.dao.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class ReductionKeyHelper {

    // TODO: The distributed poller name (now monitoring system name?) should be part of the edge details
    public static final String DEFAULT_DISTRIBUTED_POLLER_NAME = "";

    public static Set<String> getReductionKeys(final OnmsApplication application) {
        Objects.requireNonNull(application);
        Set<String> reductionKeys = new HashSet<>();

        for (OnmsMonitoredService monitoredService : application.getMonitoredServices()) {
            reductionKeys.addAll(getReductionKeys(monitoredService));
        }

        return reductionKeys;
    }

    public static Set<String> getReductionKeys(final OnmsMonitoredService monitoredService) {
        Objects.requireNonNull(monitoredService);
        Set<String> reductionKeys = new HashSet<>();
        reductionKeys.add(getNodeLostServiceReductionKey(monitoredService));
        // When node processing is enabled, we may get node down instead of node lost service events
        reductionKeys.add(getNodeDownReductionKey(monitoredService));
        // We may get interface down instead of node lost service events
        reductionKeys.add(getInterfaceDownReductionKey(monitoredService));
        return reductionKeys;
    }

    public static String getNodeDownReductionKey(final OnmsMonitoredService monitoredService) {
        return String.format("%s:%s:%d",
                EventConstants.NODE_DOWN_EVENT_UEI,
                DEFAULT_DISTRIBUTED_POLLER_NAME,
                monitoredService.getNodeId());
    }

    public static String getNodeLostServiceReductionKey(final OnmsMonitoredService monitoredService) {
        return String.format("%s:%s:%d:%s:%s",
                EventConstants.NODE_LOST_SERVICE_EVENT_UEI,
                DEFAULT_DISTRIBUTED_POLLER_NAME,
                monitoredService.getNodeId(),
                InetAddressUtils.toIpAddrString(monitoredService.getIpAddress()),
                monitoredService.getServiceName());
    }

    public static Set<String> getNodeLostServiceFromPerspectiveReductionKeys(final OnmsMonitoredService monitoredService) {
        Objects.requireNonNull(monitoredService);
        return monitoredService
                .getApplications().stream()
                .flatMap(a -> a.getPerspectiveLocations().stream())
                .map(OnmsMonitoringLocation::getLocationName)
                .distinct()
                .map(locationName -> String.format("%s:%s:%d:%s:%s",
                        EventConstants.PERSPECTIVE_NODE_LOST_SERVICE_UEI,
                        locationName,
                        monitoredService.getNodeId(),
                        InetAddressUtils.toIpAddrString(monitoredService.getIpAddress()),
                        monitoredService.getServiceName()))
                .collect(Collectors.toSet());
    }

    public static String getInterfaceDownReductionKey(final OnmsMonitoredService monitoredService) {
        return String.format("%s:%s:%s:%s",
                EventConstants.INTERFACE_DOWN_EVENT_UEI,
                DEFAULT_DISTRIBUTED_POLLER_NAME,
                monitoredService.getNodeId(),
                InetAddressUtils.toIpAddrString(monitoredService.getIpAddress()));
    }
}

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
package org.opennms.features.topology.plugins.topo.application.browsers;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

/**
 * A wrapper around an OnmsOutage object to be used in the ApplicationOutage table.
 */
public class ApplicationOutage {

    final OnmsOutage delegate;

    ApplicationOutage(final OnmsOutage delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public int getId() {
        return delegate.getId();
    }

    public String getNodeLabel() {
        return delegate.getNodeLabel();
    }

    public String getForeignSource() {
        return delegate.getForeignSource();
    }

    public String getForeignId() {
        return delegate.getForeignId();
    }

    public String getIpAddress() {
        return Optional.ofNullable(delegate.getMonitoredService())
                .map(OnmsMonitoredService::getIpInterface)
                .map(OnmsIpInterface::getIpAddress)
                .map(InetAddressUtils::toIpAddrString)
                .orElse("null");
    }

    public String getServiceName() {
        return Optional.ofNullable(delegate.getServiceType())
                .map(OnmsServiceType::getName)
                .orElse("null");
    }

    public Date getIfLostService() {
        return delegate.getIfLostService();
    }

    public String getPerspective() {
        return Optional.ofNullable(delegate.getPerspective())
                .map(OnmsMonitoringLocation::getLocationName)
                .orElse("null");
    }
}

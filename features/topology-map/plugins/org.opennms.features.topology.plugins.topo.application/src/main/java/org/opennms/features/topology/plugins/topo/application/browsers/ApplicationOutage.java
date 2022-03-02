/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

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

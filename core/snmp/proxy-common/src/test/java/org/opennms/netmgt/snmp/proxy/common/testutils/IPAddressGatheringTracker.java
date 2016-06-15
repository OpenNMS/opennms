/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.proxy.common.testutils;

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.provision.service.IPAddressTableTracker;

import com.google.common.collect.Lists;

public class IPAddressGatheringTracker extends IPAddressTableTracker {
    private final List<IPAddressTableTracker.IPAddressRow> ipAddressRows = Lists.newArrayList();

    public String getDescription() {
        return"IP address tables";
    }

    @Override
    public void processIPAddressRow(final IPAddressTableTracker.IPAddressRow row) {
        ipAddressRows.add(row);
    }

    public List<String> getIpAddresses() {
        return ipAddressRows.stream()
                .map(row -> row.getIpAddress())
                .collect(Collectors.toList());
    }
}

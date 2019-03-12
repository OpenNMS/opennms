/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.utils.LocationUtils;
import org.opennms.integration.api.v1.model.IpInterface;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.NodeAssetRecord;
import org.opennms.integration.api.v1.model.SnmpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class NodeBean implements Node {

    private final OnmsNode node;
    private final String location;
    private final NodeAssetRecord assetRecord;
    private final List<IpInterface> ipInterfaces;
    private final List<SnmpInterface> snmpInterfaces;

    public NodeBean(OnmsNode node) {
        this.node = Objects.requireNonNull(node);

        final OnmsMonitoringLocation monitoringLocation = node.getLocation();
        if (monitoringLocation != null) {
            location = LocationUtils.getEffectiveLocationName(monitoringLocation.getLocationName());
        } else {
            location = LocationUtils.DEFAULT_LOCATION_NAME;
        }

        this.assetRecord = new NodeAssetRecordBean(node.getAssetRecord());
        this.ipInterfaces = node.getIpInterfaces().stream()
                .map(IpInterfaceBean::new)
                .collect(Collectors.toList());
        this.snmpInterfaces = node.getSnmpInterfaces().stream()
                .map(SnmpInterfaceBean::new)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getId() {
        return node.getId();
    }

    @Override
    public String getForeignSource() {
        return node.getForeignSource();
    }

    @Override
    public String getForeignId() {
        return node.getForeignId();
    }

    @Override
    public String getLabel() {
        return node.getLabel();
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public NodeAssetRecord getAssetRecord() {
        return assetRecord;
    }

    @Override
    public List<IpInterface> getIpInterfaces() {
        return ipInterfaces;
    }

    @Override
    public List<SnmpInterface> getSnmpInterfaces() {
        return snmpInterfaces;
    }
}

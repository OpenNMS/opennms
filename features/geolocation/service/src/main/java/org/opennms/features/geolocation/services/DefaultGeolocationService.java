/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.geolocation.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.geolocation.api.AddressInfo;
import org.opennms.features.geolocation.api.Coordinates;
import org.opennms.features.geolocation.api.GeolocationInfo;
import org.opennms.features.geolocation.api.GeolocationQuery;
import org.opennms.features.geolocation.api.GeolocationService;
import org.opennms.features.geolocation.api.NodeInfo;
import org.opennms.features.geolocation.api.SeverityInfo;
import org.opennms.features.status.api.node.NodeStatusCalculator;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculationStrategy;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculatorConfig;
import org.opennms.features.status.api.node.strategy.Status;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;

public class DefaultGeolocationService implements GeolocationService {

    private GenericPersistenceAccessor genericPersistenceAccessor;
    private NodeStatusCalculator nodeStatusCalculator;

    public DefaultGeolocationService(GenericPersistenceAccessor genericPersistenceAccessor, NodeStatusCalculator nodeStatusCalculator) {
        this.genericPersistenceAccessor = Objects.requireNonNull(genericPersistenceAccessor);
        this.nodeStatusCalculator = Objects.requireNonNull(nodeStatusCalculator);
    }

    @Override
    public List<GeolocationInfo> getLocations(GeolocationQuery query) {
        if (query == null) {
            return new ArrayList<>();
        }

        final List<OnmsNode> nodes = getNodes(query);
        final List<GeolocationInfo> nodesWithCoordinates = nodes.stream()
                .filter(n -> geoLocation(n) != null && geoLocation(n).getLongitude() != null && geoLocation(n).getLatitude() != null)
                // Avoid including -inf values, just in case. See NMS-9338
                .filter(n -> geoLocation(n).getLatitude() != Double.NEGATIVE_INFINITY && geoLocation(n).getLongitude() != Double.NEGATIVE_INFINITY)
                .map(this::convert)
                .collect(Collectors.toList());

        if (query.getStatusCalculationStrategy() != null) {
            applyStatus(query, nodesWithCoordinates);
        }

        if (query.getSeverity() != null) {
            OnmsSeverity severity = OnmsSeverity.get(query.getSeverity().getId());
            return nodesWithCoordinates.stream()
                    .filter(n -> severity.getId() <= n.getSeverityInfo().getId())
                    .collect(Collectors.toList());
        }
        return nodesWithCoordinates;
    }

    private List<OnmsNode> getNodes(GeolocationQuery query) {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNode.class)
                .alias("assetRecord", "assetRecord")
                .and(
                    Restrictions.isNotNull("assetRecord"),
                    Restrictions.isNotNull("assetRecord.geolocation")
                );
        if (query.getLocation() != null) {
            criteriaBuilder.and(Restrictions.eq("location", query.getLocation()));
        }
        if (!query.getNodeIds().isEmpty()) {
            criteriaBuilder.in("id", query.getNodeIds());
        }
        return genericPersistenceAccessor.findMatching(criteriaBuilder.toCriteria());
    }


    private GeolocationInfo convert(OnmsNode node) {
        GeolocationInfo geolocationInfo = new GeolocationInfo();

        // Coordinates
        OnmsGeolocation onmsGeolocation = geoLocation(node);
        if (onmsGeolocation != null) {
            geolocationInfo.setAddressInfo(toAddressInfo(onmsGeolocation));
            if (onmsGeolocation.getLongitude() != null && onmsGeolocation.getLatitude() != null) {
                geolocationInfo.setCoordinates(new Coordinates(onmsGeolocation.getLongitude(), onmsGeolocation.getLatitude()));
            }
        }

        // NodeInfo
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setNodeId(node.getId());
        nodeInfo.setNodeLabel(node.getLabel());
        nodeInfo.setNodeLabel(node.getLabel());
        nodeInfo.setForeignSource(node.getForeignSource());
        nodeInfo.setForeignId(node.getForeignId());
        nodeInfo.setLocation(node.getLocation().getLocationName());
        if (node.getAssetRecord() != null) {
            nodeInfo.setDescription(node.getAssetRecord().getDescription());
            nodeInfo.setMaintcontract(node.getAssetRecord().getMaintcontract());
        }
        if (node.getPrimaryInterface() != null) {
            nodeInfo.setIpAddress(InetAddressUtils.str(node.getPrimaryInterface().getIpAddress()));
        }
        nodeInfo.setCategories(node.getCategories()
                .stream()
                .map(OnmsCategory::getName)
                .collect(Collectors.toList()));
        geolocationInfo.setNodeInfo(nodeInfo);
        return geolocationInfo;
    }

    private void applyStatus(GeolocationQuery query, List<GeolocationInfo> locations) {
        final Set<Integer> nodeIds = locations.stream().map(l -> l.getNodeInfo().getNodeId()).collect(Collectors.toSet());
        if (!nodeIds.isEmpty()) {
            final Status status = calculateStatus(query, nodeIds);
            // Appliing the calculated status to each location
            for(GeolocationInfo info : locations) {
                OnmsSeverity severity = status.getSeverity(info.getNodeInfo().getNodeId());
                // After the status was calculated, it is not guaranteed that status.size() == locations.size()
                // Therefore for all locations with no status must be set to "NORMAL" in the result
                if (severity == null) {
                    severity = OnmsSeverity.NORMAL;
                }
                info.setSeverityInfo(new SeverityInfo(severity.getId(), severity.getLabel()));
                info.setAlarmUnackedCount(status.getUnacknowledgedAlarmCount(info.getNodeInfo().getNodeId()));
            }
        }
    }

    private Status calculateStatus(GeolocationQuery query, Set<Integer> nodeIds) {
        final NodeStatusCalculatorConfig nodeStatusCalculatorConfig = new NodeStatusCalculatorConfig();
        nodeStatusCalculatorConfig.setIncludeAcknowledgedAlarms(query.isIncludeAcknowledgedAlarms());
        nodeStatusCalculatorConfig.setLocation(query.getLocation());
        if (query.getSeverity() != null) {
            final OnmsSeverity severity = OnmsSeverity.get(query.getSeverity().getId());
            final List<OnmsSeverity> severityFilter = Arrays.stream(OnmsSeverity.values()).filter(s -> s.isGreaterThanOrEqual(severity)).collect(Collectors.toList());
            nodeStatusCalculatorConfig.setSeverities(severityFilter);
        }
        nodeStatusCalculatorConfig.setCalculationStrategy(NodeStatusCalculationStrategy.None);
        if (query.getStatusCalculationStrategy() != null) {
            nodeStatusCalculatorConfig.setCalculationStrategy(NodeStatusCalculationStrategy.valueOf(query.getStatusCalculationStrategy().name()));
        }

        nodeStatusCalculatorConfig.setNodeIds(nodeIds);


        final Status status = nodeStatusCalculator.calculateStatus(nodeStatusCalculatorConfig);
        return status;
    }

    private static OnmsGeolocation geoLocation(OnmsNode node) {
        if (node != null && node.getAssetRecord() != null && node.getAssetRecord().getGeolocation() != null) {
            return node.getAssetRecord().getGeolocation();
        }
        return null;
    }

    private static AddressInfo toAddressInfo(OnmsGeolocation input) {
        if (input != null) {
            AddressInfo addressInfo = new AddressInfo();
            addressInfo.setAddress1(input.getAddress1());
            addressInfo.setAddress2(input.getAddress2());
            addressInfo.setCity(input.getCity());
            addressInfo.setCountry(input.getCountry());
            addressInfo.setState(input.getState());
            addressInfo.setZip(input.getZip());
            return addressInfo;
        }
        return null;
    }
}

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

package org.opennms.features.topology.app.internal.info;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.topology.app.internal.ui.geographical.Coordinates;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class CoordinateResolver {

    private static final Logger LOG = LoggerFactory.getLogger(CoordinateResolver.class);

    static class Result {

        private final Map<Integer, Coordinates> nodeIdToCoordinateMapping;
        private final Map<Integer, OnmsGeolocation> nodeIdToGeolocationMapping;

        public Result(Map<Integer, Coordinates> nodeIdToCoordinateMapping, Map<Integer, OnmsGeolocation> nodeIdToGeolocationMapping) {
            this.nodeIdToCoordinateMapping = nodeIdToCoordinateMapping;
            this.nodeIdToGeolocationMapping = nodeIdToGeolocationMapping;
        }

        public Coordinates getCoordinates(Integer nodeId) {
            return nodeIdToCoordinateMapping.get(nodeId);
        }

        public OnmsGeolocation getGeoLocation(Integer nodeId) {
            return nodeIdToGeolocationMapping.get(nodeId);
        }
    }

    private final GeocoderService geocoderService;

    private final NodeDao nodeDao;

    CoordinateResolver(GeocoderService geocoderService, NodeDao nodeDao) {
        this.geocoderService = Objects.requireNonNull(geocoderService);
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    Result resolve(Collection<Integer> nodeIds) {
        // Fetch the nodes
        final Criteria idCriteria = new CriteriaBuilder(OnmsNode.class).in("id", nodeIds).toCriteria();
        final List<OnmsNode> nodes = nodeDao.findMatching(idCriteria);

        // Separate nodes with longitude/latitude already set, and addresses without longitude/latitude
        final List<OnmsNode> nodesWithGeoLocation = nodes.stream()
                .filter(node -> geoLocation(node) != null)
                .collect(Collectors.toList());
        final List<OnmsNode> nodesWithLongLat = nodesWithGeoLocation.stream()
                .filter(node -> {
                    OnmsGeolocation geoLocation = geoLocation(node);
                    return geoLocation != null && geoLocation.getLatitude() != null && geoLocation.getLongitude() != null;
                })
                .collect(Collectors.toList());
        final List<OnmsNode> nodesWithAddress = nodesWithGeoLocation.stream()
                .filter(node -> !Strings.isNullOrEmpty(geoLocation(node).asAddressString()))
                .collect(Collectors.toList());

        // Retrieve coordinates for nodes with a adress, but not logitude/latitude
        Map<Integer, Coordinates> nodeIdToCoordinateMapping = new HashMap<>();
        for (OnmsNode eachNode : nodesWithAddress) {
            final String addressString = geoLocation(eachNode).asAddressString();
            try {
                org.opennms.features.geocoder.Coordinates coordinates = geocoderService.getCoordinates(addressString);
                if (coordinates != null) {
                    nodeIdToCoordinateMapping.put(eachNode.getId(), new Coordinates(coordinates.getLongitude(), coordinates.getLatitude()));
                }
            } catch (GeocoderException e) {
                LOG.warn("Couldn't resolve address '%s' for node id: %s, label: %s'", addressString, eachNode.getId(), eachNode.getLabel(), e);
            }
        }

        // Add nodesWithLongLat to node id to coordinate mapping
        nodesWithLongLat.forEach(eachNode -> {
            OnmsGeolocation geoLocation = geoLocation(eachNode);
            nodeIdToCoordinateMapping.put(eachNode.getId(), new Coordinates(geoLocation.getLongitude(), geoLocation.getLatitude()));
        });
        return new Result(nodeIdToCoordinateMapping, nodesWithGeoLocation.stream().collect(Collectors.toMap(node -> node.getId(), node -> geoLocation(node))));
    }

    private static OnmsGeolocation geoLocation(OnmsNode node) {
        if (node != null && node.getAssetRecord() != null && node.getAssetRecord().getGeolocation() != null) {
            return node.getAssetRecord().getGeolocation();
        }
        return null;
    }
}

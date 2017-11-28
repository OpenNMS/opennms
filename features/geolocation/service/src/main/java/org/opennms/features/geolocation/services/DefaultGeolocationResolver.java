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

package org.opennms.features.geolocation.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.geolocation.api.Coordinates;
import org.opennms.features.geolocation.api.GeolocationResolver;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class DefaultGeolocationResolver implements GeolocationResolver {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGeolocationResolver.class);

    private final GeocoderService geocoderService;

    private final NodeDao nodeDao;

    public DefaultGeolocationResolver(GeocoderService geocoderService, NodeDao nodeDao) {
        this.geocoderService = Objects.requireNonNull(geocoderService);
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    @Override
    public Map<Integer, Coordinates> resolve(Collection<Integer> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return new HashMap<>(); // nothing to do
        }

        // Lookup all nodes and gather the address string
        final Criteria criteria = new CriteriaBuilder(OnmsNode.class).in("id", nodeIds).toCriteria();
        final Map<Integer, String> nodeIdAddressMap = nodeDao.findMatching(criteria).stream()
                .filter(n -> getGeoLocation(n) != null)
                .filter(n -> getGeoLocation(n).getLatitude() == null && getGeoLocation(n).getLongitude() == null)
                .filter(n -> !Strings.isNullOrEmpty(getGeoLocation(n).asAddressString()))
                .collect(Collectors.toMap(OnmsNode::getId, n -> n.getAssetRecord().getGeolocation().asAddressString()));
        return resolve(nodeIdAddressMap);
    }

    @Override
    public Map<Integer, Coordinates> resolve(Map<Integer, String> nodeIdAddressMap) {
        if (nodeIdAddressMap == null || nodeIdAddressMap.isEmpty()) {
            return new HashMap<>(); // nothing to do
        }

        // 1st filter out invalid values
        nodeIdAddressMap = nodeIdAddressMap.entrySet().stream()
                .filter(e -> !Strings.isNullOrEmpty(e.getValue()) && e.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 2nd Resolve longitude/latitude coordinates from an address string
        final Map<Integer, Coordinates> resultMap = new HashMap<>();
        nodeIdAddressMap.entrySet().stream()
                .forEach(entry -> {
                    final String addressString = entry.getValue();
                    final Coordinates coordinates = resolve(addressString);
                    if (coordinates != null) {
                        resultMap.put(entry.getKey(), coordinates);
                    }
                });
        return resultMap;
    }

    @Override
    public Coordinates resolve(String addressString) {
        try {
            org.opennms.features.geocoder.Coordinates coordinates = geocoderService.getCoordinates(addressString);
            if (coordinates != null) {
                return new Coordinates(coordinates.getLongitude(), coordinates.getLatitude());
            } else {
                LOG.warn("Couldn't resolve address '{}'", addressString);
            }
        } catch (GeocoderException e) {
            LOG.warn("Couldn't resolve address '{}'", addressString, e);
        }
        return null;
    }

    private static OnmsGeolocation getGeoLocation(OnmsNode node) {
        if (node != null && node.getAssetRecord() != null && node.getAssetRecord().getGeolocation() != null) {
            return node.getAssetRecord().getGeolocation();
        }
        return null;
    }
}

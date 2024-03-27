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
package org.opennms.features.geolocation.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.geocoder.GeocoderConfigurationException;
import org.opennms.features.geocoder.GeocoderResult;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.geocoder.GeocoderServiceManager;
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

    private final NodeDao nodeDao;
    private final GeocoderServiceManager geocoderServiceManager;

    public DefaultGeolocationResolver(GeocoderServiceManager geocoderServiceManager, NodeDao nodeDao) {
        this.geocoderServiceManager = Objects.requireNonNull(geocoderServiceManager);
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
        final GeocoderService activeGeocoder = geocoderServiceManager.getActiveGeocoderService();
        if (activeGeocoder == null) {
            LOG.warn("Error resolving address '{}': No active Geocoder", addressString);
            return null;
        }
        try {
            final GeocoderResult result = activeGeocoder.resolveAddress(addressString);
            if (result.hasError()) {
                LOG.error("Error resolving address '{}': {}", addressString, result.getThrowable().getMessage(), result.getThrowable());
                return null;
            }
            if (result.isEmpty()) {
                LOG.warn("Error resolving address '{}': Response was empty", addressString);
                return null;
            }
            org.opennms.features.geocoder.Coordinates coordinates = result.getCoordinates();
            LOG.debug("Successfully resolved address '{}': Active Geocoder with id '{}' resolved to long/lat: {}/{}", addressString, activeGeocoder.getId(), coordinates.getLongitude(), coordinates.getLatitude());
            return new Coordinates(coordinates.getLongitude(), coordinates.getLatitude());
        } catch (GeocoderConfigurationException ex) {
            LOG.warn("Error resolving address '{}': Active Geocoder with id '{}' is not configured properly", addressString, activeGeocoder.getId(), ex);
            return null;
        } catch (Exception ex) {
            LOG.warn("Error resolving address '{}': An unexpected exception occurred", addressString, ex);
            return null;
        }
    }

    private static OnmsGeolocation getGeoLocation(OnmsNode node) {
        if (node != null && node.getAssetRecord() != null && node.getAssetRecord().getGeolocation() != null) {
            return node.getAssetRecord().getGeolocation();
        }
        return null;
    }
}

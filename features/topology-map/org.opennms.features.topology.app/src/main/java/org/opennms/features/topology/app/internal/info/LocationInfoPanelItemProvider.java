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
package org.opennms.features.topology.app.internal.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.geolocation.api.AddressInfo;
import org.opennms.features.geolocation.api.GeolocationConfiguration;
import org.opennms.features.geolocation.api.GeolocationInfo;
import org.opennms.features.geolocation.api.GeolocationQueryBuilder;
import org.opennms.features.geolocation.api.GeolocationService;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.InfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.ui.geographical.LocationComponent;
import org.opennms.features.topology.app.internal.ui.geographical.LocationConfiguration;
import org.opennms.features.topology.app.internal.ui.geographical.Marker;

import com.google.common.base.Strings;
import com.vaadin.server.Sizeable;

public class LocationInfoPanelItemProvider implements InfoPanelItemProvider {

    private final GeolocationService geolocationService;

    private final GeolocationConfiguration geolocationConfiguration;

    public LocationInfoPanelItemProvider(GeolocationService geolocationService, GeolocationConfiguration geolocationConfiguration) {
        this.geolocationService = geolocationService;
        this.geolocationConfiguration = geolocationConfiguration;
    }

    @Override
    public Collection<? extends InfoPanelItem> getContributions(GraphContainer container) {
        final List<Vertex> vertices = new ArrayList<>(container.getGraph().getDisplayVertices());
        final Set<Integer> nodeIds = vertices.stream()
                .filter(v -> v.getNodeID() != null)
                .map(Vertex::getNodeID)
                .collect(Collectors.toSet());
        if (nodeIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<GeolocationInfo> locations = geolocationService.getLocations(new GeolocationQueryBuilder()
                .withNodeIds(nodeIds)
                .build());
        final List<Marker> markers = locations.stream()
                .filter(locationInfo -> locationInfo.getCoordinates() != null)
                .map(locationInfo -> {
                    final Vertex vertex = vertices.stream()
                            .filter(v -> v.getNodeID() != null && locationInfo.getNodeInfo().getNodeId() == v.getNodeID())
                            .findFirst()
                            .get();
                    return new Marker(
                            locationInfo.getCoordinates(),
                            createTooltip(vertex, locationInfo.getAddressInfo()),
                            container.getSelectionManager().isVertexRefSelected(vertex));
                }).collect(Collectors.toList());

        if (!markers.isEmpty()) {
            final LocationConfiguration config = new LocationConfiguration()
                    .withTileLayer(geolocationConfiguration.getTileServerUrl())
                    .withMarker(markers)
                    .withInitialZoom(10)
                    .withLayerOptions(geolocationConfiguration.getOptions());

            final LocationComponent locationComponent = new LocationComponent(config, "mapId-" + getClass().getSimpleName().toLowerCase());
            locationComponent.setWidth(300, Sizeable.Unit.PIXELS);
            locationComponent.setHeight(300, Sizeable.Unit.PIXELS);

            return Collections.singleton(
                    new DefaultInfoPanelItem()
                        .withTitle(String.format("Geolocation (%d/%d)", markers.size(), vertices.size()))
                        .withOrder(1)
                        .withComponent(locationComponent));
        }
        return Collections.emptyList();
    }

    private String createTooltip(Vertex vertex, AddressInfo addressInfo) {
        final StringBuilder tooltip = new StringBuilder();
        tooltip.append(String.format("<b>%s</b>", vertex.getLabel()));

        if (addressInfo != null) {
            append(tooltip, "City", addressInfo.getCity());
            append(tooltip, "Zip", addressInfo.getZip());
            append(tooltip, "Address", addressInfo.getAddress1());
            append(tooltip, "", addressInfo.getAddress2());
            append(tooltip, "State", addressInfo.getState());
            append(tooltip, "Country", addressInfo.getCountry());
        }
        return tooltip.toString();
    }

    private static void append(StringBuilder builder, String key, String value) {
        if (!Strings.isNullOrEmpty(value)) {
            builder.append(String.format("<br/><b>%s</b> %s", key, value));
        }
    }
}

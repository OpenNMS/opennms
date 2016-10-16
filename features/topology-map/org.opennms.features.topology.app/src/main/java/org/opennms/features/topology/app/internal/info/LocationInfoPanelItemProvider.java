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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.geo.GeocoderConfig;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.InfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.ui.geographical.Coordinates;
import org.opennms.features.topology.app.internal.ui.geographical.LocationComponent;
import org.opennms.features.topology.app.internal.ui.geographical.LocationConfiguration;
import org.opennms.features.topology.app.internal.ui.geographical.Marker;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsGeolocation;

import com.google.common.base.Strings;
import com.vaadin.server.Sizeable;

public class LocationInfoPanelItemProvider implements InfoPanelItemProvider {

    private final GeocoderService geocoderService;
    private final NodeDao nodeDao;

    public LocationInfoPanelItemProvider(NodeDao nodeDao, GeocoderService geocoderService) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.geocoderService = Objects.requireNonNull(geocoderService);
    }

    @Override
    public Collection<? extends InfoPanelItem> getContributions(GraphContainer container) {
        final List<Vertex> vertices = new ArrayList<>(container.getGraph().getDisplayVertices());
        final Set<Integer> nodeIds = vertices.stream()
                .filter(v -> v.getNodeID() != null)
                .map(v -> v.getNodeID())
                .collect(Collectors.toSet());

        if (nodeIds.isEmpty()) {
            return Collections.emptyList();
        }
        final CoordinateResolver.Result result = new CoordinateResolver(geocoderService, nodeDao).resolve(nodeIds);
        final List<Marker> markers = vertices.stream()
                .filter(v -> result.getCoordinates(v.getNodeID()) != null)
                .map(v -> {
                    Coordinates coordinates = result.getCoordinates(v.getNodeID());
                    OnmsGeolocation geolocation = result.getGeoLocation(v.getNodeID());
                    return new Marker(
                            coordinates,
                            createTooltip(v, coordinates, geolocation),
                            container.getSelectionManager().isVertexRefSelected(v));
                }).collect(Collectors.toList());

        if (!markers.isEmpty()) {
            final LocationConfiguration config = new LocationConfiguration()
                    .withTileLayer(GeocoderConfig.getTileServerUrl())
                    .withMarker(markers)
                    .withInitialZoom(10)
                    .withLayerOptions(GeocoderConfig.getOptions());

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

    private String createTooltip(Vertex vertex, Coordinates coordinates, OnmsGeolocation geolocation) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append(String.format("<b>%s</b>", vertex.getLabel()));

        if (geolocation != null) {
            append(tooltip, "City", geolocation.getCity());
            append(tooltip, "Zip", geolocation.getZip());
            append(tooltip, "Address", geolocation.getAddress1());
            append(tooltip, "", geolocation.getAddress2());
            append(tooltip, "State", geolocation.getState());
            append(tooltip, "Country", geolocation.getCountry());

        }
        return tooltip.toString();
    }

    private static void append(StringBuilder builder, String key, String value) {
        if (!Strings.isNullOrEmpty(value)) {
            builder.append(String.format("<br/><b>%s</b> %s", key, value));
        }
    }
}

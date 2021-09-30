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

package org.opennms.features.topology.app.internal.ui.geographical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

public class LocationConfiguration {

    private Map<String, String> layerOptions = Maps.newHashMap();
    private String tileLayer;
    private int initialZoom;
    private List<Marker> marker;

    public Map<String, String> getLayerOptions() {
        return layerOptions;
    }

    public LocationConfiguration withLayerOptions(Map<String, String> layerOptions) {
        this.layerOptions = layerOptions;
        return this;
    }

    public String getTileLayer() {
        return tileLayer;
    }

    public LocationConfiguration withTileLayer(String tileLayer) {
        this.tileLayer = tileLayer;
        return this;
    }

    public void validate() {
        Objects.requireNonNull(layerOptions);
        Objects.requireNonNull(tileLayer);
    }

    public LocationConfiguration withInitialZoom(int initialZoom) {
        this.initialZoom = initialZoom;
        return this;
    }

    public int getInitialZoom() {
        return initialZoom;
    }

    public LocationConfiguration withMarker(List<Marker> marker) {
        this.marker = Objects.requireNonNull(marker);
        return this;
    }

    public List<Marker> getMarker() {
        return new ArrayList<>(marker);
    }
}

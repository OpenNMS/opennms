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

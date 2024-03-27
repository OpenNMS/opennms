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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

@JavaScript({
    "theme://../opennms/assets/location-component_connector.vaadin.js"
})
public class LocationComponent extends AbstractJavaScriptComponent {
    private static final long serialVersionUID = 1L;

    public LocationComponent(LocationConfiguration configuration, String uniqueId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(uniqueId), "The uniqueId must not be null or empty");
        configuration.validate();

        getState().tileLayer = configuration.getTileLayer();
        getState().mapId = uniqueId;
        getState().markers = configuration.getMarker();
        getState().layerOptions = configuration.getLayerOptions();
        getState().initialZoom = configuration.getInitialZoom();
    }

    @Override
    public LocationState getState() {
        return (LocationState) super.getState();
    }

}

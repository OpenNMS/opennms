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

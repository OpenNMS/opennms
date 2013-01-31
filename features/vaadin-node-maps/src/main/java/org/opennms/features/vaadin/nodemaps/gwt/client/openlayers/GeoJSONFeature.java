/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.gwt.client.openlayers;

import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;

public class GeoJSONFeature extends JavaScriptObject {
    protected GeoJSONFeature() {
    }

    public static GeoJSONFeature create(final Float longitude, final Float latitude, final Map<String, String> stringProperties, final Map<String, Integer> intProperties) {
        final GeoJSONProperties props = GeoJSONProperties.create();
        for (final Map.Entry<String, String> entry : stringProperties.entrySet()) {
            props.setString(entry.getKey(), entry.getValue());
        }
        for (final Map.Entry<String, Integer> entry : intProperties.entrySet()) {
            props.setInteger(entry.getKey(), entry.getValue());
        }
        return GeoJSONFeature.create(longitude, latitude, props);
    }

    public static native GeoJSONFeature create(final Float longitude, final Float latitude, final JavaScriptObject properties) /*-{
        var props = properties || {};
        return {
            "type" : "Feature",
            "properties" : props,
            "geometry" : {
                "type" : "Point",
                "coordinates" : [ latitude, longitude ]
            },
        };
    }-*/;

    public native final Float getLongitude() /*-{
        return this.geometry.coordinates[1];
    }-*/;

    public native final Float getLatitude() /*-{
        return this.geometry.coordinates[0];
    }-*/;
}

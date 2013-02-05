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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.vaadin.terminal.gwt.client.VConsole;

public class FeatureCollection extends JavaScriptObject {
    protected FeatureCollection() {
    }

    public static native FeatureCollection create() /*-{
        return {
            "type" : "FeatureCollection",
            "features" : []
        };
    }-*/;

    public native final void add(final GeoJSONFeature feature) /*-{
        this.features.push(feature);
    }-*/;

    public native final JsArray<GeoJSONFeature> getFeatures() /*-{
        return this.features;
    }-*/;

    public final void logFeatures() {
        final JsArray<GeoJSONFeature> features = getFeatures();
        for (int i = 0; i < features.length(); i++) {
            final GeoJSONFeature feature = features.get(i);
            VConsole.log("feature: lat=" + feature.getLatitude() + ", lon=" + feature.getLongitude());
        }
    }
}

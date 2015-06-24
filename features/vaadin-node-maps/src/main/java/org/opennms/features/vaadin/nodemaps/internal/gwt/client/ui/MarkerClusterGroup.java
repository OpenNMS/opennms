/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.List;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.layers.others.FeatureGroup;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkerClusterEventCallback;

import com.google.gwt.core.client.JsArray;

public class MarkerClusterGroup extends FeatureGroup {
    public MarkerClusterGroup(final JSObject element) {
        super(element);
    }

    public MarkerClusterGroup() {
        this(MarkerClusterGroupImpl.create(JSObject.createJSObject()));
    }

    public MarkerClusterGroup(final Options options) {
        this(MarkerClusterGroupImpl.create(options.getJSObject()));
    }

    @Override
    public MarkerClusterGroup addLayer(final ILayer layer) {
        MarkerClusterGroupImpl.addLayer(getJSObject(), layer.getJSObject());
        return this;
    }

    public MarkerClusterGroup addLayers(final List<ILayer> layers) {
        final JsArray<JSObject> layerArray = JsArray.createArray().cast();
        for (final ILayer marker : layers) {
            layerArray.push(marker.getJSObject());
        }
        MarkerClusterGroupImpl.addLayers(getJSObject(), layerArray);
        return this;
    }

    @Override
    public MarkerClusterGroup bindPopup(final String htmlContent, final Options options) {
        MarkerClusterGroupImpl.bindPopup(getJSObject(), htmlContent, options.getJSObject());   
        return this;
    }

    public MarkerClusterGroup on(final String event, final MarkerClusterEventCallback callback) {
        MarkerClusterGroupImpl.on(getJSObject(), event, callback);
        return this;
    }

    public final JSObject getMapObject() {
        return MarkerClusterGroupImpl.getMapObject(getJSObject());
    }

    public boolean hasLayer(final ILayer layer) {
        return MarkerClusterGroupImpl.hasLayer(getJSObject(), layer.getJSObject());
    }
}

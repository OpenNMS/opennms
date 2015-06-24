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

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkerClusterEventCallback;

import com.google.gwt.core.client.JsArray;


public abstract class MarkerClusterGroupImpl {
    public static native JSObject create(final JSObject options)/*-{
        return new $wnd.L.MarkerClusterGroup(options);
    }-*/;

    public static native void clearLayers(final JSObject self) /*-{
        self.clearLayers();
    }-*/;

    public static native void addLayer(final JSObject self, final JSObject marker) /*-{
        self.addLayer(marker);
    }-*/;

    public static native void addLayers(final JSObject self, final JsArray<JSObject> markers) /*-{
        self.addLayers(markers);
    }-*/;

    public static native void on(final JSObject self, final String event, final MarkerClusterEventCallback callback) /*-{
        self.on(event, callback.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkerClusterEventCallback::run(Lorg/opennms/features/vaadin/nodemaps/internal/gwt/client/event/MarkerClusterEvent;));
    }-*/;

    public static native void bindPopup(final JSObject self, final String htmlContent, final JSObject options) /*-{
        self.bindPopup(htmlContent, options);
    }-*/;

    public static native JSObject getMapObject(final JSObject self) /*-{
        return self._map;
    }-*/;

    public static native boolean hasLayer(final JSObject self, final JSObject layer) /*-{
        return self.hasLayer(layer);
    }-*/;
}

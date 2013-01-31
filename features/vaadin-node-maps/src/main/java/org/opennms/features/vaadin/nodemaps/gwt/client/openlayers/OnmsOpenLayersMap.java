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

public class OnmsOpenLayersMap extends JavaScriptObject {
    protected OnmsOpenLayersMap() {
    }

    public static OnmsOpenLayersMap newInstance(final String divId) {
        return createJso(divId);
    }

    private static native OnmsOpenLayersMap createJso(final String divId) /*-{
        var map = new $wnd.OpenLayers.Map({
            div : divId,
            displayProjection : "EPSG:900913",
            projection : "EPSG:4326",
            controls : [
                new $wnd.OpenLayers.Control.Navigation(),
                new $wnd.OpenLayers.Control.PanZoomBar(),
                new $wnd.OpenLayers.Control.LayerSwitcher(),
                new $wnd.OpenLayers.Control.MousePosition()
            ]
        });

        // Main Layer

        map.addLayer(new $wnd.OpenLayers.Layer.Google("GoogleMaps", {
            sphericalMercator : true
        }));
        map.addLayer(new $wnd.OpenLayers.Layer.OSM("OpenStreetMaps"));
        map.addLayer(new $wnd.OpenLayers.Layer.XYZ(
            "MapQuest",
            [
                "http://otile1.mqcdn.com/tiles/1.0.0/osm/${z}/${x}/${y}.png",
                "http://otile2.mqcdn.com/tiles/1.0.0/osm/${z}/${x}/${y}.png",
                "http://otile3.mqcdn.com/tiles/1.0.0/osm/${z}/${x}/${y}.png",
                "http://otile4.mqcdn.com/tiles/1.0.0/osm/${z}/${x}/${y}.png"
            ],
            {
                attribution : "Data, imagery and map information provided by <a href='http://www.mapquest.com/'  target='_blank'>MapQuest</a>, <a href='http://www.openstreetmap.org/' target='_blank'>Open Street Map</a> and contributors, <a href='http://creativecommons.org/licenses/by-sa/2.0/' target='_blank'>CC-BY-SA</a>  <img src='http://developer.mapquest.com/content/osm/mq_logo.png' border='0'>",
                transitionEffect : "resize",
                sphericalMercator : true
            }
        ));

        return map;
    }-*/;
}
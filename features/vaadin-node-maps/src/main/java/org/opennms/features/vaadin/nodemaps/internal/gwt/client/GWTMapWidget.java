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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.controls.zoom.Zoom;
import org.discotools.gwt.leaflet.client.crs.epsg.EPSG3857;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.layers.raster.TileLayer;
import org.discotools.gwt.leaflet.client.map.Map;
import org.discotools.gwt.leaflet.client.map.MapOptions;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.discotools.gwt.leaflet.client.types.LatLngBounds;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.VConsole;

public class GWTMapWidget extends Widget implements MarkerProvider {
    private final DivElement m_div;

    private Map m_map;
    private ILayer m_layer;

    private List<NodeMarker> m_markers;

    private MarkerClusterGroup m_markerClusterGroup;

    private boolean m_firstUpdate = true;

    public GWTMapWidget() {
        super();
        m_div = Document.get().createDivElement();
        m_div.setId("gwt-map");
        m_div.getStyle().setWidth(100, Unit.PCT);
        m_div.getStyle().setHeight(100, Unit.PCT);
        setElement(m_div);
        VConsole.log("GWTMapWidget initialized");
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        Scheduler.get().scheduleDeferred(new Command() {
            @Override public void execute() {
                initializeMap(m_div.getId());
            }
        });
    }

    @Override
    protected void onUnload() {
        destroyMap();
        super.onUnload();
    }

    private void initializeMap(final String divId) {
        VConsole.log("initializing map");

        createMap(divId);
        // createGoogleLayer();
        addTileLayer();
        addMarkerLayer();
        addSearchInput();
        //addSearchControl();
        addZoomControl();

        VConsole.log("finished initializing map");
    }

    @SuppressWarnings("unused")
    private void createGoogleLayer() {
        final EPSG3857 projection = new EPSG3857();
        final Options googleOptions = new Options();
        googleOptions.setProperty("crs", projection);

        VConsole.log("adding Google layer");
        m_layer = new GoogleLayer("SATELLITE", googleOptions);
        m_map.addLayer(m_layer, true);
    }

    private void createMap(final String divId) {
        final MapOptions options = new MapOptions();
        options.setCenter(new LatLng(0, 0));
        options.setProperty("zoomControl", false);
        options.setZoom(1);
        m_map = new Map(divId, options);
    }

    private void addTileLayer() {
        VConsole.log("adding tile layer");
        final String attribution = "Map data &copy; <a href=\"http://openstreetmap.org\">OpenStreetMap</a> contributors, <a href=\"http://creativecommons.org/licenses/by-sa/2.0/\">CC-BY-SA</a>, Tiles &copy; <a href=\"http://www.mapquest.com/\" target=\"_blank\">MapQuest</a> <img src=\"http://developer.mapquest.com/content/osm/mq_logo.png\" />";
        final String url = "http://otile{s}.mqcdn.com/tiles/1.0.0/{type}/{z}/{x}/{y}.png";
        final Options tileOptions = new Options();
        tileOptions.setProperty("attribution", attribution);
        tileOptions.setProperty("subdomains", "1234");
        tileOptions.setProperty("type", "osm");
        m_layer = new TileLayer(url, tileOptions);
        m_map.addLayer(m_layer, true);
    }
    
    private void addMarkerLayer() {
        VConsole.log("adding marker cluster layer");
        final Options markerClusterOptions = new Options();
        markerClusterOptions.setProperty("zoomToBoundsOnClick", false);
        markerClusterOptions.setProperty("iconCreateFunction", new IconCreateCallback());
        m_markerClusterGroup = new MarkerClusterGroup(markerClusterOptions);
        final NodeMarkerClusterCallback callback = new NodeMarkerClusterCallback();
        m_markerClusterGroup.on("clusterclick", callback);
        m_markerClusterGroup.on("clustertouchend", callback);
        m_map.addLayer(m_markerClusterGroup);
    }

    private void addSearchInput() {
        VConsole.log("adding search input");
        final SearchOptions options = new SearchOptions();
        options.setSearchCallback(new NodeMarkerSearchCallback(this) {
           public Collection<NodeMarker> search(final Collection<NodeMarker> markers, final String text) {
               VConsole.log("search() called for text: " + text + ", searching " + markers.size() + " markers.");
               final List<NodeMarker> matched = new ArrayList<NodeMarker>();
               for (final NodeMarker marker : markers) {
                   if (marker.containsText(text)) {
                       VConsole.log(" matched: " + marker.toString());
                       matched.add(marker);
                   } else {
                       VConsole.log("!matched: " + marker.toString());
                   }
               }
               return matched;
           }
        });
        options.setAutoCollapse(false);
        options.setAutoResize(false);
        options.setTipAutoSubmit(true);
        options.setAnimateLocation(true);
        options.setMarkerLocation(true);
        options.setPosition("topleft");
        options.setInitial(false);
        final Search search = new Search(options);
        m_map.addControl(search);
        search.setSize(40);
        search.expand();
        search.focus();
    }

    private void addSearchControl() {
        VConsole.log("adding search control");
        m_map.addControl(new SearchControl(this));
    }
    private void addZoomControl() {
        VConsole.log("adding zoom control");
        m_map.addControl(new Zoom(new Options()));
    }

    public void updateMarkerClusterLayer() {
        if (m_markers == null) {
            VConsole.log("markers not initialized yet, skipping update");
            return;
        }
        if (m_markerClusterGroup == null) {
            VConsole.log("marker cluster not initialized yet, skipping update");
            return;
        }

        VConsole.log("clearing existing markers");
        m_markerClusterGroup.clearLayers();
        VConsole.log("adding " + m_markers.size() + " markers to the node layer");
        m_markerClusterGroup.addLayers(m_markers);
        VConsole.log("finished adding markers");

        if (m_firstUpdate && m_markers.size() > 0) {
            final LatLngBounds bounds = new LatLngBounds();
            for (final NodeMarker marker : m_markers) {
                bounds.extend(marker.getLatLng());
            }
            VConsole.log("first update, zooming to "+ bounds.toBBoxString());
            m_map.fitBounds(bounds);
            m_firstUpdate = false;
        } else {
            VConsole.log("Skipping zoom, we've already done it once.");
        }

        VConsole.log("finished updating marker cluster layer");
    }

    public List<NodeMarker> getMarkers() {
        return m_markers;
    }

    public void setMarkers(final List<NodeMarker> markers) {
        m_markers = markers;
    }

    private final void destroyMap() {
        m_markerClusterGroup.clearLayers();
        m_map.removeLayer(m_markerClusterGroup);
        m_map.removeLayer(m_layer);
        m_map = null;
    }

}

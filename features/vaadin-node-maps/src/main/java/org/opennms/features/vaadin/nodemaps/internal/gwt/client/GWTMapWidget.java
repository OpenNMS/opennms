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

import java.util.List;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.crs.epsg.EPSG3857;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.layers.raster.TileLayer;
import org.discotools.gwt.leaflet.client.map.Map;
import org.discotools.gwt.leaflet.client.map.MapOptions;
import org.discotools.gwt.leaflet.client.marker.Marker;
import org.discotools.gwt.leaflet.client.popup.Popup;
import org.discotools.gwt.leaflet.client.popup.PopupImpl;
import org.discotools.gwt.leaflet.client.popup.PopupOptions;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.leaflet.GoogleLayer;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.leaflet.MarkerClusterGroup;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.leaflet.NodeMarker;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.VConsole;

public class GWTMapWidget extends Widget {
    private static final class NodeMarkerClusterCallback implements MarkerClusterEventCallback {
        private NodeMarkerClusterCallback() {
        }

        @Override
        public final void run(final MarkerClusterEvent event) {
            final StringBuilder sb = new StringBuilder();
            final MarkerCluster cluster = event.getMarkerCluster();
            @SuppressWarnings("unchecked")
            final List<NodeMarker> markers = (List<NodeMarker>)cluster.getAllChildMarkers();
            VConsole.log("Clicked, processing " + markers.size() + " markers.");
            if (markers.size() == 1) {
                final NodeMarker marker = markers.get(0);
                sb.append("<h2>Node ").append(marker.getNodeLabel()).append("</h2>");
                sb.append("<p>");
                sb.append("Node ID: ").append(marker.getNodeId()).append("<br/>");
                sb.append("Foreign Source: ").append(marker.getForeignSource()).append("<br/>");
                sb.append("Foreign ID: ").append(marker.getForeignId()).append("<br/>");
                sb.append("IP Address: ").append(marker.getIpAddress()).append("<br/>");
                sb.append("Severity: ").append(marker.getSeverityLabel());
                sb.append("</p>");
            } else {
                final StringBuilder nodeBuilder = new StringBuilder();
                int unacked = 0;
                for (final NodeMarker marker : markers) {
                    unacked += marker.getUnackedCount();
                    nodeBuilder.append("<li>");
                    nodeBuilder.append(marker.getNodeLabel()).append(" ");
                    nodeBuilder.append("(").append(marker.getIpAddress()).append(")").append(": ");
                    nodeBuilder.append(marker.getSeverityLabel());
                    nodeBuilder.append("</li>");
                }
                sb.append("<h2># of nodes: ").append(markers.size()).append(" ");
                sb.append("(").append(unacked).append(" Unacknowledged Alarms)");
                sb.append("</h2>");
                sb.append("<ul>").append(nodeBuilder).append("</ul>");
            }
            final PopupOptions options = new PopupOptions();
            options.setMaxWidth(500);
            options.setProperty("maxHeight", 250);
            final Popup popup = new Popup(options);
            popup.setContent(sb.toString());
            popup.setLatLng(cluster.getLatLng());
            VConsole.log("html = " + sb.toString());
            PopupImpl.openOn(popup.getJSObject(), cluster.getGroup().getMapObject());
        }
    }

    private final DivElement m_div;

    private Map m_map;
    private ILayer m_layer;

    private List<? extends Marker> m_features;

    private MarkerClusterGroup m_markerClusterGroup;

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
//        m_markerClusterGroup.bindPopup("Test Popup", new Options());
        
        m_markerClusterGroup.on("clusterclick", new NodeMarkerClusterCallback());
        m_map.addLayer(m_markerClusterGroup);
    }

    public void updateFeatureLayer() {
        if (m_features == null) {
            VConsole.log("features not initialized yet, skipping update");
            return;
        }
        if (m_markerClusterGroup == null) {
            VConsole.log("marker cluster not initialized yet, skipping update");
            return;
        }

        VConsole.log("clearing existing markers");
        m_markerClusterGroup.clearLayers();
        VConsole.log("adding " + m_features.size() + " features to the node layer");
        m_markerClusterGroup.addLayers(m_features);
        VConsole.log("finished adding features");
    }

    public List<? extends Marker> getFeatureCollection() {
        return m_features;
    }

    public void setFeatureCollection(final List<? extends Marker> featureCollection) {
        VConsole.log("setFeatureCollection: " + featureCollection.size() + " features");
        m_features = featureCollection;
    }

    private final void destroyMap() {
        m_markerClusterGroup.clearLayers();
        m_map.removeLayer(m_markerClusterGroup);
        m_map.removeLayer(m_layer);
        m_map = null;
    }

}

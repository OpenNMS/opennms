/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.List;
import java.util.ListIterator;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.controls.zoom.Zoom;
import org.discotools.gwt.leaflet.client.crs.epsg.EPSG3857;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.layers.raster.TileLayer;
import org.discotools.gwt.leaflet.client.map.MapOptions;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.discotools.gwt.leaflet.client.types.LatLngBounds;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.Map;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MarkerProvider;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchConsumer;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.IconCreateCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.NodeMarkerClusterCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm.AlarmControl;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm.AlarmControlOptions;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.SearchControl;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.VConsole;

public class GWTMapWidget extends Widget implements MarkerProvider, SearchConsumer {
    private final DivElement m_div;

    private Map m_map;

    private ILayer m_layer;

    private MarkerContainer m_markers;

    private MarkerClusterGroup m_markerClusterGroup;

    private boolean m_firstUpdate = true;

    private int m_minimumSeverity = 0;

    private String m_searchString = "";

    private SearchControl m_searchControl;

    private MarkerFilter m_filter;

    public GWTMapWidget() {
        super();
        m_div = Document.get().createDivElement();
        m_div.setId("gwt-map");
        m_div.getStyle().setWidth(100, Unit.PCT);
        m_div.getStyle().setHeight(100, Unit.PCT);
        setElement(m_div);

        m_filter = new MarkerFilter() {
            @Override
            public boolean matches(final NodeMarker marker) {
                if (marker.getSeverity() < m_minimumSeverity)
                    return false;
                if (m_searchString == null || "".equals(m_searchString))
                    return true;

                final String searchString = m_searchString.toLowerCase();
                // VConsole.log("searching: search string = " + searchString);

                // /// handle foo: style search strings for text properties
                // VConsole.log("checking property:search");
                for (final String propertyName : marker.getTextPropertyNames()) {
                    final String lowerPropertyName = propertyName.toLowerCase();
                    if (searchString.startsWith(lowerPropertyName + ":")) {
                        final String searchStringWithoutPrefix = searchString.replaceFirst(lowerPropertyName + ":\\s*", "");
                        final String propertyValue = marker.getProperty(propertyName);
                        if (propertyValue != null && propertyValue.toLowerCase().contains(searchStringWithoutPrefix)) {
                            return true;
                        } else {
                            return false;
                        }
                    } else if (searchString.startsWith(lowerPropertyName + "=")) {
                        final String searchStringWithoutPrefix = searchString.replaceFirst(lowerPropertyName + "=\\s*", "");
                        final String propertyValue = marker.getProperty(propertyName);
                        if (propertyValue != null && propertyValue.toLowerCase().equals(searchStringWithoutPrefix)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }

                // /// special case: categories: -> category: search
                if (searchString.startsWith("category:") || searchString.startsWith("categories:")) {
                    final String searchStringWithoutPrefix = searchString.replaceFirst("categor(ies|y):\\s*", "");
                    final MatchType match = matchCategories(marker, searchStringWithoutPrefix);
                    return match != MatchType.NONE;
                } else if (searchString.startsWith("category=") || searchString.startsWith("categories=")) {
                    final String searchStringWithoutPrefix = searchString.replaceFirst("categor(ies|y)=\\s*", "");
                    final MatchType match = matchCategories(marker, searchStringWithoutPrefix);
                    return match == MatchType.EXACT;
                }

                // /// if no foo: style search strings, first search all text
                // properties for a match
                for (final String propertyName : marker.getTextPropertyNames()) {
                    final String value = marker.getProperty(propertyName);
                    if (value != null) {
                        final String propertyValue = value.toLowerCase();
                        if (propertyValue.contains(searchString)) {
                            return true;
                        }
                    }
                }

                // /// otherwise, search categories
                return matchCategories(marker, searchString) != MatchType.NONE;
            }

            private MatchType matchCategories(final NodeMarker marker, final String searchString) {
                final JsArrayString categories = marker.getCategories();
                for (int i = 0; i < categories.length(); i++) {
                    final String category = categories.get(i).toLowerCase();
                    if (category.equals(searchString)) {
                        return MatchType.EXACT;
                    } else if (category.contains(searchString)) {
                        return MatchType.SUBSTRING;
                    }
                }
                return MatchType.NONE;
            }

        };
        m_markers = new MarkerContainer(m_filter);
        VConsole.log("GWTMapWidget initialized");
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
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

        // overlay controls
        addSearchControl();
        addAlarmControl();
        addZoomControl();

        m_searchControl.focus();

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
        options.setMaxZoom(15);
        m_map = new Map(divId, options);
    }

    private void addTileLayer() {
        VConsole.log("adding tile layer");
        final String attribution = "Map data &copy; <a tabindex=\"-1\" href=\"http://openstreetmap.org\">OpenStreetMap</a> contributors, <a tabindex=\"-1\" href=\"http://creativecommons.org/licenses/by-sa/2.0/\">CC-BY-SA</a>, Tiles &copy; <a tabindex=\"-1\" href=\"http://www.mapquest.com/\" target=\"_blank\">MapQuest</a> <img src=\"http://developer.mapquest.com/content/osm/mq_logo.png\" />";
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
        // markerClusterOptions.setProperty("disableClusteringAtZoom", 13);
        m_markerClusterGroup = new MarkerClusterGroup(markerClusterOptions);
        final NodeMarkerClusterCallback callback = new NodeMarkerClusterCallback();
        m_markerClusterGroup.on("clusterclick", callback);
        m_markerClusterGroup.on("clustertouchend", callback);
        m_map.addLayer(m_markerClusterGroup);
    }

    private void addSearchControl() {
        VConsole.log("adding search control");
        m_searchControl = new SearchControl(this, m_markers);
        m_map.addControl(m_searchControl);
    }

    private void addAlarmControl() {
        VConsole.log("adding alarm control");
        final AlarmControlOptions options = new AlarmControlOptions();
        options.setPosition("topleft");
        final AlarmControl alarmControl = new AlarmControl(this, options);
        m_map.addControl(alarmControl);
    }

    private void addZoomControl() {
        VConsole.log("adding zoom control");
        m_map.addControl(new Zoom(new Options()));
    }

    public boolean markerShouldBeVisible(final NodeMarker marker) {
        return m_filter.matches(marker);
    }

    public boolean isSearching() {
        return m_searchString != null && !"".equals(m_searchString);
    }

    public void refresh() {
        if (m_markers == null) {
            VConsole.log("markers not initialized yet, skipping update");
            return;
        }
        if (m_markerClusterGroup == null) {
            VConsole.log("marker cluster not initialized yet, skipping update");
            return;
        }

        m_markers.refresh();

        VConsole.log("processing " + m_markers.size() + " markers for the node layer");
        // make the search control refresh with the new markers
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override public void execute() {
                m_searchControl.refresh();
            }
        });

        // add new markers
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {
            final ListIterator<NodeMarker> m_markerIterator = m_markers.listIterator();

            @Override
            public boolean execute() {
                if (m_markerIterator.hasNext()) {
                    final NodeMarker marker = m_markerIterator.next();
                    if (!m_markerClusterGroup.hasLayer(marker)) {
                        m_markerClusterGroup.addLayer(marker);
                    }
                    return true;
                }

                VConsole.log("finished adding visible markers");

                return false;
            }

        });

        // remove disabled markers
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {
            final ListIterator<NodeMarker> m_markerIterator = m_markers.getDisabledMarkers().listIterator();

            @Override
            public boolean execute() {
                if (m_markerIterator.hasNext()) {
                    final NodeMarker marker = m_markerIterator.next();
                    marker.closePopup();
                    m_markerClusterGroup.removeLayer(marker);
                    return true;
                }

                VConsole.log("finished removing filtered markers");

                return false;
            }
        });

        // zoom on first run
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                if (m_firstUpdate) {
                    final LatLngBounds bounds = new LatLngBounds();
                    for (final NodeMarker marker : m_markers.getMarkers()) {
                        bounds.extend(marker.getLatLng());
                    }
                    for (final NodeMarker marker : m_markers.getDisabledMarkers()) {
                        bounds.extend(marker.getLatLng());
                    }
                    VConsole.log("first update, zooming to " + bounds.toBBoxString());
                    m_map.fitBounds(bounds);
                    m_firstUpdate = false;
                }

                VConsole.log("finished updating marker cluster layer");
            }
        });
    }

    public void updateMarkerClusterLayer() {
        if (m_markerClusterGroup == null) {
            VConsole.log("marker cluster not initialized yet, skipping update");
            return;
        }

        VConsole.log("clearing existing markers");
        m_markerClusterGroup.clearLayers();

        refresh();
    }

    public List<NodeMarker> getMarkers() {
        return m_markers.getMarkers();
    }

    public void setMarkers(final List<NodeMarker> markers) {
        m_markers.setMarkers(markers);
    }

    @Override
    public int getMinimumSeverity() {
        return m_minimumSeverity;
    }

    @Override
    public void setMinimumSeverity(final int minSeverity) {
        m_minimumSeverity = minSeverity;
    }

    @Override
    public String getSearchString() {
        return m_searchString;
    }

    @Override
    public void setSearchString(final String searchString) {
        m_searchString = searchString;
    }

    @Override
    public void clearSearch() {
        m_minimumSeverity = 0;
        m_searchString = "";
    }

    private final void destroyMap() {
        m_markerClusterGroup.clearLayers();
        m_map.removeLayer(m_markerClusterGroup);
        m_map.removeLayer(m_layer);
        m_map = null;
    }

    private enum MatchType {
        NONE, SUBSTRING, EXACT
    };
}

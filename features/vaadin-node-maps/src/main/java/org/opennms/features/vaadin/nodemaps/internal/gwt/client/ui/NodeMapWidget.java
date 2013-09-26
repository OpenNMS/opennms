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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.controls.zoom.Zoom;
import org.discotools.gwt.leaflet.client.crs.epsg.EPSG3857;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.layers.raster.TileLayer;
import org.discotools.gwt.leaflet.client.map.MapOptions;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.discotools.gwt.leaflet.client.types.LatLngBounds;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.Map;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchConsumer;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.IconCreateCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.NodeMarkerClusterCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm.AlarmControl;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm.AlarmControlOptions;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.SearchControl;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class NodeMapWidget extends Widget implements MarkerProvider, SearchConsumer {
    private final DivElement m_div;
    private Map m_map;
    private ILayer m_layer;
    private MarkerContainer m_markers;
    private MarkerClusterGroup m_markerClusterGroup;
    private MarkerClusterGroup[] m_stateClusterGroups;

    private boolean m_firstUpdate = true;
    private int m_minimumSeverity = 0;
    private String m_searchString = "";
    private SearchControl m_searchControl;
    private MarkerFilter m_filter;
    private NodeIdSelectionRpc m_rpc;

    private Logger logger = Logger.getLogger(getClass().getName());

    public NodeMapWidget() {
        m_div = Document.get().createDivElement();
        m_div.setId("gwt-map");
        m_div.getStyle().setWidth(100, Unit.PCT);
        m_div.getStyle().setHeight(100, Unit.PCT);
        setElement(m_div);

        setStyleName("v-openlayers");
        logger.log(Level.INFO, "div ID = " + getElement().getId());

        m_filter = new MarkerFilterImpl(this);
        m_markers = new MarkerContainer(m_filter);
        logger.log(Level.INFO, "NodeMapWidget initialized");
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
        logger.log(Level.INFO, "initializing map");

        createMap(divId);
        // createGoogleLayer();
        addTileLayer();
        addMarkerLayer();

        // overlay controls
        addSearchControl();
        addAlarmControl();
        addZoomControl();

        m_searchControl.focus();

        logger.log(Level.INFO, "finished initializing map");
    }

    @SuppressWarnings("unused")
    private void createGoogleLayer() {
        final EPSG3857 projection = new EPSG3857();
        final Options googleOptions = new Options();
        googleOptions.setProperty("crs", projection);

        logger.log(Level.INFO, "adding Google layer");
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
        logger.log(Level.INFO, "adding tile layer");
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
        logger.log(Level.INFO, "adding marker cluster layer");
        final Options markerClusterOptions = new Options();
        markerClusterOptions.setProperty("zoomToBoundsOnClick", false);
        markerClusterOptions.setProperty("iconCreateFunction", new IconCreateCallback());
        // markerClusterOptions.setProperty("disableClusteringAtZoom", 13);
        m_markerClusterGroup = new MarkerClusterGroup(markerClusterOptions);
        final NodeMarkerClusterCallback callback = new NodeMarkerClusterCallback();
        m_markerClusterGroup.on("clusterclick", callback);
        m_markerClusterGroup.on("clustertouchend", callback);
        m_map.addLayer(m_markerClusterGroup);
        m_stateClusterGroups = new MarkerClusterGroup[52];
        Options[] stateClusterOptions = new Options[m_stateClusterGroups.length];
        for(int i = 0; i < m_stateClusterGroups.length; i++){
            //stateClusterOptions[i] = new Options();
            stateClusterOptions[i] = markerClusterOptions;
            stateClusterOptions[i].setProperty("maxClusterRadius", 350);
            stateClusterOptions[i].setProperty("inUs", true);
            stateClusterOptions[i].setProperty("stateID", i);
            stateClusterOptions[i].setProperty("stateData", StatesData.getPolygonInfo(i, StatesData.getInstance()));
            //stateClusterOptions[i].setProperty("zoomToBoundsOnClick", false);
            //stateClusterOptions[i].setProperty("iconCreateFunction", new IconCreateCallback());
            m_stateClusterGroups[i] = new MarkerClusterGroup(stateClusterOptions[i]);
            m_stateClusterGroups[i].on("clusterclick", callback);
            m_stateClusterGroups[i].on("clustertouchend", callback);
            m_map.addLayer(m_stateClusterGroups[i]);
        }


    }



    private void addSearchControl() {
        logger.log(Level.INFO, "adding search control");
        m_searchControl = new SearchControl(this, m_markers);
        m_map.addControl(m_searchControl);
    }

    private void addAlarmControl() {
        logger.log(Level.INFO, "adding alarm control");
        final AlarmControlOptions options = new AlarmControlOptions();
        options.setPosition("topleft");
        final AlarmControl alarmControl = new AlarmControl(this, options);
        m_map.addControl(alarmControl);
    }

    private void addZoomControl() {
        logger.log(Level.INFO, "adding zoom control");
        m_map.addControl(new Zoom(new Options()));
    }

    public boolean markerShouldBeVisible(final JSNodeMarker marker) {
        return m_filter.matches(marker);
    }

    @Override
    public boolean isSearching() {
        return m_searchString != null && !"".equals(m_searchString);
    }

    @Override
    public void refresh() {
        if (m_markers == null) {
            logger.log(Level.INFO, "markers not initialized yet, skipping update");
            return;
        }
        if (m_markerClusterGroup == null) {
            logger.log(Level.INFO, "marker cluster not initialized yet, skipping update");
            return;
        }

        m_markers.refresh();

        final List<Integer> nodeIds = new ArrayList<Integer>();
        for (final JSNodeMarker marker : m_markers.getMarkers()) {
            final Integer nodeId = marker.getNodeId();
            if (nodeId != null) {
                nodeIds.add(nodeId);
            }
        }
        m_rpc.setSelectedNodes(nodeIds);

        logger.log(Level.INFO, "processing " + m_markers.size() + " markers for the node layer");
        // make the search control refresh with the new markers
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override public void execute() {
                m_searchControl.refresh();
            }
        });

        // add new markers
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {
            final ListIterator<JSNodeMarker> m_markerIterator = m_markers.listIterator();

            @Override
            public boolean execute() {
                if (m_markerIterator.hasNext()) {
                    final JSNodeMarker marker = m_markerIterator.next();
                    if(StatesData.inUs(marker.getLatLng().lat(), marker.getLatLng().lng(), StatesData.getUsShape())){
                        int stateId = StatesData.getStateId(marker.getLatLng().lat(), marker.getLatLng().lng(), StatesData.getInstance());
                        if(!m_stateClusterGroups[stateId].hasLayer(marker)){
                            m_stateClusterGroups[stateId].addLayer(marker);
                        }
                    }

                    else{
                        if (!m_markerClusterGroup.hasLayer(marker)) {
                            m_markerClusterGroup.addLayer(marker);
                        }
                    }
                    return true;
                }

                logger.log(Level.INFO, "finished adding visible markers");

                return false;
            }

        });

        // remove disabled markers
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {
            final ListIterator<JSNodeMarker> m_markerIterator = m_markers.getDisabledMarkers().listIterator();

            @Override
            public boolean execute() {
                if (m_markerIterator.hasNext()) {
                    final JSNodeMarker marker = m_markerIterator.next();
                    marker.closePopup();
                    if(StatesData.inUs(marker.getLatLng().lat(), marker.getLatLng().lng(), StatesData.getUsShape())){
                        int stateId = StatesData.getStateId(marker.getLatLng().lat(), marker.getLatLng().lng(), StatesData.getInstance());

                        m_stateClusterGroups[stateId].removeLayer(marker);

                    }

                    else{

                        m_markerClusterGroup.removeLayer(marker);

                    }
                    return true;
                }

                logger.log(Level.INFO, "finished removing filtered markers");

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
                        bounds.extend(JSNodeMarker.coordinatesToLatLng(marker.getCoordinates()));
                    }
                    for (final NodeMarker marker : m_markers.getDisabledMarkers()) {
                        bounds.extend(JSNodeMarker.coordinatesToLatLng(marker.getCoordinates()));
                    }
                    //logger.log(Level.INFO, "first update, zooming to " + bounds.toBBoxString());
                    m_map.fitBounds(bounds);
                    m_firstUpdate = false;
                }

                logger.log(Level.INFO, "finished updating marker cluster layer");
            }
        });
    }

    public void updateMarkerClusterLayer() {
        if (m_markerClusterGroup == null) {
            logger.log(Level.INFO, "marker cluster not initialized yet, skipping update");
            return;
        }

        logger.log(Level.INFO, "clearing existing markers");
        m_markerClusterGroup.clearLayers();
        for(int i = 0; i < m_stateClusterGroups.length; i++){
            m_stateClusterGroups[i].clearLayers();
        }

        refresh();
    }

    @Override
    public List<JSNodeMarker> getMarkers() {
        return m_markers.getMarkers();
    }

    public void setMarkers(final List<JSNodeMarker> markers) {
        m_markers.setMarkers(markers);

        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                updateMarkerClusterLayer();
            }
        });
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
        if (m_searchControl != null) {
            m_searchControl.replaceSearchWith(searchString);
        }
    }

    @Override
    public void clearSearch() {
        m_minimumSeverity = 0;
        m_searchString = "";
    }

    private final void destroyMap() {
        if (m_markerClusterGroup != null) {
            m_markerClusterGroup.clearLayers();
            for(int i = 0; i < m_stateClusterGroups.length; i++){
                m_stateClusterGroups[i].clearLayers();
            }
        }
        if (m_map != null) {
            m_map.removeLayer(m_markerClusterGroup);
            m_map.removeLayer(m_layer);
            m_map = null;
        }
    }

    public void setRpc(final NodeIdSelectionRpc rpc) {
        m_rpc = rpc;
    }
}

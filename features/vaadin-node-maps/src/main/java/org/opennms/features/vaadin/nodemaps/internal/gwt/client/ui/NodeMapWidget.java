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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.controls.zoom.Zoom;
import org.discotools.gwt.leaflet.client.controls.zoom.ZoomOptions;
import org.discotools.gwt.leaflet.client.crs.epsg.EPSG3857;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.layers.raster.TileLayer;
import org.discotools.gwt.leaflet.client.map.MapOptions;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.discotools.gwt.leaflet.client.types.LatLngBounds;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.Map;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.OpenNMSEventManager;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.ComponentInitializedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilteredMarkersUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilteredMarkersUpdatedEventHandler;
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
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.Widget;

@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class NodeMapWidget extends Widget implements MarkerProvider, FilteredMarkersUpdatedEventHandler {
    private final DivElement m_div;
    private Map m_map;
    private ILayer m_layer;
    private MarkerContainer m_markerContainer;
    private MarkerClusterGroup m_markerClusterGroup;
    private MarkerClusterGroup[] m_stateClusterGroups;

    private boolean m_firstUpdate = true;
    private SearchControl m_searchControl;
    private MarkerFilterImpl m_filter;
    private NodeIdSelectionRpc m_clientToServerRpc;

    private OpenNMSEventManager m_eventManager;

    private Logger logger = Logger.getLogger(getClass().getName());

    public NodeMapWidget() {
        m_eventManager = new OpenNMSEventManager();
        m_eventManager.addHandler(FilteredMarkersUpdatedEvent.TYPE, this);

        m_div = Document.get().createDivElement();
        m_div.setId("gwt-map");
        m_div.getStyle().setWidth(100, Unit.PCT);
        m_div.getStyle().setHeight(100, Unit.PCT);
        setElement(m_div);

        setStyleName("v-openlayers");
        logger.log(Level.INFO, "NodeMapWidget(): div ID = " + getElement().getId());

        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(final AttachEvent event) {
                if (event.isAttached()) {
                    logger.log(Level.INFO, "NodeMapWidget.onAttach()");

                    m_filter = new MarkerFilterImpl("", 0, m_eventManager);
                    m_markerContainer = new MarkerContainer(m_filter, m_eventManager);

                    m_filter.onLoad();
                    m_markerContainer.onLoad();

                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override public void execute() {
                            initializeMap(m_div.getId());
                        }
                    });
                } else {
                    logger.log(Level.INFO, "NodeMapwidget.onDetach()");
                    if (m_markerContainer != null) m_markerContainer.onUnload();
                    if (m_filter != null) m_filter.onUnload();
                    destroyMap();
                }
            }
        });
        logger.log(Level.INFO, "NodeMapWidget(): initialized");
    }

    private void initializeMap(final String divId) {
        logger.log(Level.INFO, "NodeMapWidget.initializeMap()");

        createMap(divId);
        // createGoogleLayer();
        addTileLayer();
        addMarkerLayer();

        // overlay controls
        addSearchControl();
        addAlarmControl();
        addZoomControl();

        m_searchControl.focusInputBox();

        m_eventManager.fireEvent(new ComponentInitializedEvent(NodeMapConnector.class.getName()));
        logger.log(Level.INFO, "NodeMapWidget.initializeMap(): finished");
    }

    @SuppressWarnings("unused")
    private void createGoogleLayer() {
        final EPSG3857 projection = new EPSG3857();
        final Options googleOptions = new Options();
        googleOptions.setProperty("crs", projection);

        logger.log(Level.INFO, "NodeMapWidget.createGoogleLayer(): adding Google layer");
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
        logger.log(Level.INFO, "NodeMapWidget.addTileLayer()");
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
        logger.log(Level.INFO, "NodeMapWidget.addMarkerLayer()");

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

        final Options[] stateClusterOptions = new Options[m_stateClusterGroups.length];
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
        logger.log(Level.INFO, "NodeMapWidget.addSearchControl()");
        m_searchControl = new SearchControl(m_markerContainer, this, m_eventManager);
        m_map.addControl(m_searchControl);
    }

    private void addAlarmControl() {
        logger.log(Level.INFO, "NodeMapWidget.addAlarmControl()");
        final AlarmControlOptions options = new AlarmControlOptions();
        options.setPosition("topleft");
        final AlarmControl alarmControl = new AlarmControl(m_eventManager);
        m_map.addControl(alarmControl);
    }

    private void addZoomControl() {
        logger.log(Level.INFO, "NodeMapWidget.addZoomControl()");
        final ZoomOptions options = new ZoomOptions();
        options.setPosition("topright");
        m_map.addControl(new Zoom(options));
    }

    public boolean markerShouldBeVisible(final JSNodeMarker marker) {
        return m_filter.matches(marker);
    }

    public void updateMarkerClusterLayer() {
        if (m_markerContainer == null || m_markerClusterGroup == null) {
            logger.log(Level.INFO, "NodeMapWidget.updateMarkerClusterLayout(): markers or marker clusters not initialized yet, deferring refresh");
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                @Override public boolean execute() {
                    updateMarkerClusterLayer();
                    return false;
                }
            }, 1000);
            return;
        }

        clearExistingMarkers();
        addNewMarkers();
        removeDisabledMarkers();
        zoomToFit();
        sendSelectionToBackend();
    }

    private void clearExistingMarkers() {
        logger.log(Level.INFO, "NodeMapWidget.clearExistingMarkers()");
        m_markerClusterGroup.clearLayers();
        for(int i = 0; i < m_stateClusterGroups.length; i++){
            m_stateClusterGroups[i].clearLayers();
        }
    }

    private void addNewMarkers() {
        // add new markers
        logger.log(Level.INFO, "NodeMapWidget.addNewMarkers(): adding " + m_markerContainer.size() + " markers to the map.");
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {
            final ListIterator<JSNodeMarker> m_markerIterator = m_markerContainer.listIterator();

            @Override public boolean execute() {
                if (m_markerIterator.hasNext()) {
                    final JSNodeMarker marker = m_markerIterator.next();
                    final Coordinates coordinates = marker.getCoordinates();
                    if (coordinates == null) {
                        logger.log(Level.WARNING, "NodeMapWidget.addNewMarkers(): no coordinates found for marker! " + marker);
                        return true;
                    }
                    if (StatesData.inUs(coordinates.getLatitudeAsDouble(), coordinates.getLongitudeAsDouble(), StatesData.getUsShape())) {
                        final int stateId = StatesData.getStateId(marker.getLatLng().lat(), marker.getLatLng().lng(), StatesData.getInstance());
                        if (!m_stateClusterGroups[stateId].hasLayer(marker)) {
                            m_stateClusterGroups[stateId].addLayer(marker);
                        }
                    } else {
                        if (!m_markerClusterGroup.hasLayer(marker)) {
                            m_markerClusterGroup.addLayer(marker);
                        }
                    }
                    return true;
                }

                logger.log(Level.INFO, "NodeMapWidget.addNewMarkers(): finished adding visible markers (" + m_markerContainer.size() + " entries)");
                return false;
            }

        });
    }

    private void removeDisabledMarkers() {
        // remove disabled markers
        final List<JSNodeMarker> disabledMarkers = m_markerContainer.getDisabledMarkers();
        logger.log(Level.INFO, "NodeMapWidget.removeDisabledMarkers(): removing " + disabledMarkers.size() + " disabled markers from the map.");
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {
            final ListIterator<JSNodeMarker> m_markerIterator = disabledMarkers.listIterator();

            @Override public boolean execute() {
                if (m_markerIterator.hasNext()) {
                    final JSNodeMarker marker = m_markerIterator.next();
                    marker.closePopup();
                    final Coordinates coordinates = marker.getCoordinates();
                    if (coordinates == null) {
                        logger.log(Level.WARNING, "NodeMapWidget.removeDisabledMarkers(): no coordinates found for marker! " + marker);
                        return true;
                    }
                    if (StatesData.inUs(marker.getLatLng().lat(), marker.getLatLng().lng(), StatesData.getUsShape())){
                        final int stateId = StatesData.getStateId(marker.getLatLng().lat(), marker.getLatLng().lng(), StatesData.getInstance());
                        m_stateClusterGroups[stateId].removeLayer(marker);
                    } else {
                        m_markerClusterGroup.removeLayer(marker);
                    }
                    return true;
                }

                logger.log(Level.INFO, "NodeMapWidget.removeDisabledMarkers(): finished removing filtered markers (" + disabledMarkers.size() + " entries)");
                return false;
            }
        });
    }

    private void zoomToFit() {
        // zoom on first run
        if (m_firstUpdate) {
            logger.log(Level.INFO, "NodeMapWidget.zoomToFit(): first update, zooming to bounds.");
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override public void execute() {
                    if (m_firstUpdate) {
                        final List<JSNodeMarker> allMarkers = m_markerContainer.getAllMarkers();

                        if (allMarkers.size() == 0) {
                            logger.log(Level.INFO, "NodeMapWidget.zoomToFit(): no bounds yet, skipping.");
                        } else {
                            final LatLngBounds bounds = new LatLngBounds();
                            for (final NodeMarker marker : allMarkers) {
                                logger.log(Level.INFO, "NodeMapWidget.zoomToFit(): processing marker: " + marker);
                                final Coordinates coordinates = marker.getCoordinates();
                                if (coordinates == null) {
                                    logger.log(Level.WARNING, "NodeMapWidget.zoomToFit(): no coordinates found for marker! " + marker);
                                } else {
                                    bounds.extend(JSNodeMarker.coordinatesToLatLng(coordinates));
                                }
                            }
                            logger.log(Level.INFO, "NodeMapWidget.zoomToFit(): setting boundary to " + bounds.toBBoxString() + ".");
                            m_map.fitBounds(bounds);
                            m_firstUpdate = false;
                        }
                    }
                }
            });
        }
    }

    private void sendSelectionToBackend() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override public void execute() {
                final List<Integer> nodeIds = new ArrayList<Integer>();
                for (final JSNodeMarker marker : m_markerContainer.getMarkers()) {
                    final Integer nodeId = marker.getNodeId();
                    if (nodeId != null) {
                        nodeIds.add(nodeId);
                    }
                }
                m_clientToServerRpc.setSelectedNodes(nodeIds);
                logger.log(Level.INFO, "NodeMapWidget.sendSelectionToBackend(): sent " + nodeIds.size() + " nodes to backend.");
            }
        });
    }

    @Override
    public List<JSNodeMarker> getMarkers() {
        return m_markerContainer.getMarkers();
    }

    public void setMarkers(final List<JSNodeMarker> markers) {
        if (markers != null) {
            m_markerContainer.setMarkers(markers);
        } else {
            m_markerContainer.setMarkers(new ArrayList<JSNodeMarker>());
        }
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
        m_clientToServerRpc = rpc;
    }

    @Override public void onFilteredMarkersUpdatedEvent(final FilteredMarkersUpdatedEvent event) {
        logger.log(Level.INFO, "NodeMapWidget.onFilteredMarkersUpdated(), refreshing node map widgets");
        updateMarkerClusterLayer();
    }

    public OpenNMSEventManager getEventManager() {
        return m_eventManager;
    }
}

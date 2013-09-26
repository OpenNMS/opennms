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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.discotools.gwt.leaflet.client.types.Icon;
import org.discotools.gwt.leaflet.client.types.IconOptions;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.discotools.gwt.leaflet.client.types.Point;
import org.opennms.features.vaadin.nodemaps.internal.NodeMapComponent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MapNode;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMapState;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.NodeMarkerClusterCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(NodeMapComponent.class)
public class NodeMapConnector extends AbstractComponentConnector {
    private Logger LOG = Logger.getLogger(getClass().getName());

    private static final long serialVersionUID = 3902997653175283284L;
    private Map<String, Icon> m_icons;

    private NodeIdSelectionRpc m_rpc = RpcProxy.create(NodeIdSelectionRpc.class, this);

    public NodeMapConnector() {
        initializeIcons();
    }

    private static native final boolean isRetina() /*-{
        return $wnd.L.Browser.retina;
    }-*/;

    @Override
    public void onStateChanged(final StateChangeEvent stateChangeEvent) {
        LOG.info("onStateChanged(" + stateChangeEvent + ")");
        // Handle all common Vaadin features first
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("initialSearch")) {
            getWidget().setSearchString(getState().initialSearch);
        }

        if (stateChangeEvent.hasPropertyChanged("nodes")) {
            updateNodes();
        }

        if (stateChangeEvent.hasPropertyChanged("nodeIds")) {
            final List<Integer> nodeIds = getState().nodeIds;
            LOG.info("nodeIds has changed to: " + getState().nodeIds);
            if (nodeIds != null && nodeIds.size() > 0) {
                final StringBuilder sb = new StringBuilder("nodeId in ");
                final Iterator<Integer> i = nodeIds.iterator();
                while (i.hasNext()) {
                    sb.append(i.next());
                    if (i.hasNext()) sb.append(", ");
                }
                getWidget().setSearchString(sb.toString());
            }
        }
    }

    private void updateNodes() {
        final List<JSNodeMarker> featureCollection = new ArrayList<JSNodeMarker>();

        if (getState().nodes.isEmpty()) {
            getWidget().setMarkers(featureCollection);
            return;
        }

        for (final MapNode node : getState().nodes) {
            final JSNodeMarker marker = new JSNodeMarker(new LatLng(node.getLatitude(), node.getLongitude()));
            marker.putProperty(JSNodeMarker.Property.NODE_ID, node.getNodeId());
            marker.putProperty(JSNodeMarker.Property.NODE_LABEL, node.getNodeLabel());
            marker.putProperty(JSNodeMarker.Property.FOREIGN_SOURCE, node.getForeignSource());
            marker.putProperty(JSNodeMarker.Property.FOREIGN_ID, node.getForeignId());
            marker.putProperty(JSNodeMarker.Property.DESCRIPTION, node.getDescription());
            marker.putProperty(JSNodeMarker.Property.MAINTCONTRACT, node.getMaintcontract());
            marker.putProperty(JSNodeMarker.Property.IP_ADDRESS, node.getIpAddress());
            marker.putProperty(JSNodeMarker.Property.SEVERITY, node.getSeverity());
            marker.putProperty(JSNodeMarker.Property.SEVERITY_LABEL, node.getSeverityLabel());

            final List<String> categories = node.getCategories();
            if (categories != null) {
                marker.setCategories(categories.toArray(new String[]{}));
            }

            // TODO: Icon handling should be moved to the Widget
            if (m_icons.containsKey(marker.getSeverityLabel())) {
                marker.setIcon(m_icons.get(marker.getSeverityLabel()));
            } else {
                marker.setIcon(m_icons.get("Normal"));
            }

            // TODO: This should be moved to the Widget
            marker.bindPopup(NodeMarkerClusterCallback.getPopupTextForMarker(marker));

            featureCollection.add(marker);
        }
        getWidget().setMarkers(featureCollection);
    }

    @Override
    public NodeMapState getState() {
        return (NodeMapState) super.getState();
    }

    @Override
    protected Widget createWidget() {
        final NodeMapWidget widget = GWT.create(NodeMapWidget.class);
        widget.setRpc(m_rpc);
        return widget;
    }

    @Override
    public NodeMapWidget getWidget() {
        return (NodeMapWidget) super.getWidget();
    }

    private void initializeIcons() {
        if (m_icons == null) {
            m_icons = new HashMap<String, Icon>();
            for (final String severity : new String[]{"Normal", "Warning", "Minor", "Major", "Critical"}) {
                IconOptions options = new IconOptions();
                options.setIconSize(new Point(25, 41));
                options.setIconAnchor(new Point(12, 41));
                options.setPopupAnchor(new Point(1, -34));
                options.setShadowUrl(new Point(41, 41));
                String basePath = GWT.getModuleBaseForStaticFiles() + "images/";
                if (isRetina()) {
                    options.setIconUrl(basePath + severity + "@2x.png");
                } else {
                    options.setIconUrl(basePath + severity + ".png");
                }
                Icon icon = new Icon(options);

                m_icons.put(severity, icon);
            }
        }
    }
}

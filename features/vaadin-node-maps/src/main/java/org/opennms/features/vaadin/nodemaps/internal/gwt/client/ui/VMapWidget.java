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

import org.discotools.gwt.leaflet.client.types.Icon;
import org.discotools.gwt.leaflet.client.types.IconOptions;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.discotools.gwt.leaflet.client.types.Point;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.NodeMarkerClusterCallback;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

public class VMapWidget extends GWTMapWidget implements Paintable {

    private ApplicationConnection m_client;
    private String m_uidlId;
    private boolean m_firstRun = true;
    private Map<String,Icon> m_icons;

    public VMapWidget() {
        super();
        setStyleName("v-openlayers");
        VConsole.log("div ID = " + getElement().getId());
    }

    private static native final boolean isRetina() /*-{
        return $wnd.L.Browser.retina;
    }-*/;

    @Override
    public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) return;
        m_client = client;
        m_uidlId = uidl.getId();

        initializeIcons();

        if (uidl.hasAttribute("initialSearchString")) {
            setSearchString(uidl.getStringAttribute("initialSearchString"));
        }

        final UIDL nodeUIDL = uidl.getChildByTagName("nodes");

        final List<NodeMarker> featureCollection = new ArrayList<NodeMarker>();

        for (final Iterator<?> iterator = nodeUIDL.getChildIterator(); iterator.hasNext();) {
            final UIDL node = (UIDL) iterator.next();

            final double latitude = Float.valueOf(node.getFloatAttribute("latitude")).doubleValue();
            final double longitude = Float.valueOf(node.getFloatAttribute("longitude")).doubleValue();

            final NodeMarker marker = new NodeMarker(new LatLng(latitude, longitude));

            for (final String key : new String[] { "nodeId", "nodeLabel", "foreignSource", "foreignId", "description", "maintcontract", "ipAddress", "severity", "severityLabel", "unackedCount" }) {
                if (node.hasAttribute(key)) marker.putProperty(key, node.getStringAttribute(key));
            }

            if (node.hasAttribute("categories")) {
                marker.setCategories(node.getStringArrayAttribute("categories"));
            }

            if (m_icons.containsKey(marker.getSeverityLabel())) {
                marker.setIcon(m_icons.get(marker.getSeverityLabel()));
            } else {
                marker.setIcon(m_icons.get("Normal"));
            }
            marker.bindPopup(NodeMarkerClusterCallback.getPopupTextForMarker(marker));
            featureCollection.add(marker);
        }

        setMarkers(featureCollection);
        Scheduler.get().scheduleDeferred(new Command() {
            @Override public void execute() {
                updateMarkerClusterLayer();
            }
        });
    }

    private void initializeIcons() {
        if (m_icons == null) {
            m_icons = new HashMap<String,Icon>();
            final String basepath = m_client.getAppUri();
            for (final String severity : new String[] { "Normal", "Warning", "Minor", "Major", "Critical" }) {
                IconOptions options = new IconOptions();
                options.setIconSize(new Point(25,41));
                options.setIconAnchor(new Point(12,41));
                options.setPopupAnchor(new Point(1,-34));
                options.setShadowUrl(new Point(41,41));
                if (isRetina()) {
                    options.setIconUrl(basepath + "../VAADIN/widgetsets/org.opennms.features.vaadin.nodemaps.internal.gwt.NodeMapsWidgetset/images/" + severity + "@2x.png");
                } else {
                    options.setIconUrl(basepath + "../VAADIN/widgetsets/org.opennms.features.vaadin.nodemaps.internal.gwt.NodeMapsWidgetset/images/" + severity + ".png");
                }
                Icon icon = new Icon(options);

                m_icons.put(severity, icon);
            }
        }
    }
}

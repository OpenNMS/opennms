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
import java.util.Iterator;
import java.util.List;

import org.discotools.gwt.leaflet.client.types.LatLng;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.leaflet.NodeMarker;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

public class VMapWidget extends GWTMapWidget implements Paintable {

    @SuppressWarnings("unused")
    private ApplicationConnection m_client;
    @SuppressWarnings("unused")
    private String m_uidlId;

    public VMapWidget() {
        super();
        setStyleName("v-openlayers");
        VConsole.log("div ID = " + getElement().getId());
    }

    @Override
    public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) return;
        m_client = client;
        m_uidlId = uidl.getId();

        final UIDL nodeUIDL = uidl.getChildByTagName("nodes");

        final List<NodeMarker> featureCollection = new ArrayList<NodeMarker>();

        for (final Iterator<?> iterator = nodeUIDL.getChildIterator(); iterator.hasNext();) {
            final UIDL node = (UIDL) iterator.next();

            final double latitude = Float.valueOf(node.getFloatAttribute("latitude")).doubleValue();
            final double longitude = Float.valueOf(node.getFloatAttribute("longitude")).doubleValue();

            final NodeMarker feature = new NodeMarker(new LatLng(latitude, longitude));

            for (final String key : new String[] { "nodeId", "nodeLabel", "foreignSource", "foreignId", "ipAddress", "severity", "severityLabel", "unackedCount" }) {
                if (node.hasAttribute(key)) feature.putProperty(key, node.getStringAttribute(key));
            }

            featureCollection.add(feature);
        }

        setFeatureCollection(featureCollection);
        Scheduler.get().scheduleDeferred(new Command() {
            @Override public void execute() {
                updateFeatureLayer();
            }
        });
    }
}

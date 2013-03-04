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

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.marker.Marker;
import org.discotools.gwt.leaflet.client.types.LatLng;

public class NodeMarker extends Marker {
    private String[] m_textProperties = new String[] { "nodeLabel", "foreignSource", "foreignId", "ipAddress" };

    public NodeMarker(final LatLng latLng) {
        super(latLng, new Options());
    }

    public NodeMarker(final JSObject element) {
        super(element);
    }

    public void putProperty(final String key, final String value) {
        getJSObject().setProperty(key, value);
    }

    public String getProperty(final String key) {
        return getJSObject().getPropertyAsString(key);
    }

    public Integer getNodeId() {
        final String id = getProperty("nodeId");
        return id == null? null : Integer.valueOf(id);
    }

    public String getNodeLabel() {
        return getProperty("nodeLabel");
    }

    public String getForeignSource() {
        return getProperty("foreignSource");
    }

    public String getForeignId() {
        return getProperty("foreignId");
    }
    
    public String getIpAddress() {
        return getProperty("ipAddress");
    }

    public String getSeverityLabel() {
        return getProperty("severityLabel");
    }

    public int getSeverity() {
        final String severity = getProperty("severity");
        return severity == null? 0 : Integer.valueOf(severity);
    }

    public Integer getUnackedCount() {
        final String count = getProperty("unackedCount");
        return count == null? 0 : Integer.valueOf(count);
    }

    public boolean containsText(final String text) {
        if (text == null) return false;
        if ("".equals(text)) return true;

        for (final String propertyName : m_textProperties) {
            final String value = getProperty(propertyName);
            if (value != null) {
                final String searchString = text.toLowerCase();
                final String property = value.toLowerCase();
                if (property.contains(searchString)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public final String toString() {
        return "Feature[lat=" + getLatLng().lat() + ",lon=" + getLatLng().lng() + ",label=" + getNodeLabel() + "]";
    }

    public JSObject toSearchResult() {
        return SearchResult.create(getNodeLabel(), getLatLng());
    }

}

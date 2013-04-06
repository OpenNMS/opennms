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

import com.google.gwt.core.client.JsArrayString;

public class NodeMarker extends Marker {
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

    public String[] getTextPropertyNames() {
        final JsArrayString nativeNames = getNativePropertyNames(getJSObject());
        final String[] names = new String[nativeNames.length()];
        for (int i = 0; i < nativeNames.length(); i++) {
            names[i] = nativeNames.get(i);
        }
        return names;
    }

    private native JsArrayString getNativePropertyNames(final JSObject self) /*-{
        var props = [];
        for (var prop in self) {
            if (self.hasOwnProperty(prop) && typeof self[prop] === 'string') {
                props.push(prop);
            }
        }
        return props;
    }-*/;

    public JsArrayString getCategories() {
        final JSObject property = getJSObject().getProperty("categories");
        if (property == null) {
            return JsArrayString.createArray().cast();
        } else {
            return property.cast();
        }
    }

    public String getCategoriesAsString() {
        final StringBuilder catBuilder = new StringBuilder();
        final JsArrayString categories = getCategories();
        if (categories.length() > 0) {
            if (categories.length() == 1) {
                catBuilder.append("Category: ");
            } else {
                catBuilder.append("Categories: ");
            }
            for (int i = 0; i < categories.length(); i++) {
                catBuilder.append(categories.get(i));
                if (i != (categories.length() - 1)) {
                    catBuilder.append(", ");
                }
            }
        }
        return catBuilder.toString();
    }

    public void setCategories(final String[] categories) {
        final JsArrayString array = JsArrayString.createArray().cast();
        for (final String category : categories) {
            array.push(category);
        }
        final JSObject jsObject = array.cast();
        getJSObject().setProperty("categories", jsObject);
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

    public String getDescription() {
        return getProperty("description");
    }

    public String getMaintContract() {
        return getProperty("maintcontract");
    }

    public int getSeverity() {
        final String severity = getProperty("severity");
        return severity == null? 0 : Integer.valueOf(severity);
    }

    public Integer getUnackedCount() {
        final String count = getProperty("unackedCount");
        return count == null? 0 : Integer.valueOf(count);
    }

    @Override
    public final String toString() {
        return "Feature[lat=" + getLatLng().lat() + ",lon=" + getLatLng().lng() + ",label=" + getNodeLabel() + "]";
    }

    public JSObject toSearchResult() {
        return SearchResult.create(getNodeLabel(), getLatLng());
    }

}

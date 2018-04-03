/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.marker.Marker;
import org.discotools.gwt.leaflet.client.types.LatLng;

import com.google.gwt.core.client.JsArrayString;

public class JSNodeMarker extends Marker implements NodeMarker {

    public static class Property {
        public static final String CATEGORIES = "categories";
        public static final String NODE_ID = "nodeId";
        public static final String NODE_LABEL = "nodeLabel";
        public static final String FOREIGN_SOURCE = "foreignSource";
        public static final String FOREIGN_ID = "foreignId";
        public static final String IP_ADDRESS = "ipAddress";
        public static final String SEVERITY_LABEL = "severityLabel";
        public static final String DESCRIPTION = "description";
        public static final String MAINTCONTRACT = "maintcontract";
        public static final String SEVERITY = "severity";
        public static final String UNACKED_COUNT = "unackedCount";
    }

    public JSNodeMarker(final LatLng latLng) {
        super(latLng, new Options());
    }

    public JSNodeMarker(final JSObject element) {
        super(element);
    }

    public void putProperty(final String key, final String value) {
        getJSObject().setProperty(key, value);
    }

    public String getProperty(final String key) {
        return getJSObject().getPropertyAsString(key);
    }

    public List<String> getTextPropertyNames() {
        final JsArrayString nativeNames = getNativePropertyNames(getJSObject());
        final List<String> names = new ArrayList<>();
        for (int i = 0; i < nativeNames.length(); i++) {
            names.add(nativeNames.get(i));
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

    public List<String> getCategoryList() {
        final List<String> categories = new ArrayList<>();
        final JsArrayString jsCategories = getCategories();
        for (int i = 0; i < jsCategories.length(); i++) {
            categories.add(jsCategories.get(i));
        }
        return categories;
    }

    public JsArrayString getCategories() {
        final JSObject property = getJSObject().getProperty(Property.CATEGORIES);
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
        getJSObject().setProperty(Property.CATEGORIES, jsObject);
    }

    public Integer getNodeId() {
        final String id = getProperty(Property.NODE_ID);
        return id == null? null : Integer.valueOf(id);
    }

    public String getNodeLabel() {
        return getProperty(Property.NODE_LABEL);
    }

    public String getForeignSource() {
        return getProperty(Property.FOREIGN_SOURCE);
    }

    public String getForeignId() {
        return getProperty(Property.FOREIGN_ID);
    }

    public String getIpAddress() {
        return getProperty(Property.IP_ADDRESS);
    }

    public String getSeverityLabel() {
        return getProperty(Property.SEVERITY_LABEL);
    }

    public String getDescription() {
        return getProperty(Property.DESCRIPTION);
    }

    public String getMaintContract() {
        return getProperty(Property.MAINTCONTRACT);
    }

    public Coordinates getCoordinates() {
        final LatLng latLng = this.getLatLng();
        if (latLng == null || Double.isNaN(latLng.lat()) || Double.isNaN(latLng.lng())) {
            return null;
        }
        return new Coordinates(Double.valueOf(latLng.lng()).floatValue(), Double.valueOf(latLng.lat()).floatValue());
    }

    public Integer getSeverity() {
        final String severity = getProperty(Property.SEVERITY);
        return severity == null? 0 : Integer.valueOf(severity);
    }

    public Integer getUnackedCount() {
        final String count = getProperty(Property.UNACKED_COUNT);
        return count == null? 0 : Integer.valueOf(count);
    }

    public Map<String,String> getProperties() {
        final Map<String,String> props = new HashMap<String,String>();
        for (final String k : getTextPropertyNames()) {
            final String value = getProperty(k);
            if (k == null || value == null) continue;
            final String key = k.toLowerCase();
            props.put(key, value);
        }

        final String cats = getCategoriesAsString();
        if (cats != null && cats.length() > 0) {
            props.put("categories", cats);
        }

        final Integer nodeId = getNodeId();
        if (nodeId != null) {
            props.put("nodeid", nodeId.toString());
        }

        final Integer severity = getSeverity();
        if (severity != null) {
            props.put("severity", severity.toString());
        }

        final Integer unackedCount = getUnackedCount();
        if (unackedCount != null) {
            props.put("unackedcount", unackedCount.toString());
        }

        final Coordinates coordinates = getCoordinates();
        if (coordinates != null) {
            props.put("coordinates", coordinates.toString());
        }

        return props;
    }

    @Override
    public final String toString() {
        return "Feature[lat=" + getLatLng().lat() + ",lon=" + getLatLng().lng() + ",label=" + getNodeLabel() + "]";
    }

    public JSObject toSearchResult() {
        return SearchResult.create(getNodeLabel(), getLatLng());
    }

    public static LatLng coordinatesToLatLng(final Coordinates coordinates) {
        return new LatLng(coordinates.latitude, coordinates.longitude);
    }

}

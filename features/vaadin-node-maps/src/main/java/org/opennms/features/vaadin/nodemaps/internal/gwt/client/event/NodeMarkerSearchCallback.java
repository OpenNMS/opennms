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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.jsobject.JSObjectWrapper;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchResults;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.MarkerProvider;

public abstract class NodeMarkerSearchCallback extends JSObjectWrapper {
    Logger logger = Logger.getLogger(getClass().getName());

    private MarkerProvider m_markerProvider;

    protected NodeMarkerSearchCallback(final JSObject jsObject) {
        super(jsObject);
    }

    public NodeMarkerSearchCallback(final MarkerProvider provider) {
        super(JSObject.createJSFunction());
        setJSObject(getCallbackFunction());
        m_markerProvider = provider;
    }

    public abstract Collection<JSNodeMarker> search(final Collection<JSNodeMarker> markers, final String text);

    protected JSObject doSearch(final String text) {
        logger.log(Level.INFO, "doSearch(" + text +")");
        final Collection<JSNodeMarker> markers = search(m_markerProvider.getMarkers(), text);
        logger.log(Level.INFO, markers.size() + " markers returned.");
        final SearchResults results = SearchResults.create();
        for (final JSNodeMarker marker : markers) {
            final LatLng latLng = JSNodeMarker.coordinatesToLatLng(marker.getCoordinates());
            results.setProperty(marker.getNodeLabel(), latLng.getJSObject());
        }
        return results;
    }

    private final native JSObject getCallbackFunction() /*-{
        var self = this;
        return function(text) {
            return self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.NodeMarkerSearchCallback::doSearch(Ljava/lang/String;)(text);
        };
    }-*/;

}

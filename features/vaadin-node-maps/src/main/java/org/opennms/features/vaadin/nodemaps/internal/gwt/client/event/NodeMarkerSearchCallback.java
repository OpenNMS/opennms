package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.jsobject.JSObjectWrapper;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MarkerProvider;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchResults;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


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

    public abstract Collection<NodeMarker> search(final Collection<NodeMarker> markers, final String text);

    protected JSObject doSearch(final String text) {
        logger.log(Level.INFO, "doSearch(" + text +")");
        final Collection<NodeMarker> markers = search(m_markerProvider.getMarkers(), text);
        logger.log(Level.INFO, markers.size() + " markers returned.");
        final SearchResults results = SearchResults.create();
        for (final NodeMarker marker : markers) {
            results.setProperty(marker.getNodeLabel(), marker.getLatLng().getJSObject());
        }
        return results;
    }

    private native final JSObject getCallbackFunction() /*-{
        var self = this;
        return function(text) {
            return self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.NodeMarkerSearchCallback::doSearch(Ljava/lang/String;)(text);
        };
    }-*/;

}

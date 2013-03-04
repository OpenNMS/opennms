package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.Collection;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.jsobject.JSObjectWrapper;

import com.vaadin.terminal.gwt.client.VConsole;

public abstract class NodeMarkerSearchCallback extends JSObjectWrapper {
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
        VConsole.log("doSearch(" + text +")");
        final Collection<NodeMarker> markers = search(m_markerProvider.getMarkers(), text);
        VConsole.log(markers.size() + " markers returned.");
        final SearchResults results = SearchResults.create();
        for (final NodeMarker marker : markers) {
            results.setProperty(marker.getNodeLabel(), marker.getLatLng().getJSObject());
        }
        return results;
    }

    private native final JSObject getCallbackFunction() /*-{
        var self = this;
        return function(text) {
            return self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarkerSearchCallback::doSearch(Ljava/lang/String;)(text);
        };
    }-*/;

}

package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.controls.Control;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.vaadin.terminal.gwt.client.VConsole;

public class SearchControl extends Control {
    private MarkerProvider m_markerProvider;
    private SearchOptions m_options;

    protected SearchControl(final JSObject element) {
        super(element);
    }

    public SearchControl(final MarkerProvider provider) {
        this(provider, new SearchOptions());
    }

    public SearchControl(final MarkerProvider provider, final SearchOptions options) {
        this(SearchControlImpl.create(options.getJSObject()));
        VConsole.log("new SearchControl()");
        m_options = options;
        m_markerProvider = provider;
    }

    protected Element onAdd(final JavaScriptObject map) {
        VConsole.log("onAdd() called");
        return super.onAdd(map);
    }

    protected SearchControl onRemove(final JavaScriptObject map) {
        VConsole.log("onRemove() called");
        return (SearchControl)super.onRemove(map);
    }
}

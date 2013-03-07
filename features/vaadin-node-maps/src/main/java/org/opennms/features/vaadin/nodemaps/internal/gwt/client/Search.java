package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.controls.Control;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;

public class Search extends Control {
    public Search(final JSObject element) {
        super(element);
    }

    public Search() {
        this(SearchImpl.create(JSObject.createJSObject()));
    }

    public Search(final SearchOptions options) {
        this(SearchImpl.create(options.getJSObject()));
    }

    public void setSize(final int size) {
        setSize(getJSObject(), size);
    }

    private final native void setSize(final JSObject self, final int size) /*-{
        self._inputMinSize = size;
        self._input.size = size;
    }-*/;

    public void expand() {
        this.expand(getJSObject());
    }
    public final native void expand(final JSObject self) /*-{
        self.expand();
    }-*/;

    public void collapse() {
        this.collapse(getJSObject());
    }
    public final native void collapse(final JSObject self) /*-{
        self.collapse();
    }-*/;

    public void focus() {
        this.focus(getJSObject());
    }

    private final native void focus(final JSObject self) /*-{
        self._input.focus();
    }-*/;
}

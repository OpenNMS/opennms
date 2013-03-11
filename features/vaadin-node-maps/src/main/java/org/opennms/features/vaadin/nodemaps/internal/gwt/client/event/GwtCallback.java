package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.jsobject.JSObjectWrapper;

public abstract class GwtCallback extends JSObjectWrapper {

    protected GwtCallback() {
        super(JSObject.createJSFunction());
        setJSObject(getCallbackFunction(this));
    }

    protected abstract JSObject getCallbackFunction(GwtCallback callback);
}

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import com.google.gwt.core.client.JavaScriptObject;

public abstract class UpdateEvent extends JavaScriptObject {
    protected UpdateEvent() {
    }
    
    protected static final native UpdateEvent createUpdateEvent(final String type) /*-{
        var event = new CustomEvent(type, {
            'bubbles': false,
            'cancelable': true
        });
        return event;
    }-*/;

    public static final native String getType() /*-{
        return this.type;
    }-*/;
}

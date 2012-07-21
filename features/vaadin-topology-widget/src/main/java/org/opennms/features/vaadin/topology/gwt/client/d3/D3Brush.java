package org.opennms.features.vaadin.topology.gwt.client.d3;

import org.opennms.features.vaadin.topology.gwt.client.d3.D3Events.Handler;

import com.google.gwt.core.client.JavaScriptObject;

public class D3Brush extends JavaScriptObject {
    
    protected D3Brush() {};
    
    public final native D3Brush on(String event, Handler<?> handler) /*-{
        var f = function(d, i) {
            return handler.@org.opennms.features.vaadin.topology.gwt.client.d3.D3Events.Handler::call(Ljava/lang/Object;I)(d,i);
        }
    
        return this.on(event, f);
    }-*/;

}

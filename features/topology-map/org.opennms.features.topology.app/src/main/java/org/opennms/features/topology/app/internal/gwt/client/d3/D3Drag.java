package org.opennms.features.topology.app.internal.gwt.client.d3;

import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;

import com.google.gwt.core.client.JavaScriptObject;

public class D3Drag extends JavaScriptObject {
    
    protected D3Drag() {};
    
    public final native D3Drag on(String event, Handler<?> handler) /*-{
        
        var f = function(d, i) {
            return handler.@org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler::call(Ljava/lang/Object;I)(d,i);
        }
    
        return this.on(event, f);
    }-*/;

}

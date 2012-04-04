package org.opennms.features.vaadin.topology.gwt.client.d3;

import org.opennms.features.vaadin.topology.gwt.client.Edge;
import org.opennms.features.vaadin.topology.gwt.client.Vertex;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Events.Handler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class D3 extends JavaScriptObject {
    
    protected D3() {};
    
    public final native D3 select(String elementId) /*-{
        return this.select(elementId);
    }-*/;
    
    public final native D3 append(String tagName) /*-{
        return this.append(tagName);
    }-*/;
    
    public final native D3 attr(String propName, int value) /*-{
        return this.attr(propName, value);
    }-*/;
    
    public final native D3 attr(String propName, JavaScriptObject func) /*-{
        return this.attr(propName, func);
    }-*/;
    
    public final native D3 attr(String attrName, Func<?,?> func) /*-{
		var f = function(d, i){
			return func.@org.opennms.features.vaadin.topology.gwt.client.d3.Func::call(Ljava/lang/Object;I)(d,i);
		}
		return this.attr(attrName, f);
    }-*/;

    public final native D3 selectAll(String selectionName) /*-{
        return this.selectAll(selectionName);
    }-*/;
    
    public final native D3 data(JsArray array) /*-{
        return this.data(array);
    }-*/;

    public final native D3 enter() /*-{
        return this.enter();
    }-*/;
    
    public final native D3 update() /*-{
        return this.update();
    }-*/;

    public final native D3 attr(String propName, String value) /*-{
        return this.attr(propName, value);
    }-*/;
    
    public final native D3Scale scale() /*-{
        return this.scale;
    }-*/;

    public final native D3 style(String styleName, String value) /*-{
        return this.style(styleName, value);
    }-*/;

    public final native D3 transition() /*-{
        return this.transition();
    }-*/;

    public final native D3 duration(int duration) /*-{
        return this.duration(duration);
    }-*/;

    public final native D3 delay(int delayInMilliSeconds) /*-{
        return this.delay(delayInMilliSeconds);
    }-*/;

    public final native D3 data(int[] data) /*-{
        return this.data(data);
    }-*/;

	public final native D3 data(Vertex[] nodes) /*-{
		return this.data(nodes);
	}-*/;

	public final native D3 data(Edge[] links) /*-{
		return this.data(links);
	}-*/;

    public final native D3 exit() /*-{
        return this.exit();
    }-*/;

    public final native D3 remove() /*-{
        return this.remove();
    }-*/;

    public final native <T extends JavaScriptObject> D3 data(JsArray<T> data, Func<?, T> func) /*-{
    	var f = function(d, i) {
        	return func.@org.opennms.features.vaadin.topology.gwt.client.d3.Func::call(Ljava/lang/Object;I)(d,i);
        };
        return this.data(data, f);
		
    }-*/;

    public final native D3 text(JavaScriptObject textFunc) /*-{
        return this.text(textFunc);
    }-*/;

	public final native D3 on(String event, Handler<?> handler) /*-{
	   	var f = function(d, i) {
	   		return handler.@org.opennms.features.vaadin.topology.gwt.client.d3.D3Events.Handler::call(Ljava/lang/Object;I)(d,i);
	   	}
	
		return this.on(event, f);
	}-*/;

	public final native D3 style(String styleName, Func<String, ?> func) /*-{
		var f = function(d, i){
			return func.@org.opennms.features.vaadin.topology.gwt.client.d3.Func::call(Ljava/lang/Object;I)(d,i);
		}
		return this.style(styleName, f);
		
	}-*/;

    public static final native JavaScriptObject event() /*-{
        return $wnd.d3.event;
    }-*/;
    
    public static final native D3MouseEvent mouseEvent() /*-{
    	return $wnd.d3.event;
    }-*/;
    
    public static final native void eventPreventDefault() /*-{
        $wnd.d3.event.preventDefault();
    }-*/;
    
    public static final native D3 d3() /*-{
        return $wnd.d3;
    }-*/;
    
    public static final native JavaScriptObject property(String propertName) /*-{
        return function(d,i){
            return d[propertName];
        };
    }-*/;

}

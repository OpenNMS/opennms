package org.opennms.features.vaadin.topology.gwt.client.d3;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class D3Scale extends JavaScriptObject {
    
    protected D3Scale() {}

    public final native D3Scale ordinal() /*-{
        return this.ordinal();
    }-*/;

    public final native D3Scale domain(JsArray<?> array) /*-{
        return this.domain(array);
    }-*/;
    
    public final native D3Scale domain(int[] data) /*-{
    	return this.domain(data);
    }-*/;

    public final native JavaScriptObject rangePoints(JsArray<?> rangeArray, int i) /*-{
        return this.rangePoints(rangeArray, i);
    }-*/;
}

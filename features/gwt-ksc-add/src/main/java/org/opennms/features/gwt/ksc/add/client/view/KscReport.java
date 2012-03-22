package org.opennms.features.gwt.ksc.add.client.view;

import com.google.gwt.core.client.JavaScriptObject;

public class KscReport extends JavaScriptObject {
    protected KscReport() {}
    
    public final native int getId() /*-{
        return parseInt(this["@index"]);
    }-*/;
    
    public final native String getLabel() /*-{
        return this["@label"];
    }-*/;
}

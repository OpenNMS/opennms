package org.opennms.features.gwt.combobox.client.view;

import com.google.gwt.core.client.JavaScriptObject;

public class NodeDetail extends JavaScriptObject {

    protected NodeDetail() {}
    
    public final native String getLabel() /*-{
        return this["@label"];
    }-*/;
    
    public final native int getId() /*-{
        return this["@id"];
    }-*/;
}

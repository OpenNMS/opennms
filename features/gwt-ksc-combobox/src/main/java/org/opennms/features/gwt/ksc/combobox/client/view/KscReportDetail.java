package org.opennms.features.gwt.ksc.combobox.client.view;

import com.google.gwt.core.client.JavaScriptObject;

public class KscReportDetail extends JavaScriptObject {

    protected KscReportDetail() {};
    
    public final native String getLabel() /*-{
        return this[1];
    }-*/;
    
    public final native int getId() /*-{
        return parseInt(this[0]);
    }-*/;
}

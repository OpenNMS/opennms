package org.opennms.gwt.web.ui.reports.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ResourceListItem extends JavaScriptObject {

    protected ResourceListItem() {};
    
    public final native String getId()/*-{ return this.id }-*/;
    public final native String getValue()/*-{ return this.value }-*/;
    public final native String getType()/*-{ return this.type }-*/;
}

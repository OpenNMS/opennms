package org.opennms.features.gwt.snmpselect.list.client.view;

import com.google.gwt.core.client.JavaScriptObject;

public class SnmpCellListItem extends JavaScriptObject {
    
    protected SnmpCellListItem() {
        
    }
    
    public final native String getIfIndex()/*-{
        return this["@ifIndex"];
    }-*/;

    public final native String getSnmpType() /*-{
        return this.ifType;
    }-*/;

    public final native String getIfDescr() /*-{
        return this.ifDescr;
    }-*/;

    public final native String getIfName() /*-{
        return this.ifName;
    }-*/;

    public final native String getIfAlias() /*-{
        return this.ifAlias;
    }-*/;

    public final native String getCollectFlag() /*-{
        return this["@collectFlag"];
    }-*/;
    
    public final native void setCollectFlag(String flag) /*-{
        this["@collectFlag"] = flag;
    }-*/;

    public final native int getIfAdminStatus() /*-{
        return parseInt(this.ifAdminStatus);
    }-*/;

    public final native int getIfOperStatus() /*-{
        return parseInt(this.ifOperStatus);
    }-*/;

    public final native int getId() /*-{
        return parseInt(this["@id"]);
    }-*/;
         
}

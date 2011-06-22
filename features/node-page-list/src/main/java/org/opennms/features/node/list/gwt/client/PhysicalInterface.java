package org.opennms.features.node.list.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;


public class PhysicalInterface extends JavaScriptObject {
    
    protected PhysicalInterface() {};
    
    public final native String getId() /*-{
        return this["@id"];
    }-*/;
    
    public final native String getIfIndex() /*-{
        return this["@ifIndex"];
    }-*/;

    public final native String getSnmpIfDescr() /*-{
        return this.ifDescr;
    }-*/;

    public final native String getSnmpIfName() /*-{
        return this.ifName;
    }-*/;

    public final native String getSnmpIfAlias() /*-{
        return this.ifAlias;
    }-*/;

    public final native String getSnmpIfSpeed() /*-{
        return this.ifSpeed;
    }-*/;

    public final native String getIpAddress() /*-{
        return this.ipAddress;
    }-*/;

}

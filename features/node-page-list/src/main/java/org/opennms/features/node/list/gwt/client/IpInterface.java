package org.opennms.features.node.list.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;


public class IpInterface extends JavaScriptObject {

    protected IpInterface() {}
    
    public final native String getId()/*-{
        return this["@id"];
    }-*/;
    
    public final native String getIpAddress() /*-{
        return this.ipAddress;
    }-*/;
    
    public final native String getIpHostName() /*-{
        return this.hostName;
    }-*/;
    
    public final native String getManaged() /*-{
        return this["@isManaged"];
    }-*/;
    
    public final native String isDown() /*-{
        return this["@isDown"];
    }-*/;
    
    public final native String getMonitoredServiceCount()/*-{
        return this["@monitoredServiceCount"];
    }-*/;

    public final native String getIfIndex() /*-{
        return this["@ifIndex"];
    }-*/;
}

package org.opennms.features.gwt.snmpselect.list.client.rest;


public interface SnmpInterfaceRestService {
    
    public void setSnmpInterfaceRequestHandler(SnmpInterfaceRequestHandler handler);
    public void getInterfaceList();
    public void updateCollection(int interfaceId, String collectFlag);
}

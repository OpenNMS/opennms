package org.opennms.features.gwt.snmpselect.list.client.view.handler;

public interface SnmpSelectTableCollectUpdateHandler {
    
    void onSnmpInterfaceCollectUpdated(int interfaceId, String oldValue, String newValue);
}

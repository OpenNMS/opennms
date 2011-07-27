package org.opennms.features.gwt.snmpselect.list.client.rest;

import java.util.List;

import org.opennms.features.gwt.snmpselect.list.client.view.SnmpCellListItem;


public interface SnmpInterfaceRequestHandler {

    void onResponse(List<SnmpCellListItem> itemList);
    void onError(String message);

}

package org.opennms.features.node.list.gwt.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;

public class IpInterfaceTable extends CellTable<IpInterface> {

    public IpInterfaceTable() {
        super();
        initialize();
    }

    private void initialize() {
        TextColumn<IpInterface> ipAddressColumn = new TextColumn<IpInterface>() {

            @Override
            public String getValue(IpInterface ipIface) {
                return ipIface.getIpAddress();
            }
        };
        addColumn(ipAddressColumn, "IP Address");
        
        TextColumn<IpInterface> ipHostNameColumn = new TextColumn<IpInterface>() {

            @Override
            public String getValue(IpInterface ipIface) {
                return ipIface.getIpHostName();
            }
            
        };
        addColumn(ipHostNameColumn, "IP Host Name");
        
        TextColumn<IpInterface> managedColumn = new TextColumn<IpInterface>() {

            @Override
            public String getValue(IpInterface ipIface) {
                return ipIface.getManaged();
            }
        };
        addColumn(managedColumn, "Managed");
    }
}

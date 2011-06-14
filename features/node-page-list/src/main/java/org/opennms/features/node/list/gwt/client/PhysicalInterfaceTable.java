package org.opennms.features.node.list.gwt.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;

public class PhysicalInterfaceTable extends CellTable<PhysicalInterface> {

    public PhysicalInterfaceTable() {
        super();
        initialize();
    }

    private void initialize() {
        TextColumn<PhysicalInterface> indexColumn = new TextColumn<PhysicalInterface>(){

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getIndex();
            }
        };
        addColumn(indexColumn, "index");
        
        TextColumn<PhysicalInterface> snmpIfDescrColumn = new TextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getSnmpIfDescr();
            }
        };
        addColumn(snmpIfDescrColumn, "SNMP ifDescr");
        
        TextColumn<PhysicalInterface> snmpIfName = new TextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getSnmpIfName();
            }
        };
        addColumn(snmpIfName, "SNMP ifName");
        
        TextColumn<PhysicalInterface> snmpIfAliasColumn = new TextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getSnmpIfAlias();
            }
        };
        addColumn(snmpIfAliasColumn, "SNMP ifAlias");
        
        TextColumn<PhysicalInterface> snmpIfSpeedColumn = new TextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getSnmpIfSpeed();
            }
            
        };
        addColumn(snmpIfSpeedColumn, "SNMP ifSpeed");
        
        TextColumn<PhysicalInterface> ipAddresColumn = new TextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getIpAddress();
            }
            
        };
        addColumn(ipAddresColumn, "IP Address");
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.node.list.gwt.client;

import org.opennms.features.node.list.gwt.client.events.PhysicalInterfaceSelectionEvent;
import org.opennms.features.node.list.gwt.client.events.PhysicalInterfaceSelectionHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.Event;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class PhysicalInterfaceTable extends CellTable<PhysicalInterface> {
    
    SimpleEventBus m_eventBus = new SimpleEventBus();

    public PhysicalInterfaceTable() {
        super(15, (CellTable.Resources) GWT.create(OnmsTableResources.class));
        setStyleName("table table-condensed table-bordered severity");
        initialize();
    }
    
    //TODO:finish handler
    public void addSelectEventHandler(PhysicalInterfaceSelectionHandler handler) {
        getEventBus().addHandler(PhysicalInterfaceSelectionEvent.TYPE, handler);
    }

    private void initialize() {
        setRowStyles(new RowStyles<PhysicalInterface>() {
            
            @Override
            public String getStyleNames(PhysicalInterface physicalInterface, int rowIndex) {
                String bgStyle = null;
                if(physicalInterface.getIfAdminStatus() != 1){
                    bgStyle = "onms-ipinterface-status-unknown";
                }else if(physicalInterface.getIfAdminStatus() == 1 && physicalInterface.getIfOperStatus() == 1){
                    bgStyle = "onms-ipinterface-status-up";
                }else if(physicalInterface.getIfAdminStatus() == 1 && physicalInterface.getIfOperStatus() != 1){
                    bgStyle = "onms-ipinterface-status-down";
                }
                
                return bgStyle;
            }
        });
        
        
        addColumns();
        
        final SingleSelectionModel<PhysicalInterface> selectionModel = new SingleSelectionModel<PhysicalInterface>();
        setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                GWT.log("got a selection model change");
                
            }
        });
        
        addCellPreviewHandler(new CellPreviewEvent.Handler<PhysicalInterface>(){

            @Override
            public void onCellPreview(CellPreviewEvent<PhysicalInterface> event) {
                Event evt = Event.as(event.getNativeEvent());
                
                switch(evt.getTypeInt()) {
                    case Event.ONDBLCLICK:
                        PhysicalInterface selected = selectionModel.getSelectedObject();
                        getEventBus().fireEvent(new PhysicalInterfaceSelectionEvent(selected.getIfIndex()));
                        break;
                }
            }
            
        });
        
    }

    private void addColumns() {
        DblClickTextColumn<PhysicalInterface> indexColumn = new DblClickTextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physInterface) {
                return physInterface.getIfIndex();
            }
            
        };
        
        addColumn(indexColumn, "index");
        
        DblClickTextColumn<PhysicalInterface> snmpIfDescrColumn = new DblClickTextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getSnmpIfDescr();
            }
        };
        addColumn(snmpIfDescrColumn, "SNMP ifDescr");
        
        DblClickTextColumn<PhysicalInterface> snmpIfName = new DblClickTextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getSnmpIfName();
            }
        };
        addColumn(snmpIfName, "SNMP ifName");
        
        DblClickTextColumn<PhysicalInterface> snmpIfAliasColumn = new DblClickTextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getSnmpIfAlias();
            }
        };
        addColumn(snmpIfAliasColumn, "SNMP ifAlias");
        
        DblClickTextColumn<PhysicalInterface> snmpIfSpeedColumn = new DblClickTextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getSnmpIfSpeed();
            }
            
        };
        addColumn(snmpIfSpeedColumn, "SNMP ifSpeed");
        
//        DblClickTextColumn<PhysicalInterface> ipAddresColumn = new DblClickTextColumn<PhysicalInterface>() {
//
//            @Override
//            public String getValue(PhysicalInterface physIface) {
//                return physIface.getIpAddress();
//            }
//            
//        };
//        addColumn(ipAddresColumn, "IP Address");
    }
    
    
    public SimpleEventBus getEventBus() {
        return m_eventBus;
    }

    public void setEventBus(SimpleEventBus eventBus) {
        m_eventBus = eventBus;
    }
    
}

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

import org.opennms.features.node.list.gwt.client.events.IpInterfaceSelectionEvent;
import org.opennms.features.node.list.gwt.client.events.IpInterfaceSelectionHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.Event;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class IpInterfaceTable extends CellTable<IpInterface> {

    private SimpleEventBus m_eventBus = new SimpleEventBus();

    public IpInterfaceTable() {
        super(15, (CellTable.Resources) GWT.create(OnmsTableResources.class));
        initialize();
    }

    private void initialize() {
        
        setRowStyles(new RowStyles<IpInterface>() {
            
            @Override
            public String getStyleNames(IpInterface row, int rowIndex) {
                String bgStyle;
                if (row.getManaged().equals("U") || row.getManaged().equals("F") || row.getManaged().equals("N") || row.getMonitoredServiceCount() < 1) {
                    bgStyle = "onms-ipinterface-status-unknown";
                } else {
                    bgStyle = "onms-ipinterface-status-up";
                    if (row.isDown().equals("true")) {
                        bgStyle = "onms-ipinterface-status-down";
                    }
                }
                
                return bgStyle;
            }
        });
        
        
        DblClickTextColumn<IpInterface> ipAddressColumn = new DblClickTextColumn<IpInterface>() {

            @Override
            public String getValue(IpInterface ipIface) {
                return ipIface.getIpAddress();
            }
        };
        addColumn(ipAddressColumn, "IP Address");
        
        DblClickTextColumn<IpInterface> ipHostNameColumn = new DblClickTextColumn<IpInterface>() {

            @Override
            public String getValue(IpInterface ipIface) {
                return ipIface.getIpHostName();
            }
            
        };
        addColumn(ipHostNameColumn, "IP Host Name");
        
        DblClickTextColumn<IpInterface> ifIndexColumn = new DblClickTextColumn<IpInterface>() {
            
            @Override
            public String getValue(IpInterface ipIface) {
                return ipIface.getIfIndex();
            }
        };
        addColumn(ifIndexColumn, "ifIndex");
        
        DblClickTextColumn<IpInterface> managedColumn = new DblClickTextColumn<IpInterface>() {

            @Override
            public String getValue(IpInterface ipIface) {
                return ipIface.getManaged();
            }
        };
        addColumn(managedColumn, "Managed");
        
        
        final SingleSelectionModel<IpInterface> selectionModel = new SingleSelectionModel<IpInterface>();
        setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                IpInterface selected = selectionModel.getSelectedObject();
                // TODO: Do something here?
            }
        });
        
        addCellPreviewHandler(new CellPreviewEvent.Handler<IpInterface>(){

            @Override
            public void onCellPreview(CellPreviewEvent<IpInterface> event) {
                Event evt = Event.as(event.getNativeEvent());
                
                switch(evt.getTypeInt()) {
                    case Event.ONDBLCLICK:
                        IpInterface selected = selectionModel.getSelectedObject();
                        getEventBus().fireEvent(new IpInterfaceSelectionEvent(selected.getId()));
                        break;
                }
            }
            
        });
        
    }

    public void setEventBus(SimpleEventBus eventBus) {
        m_eventBus = eventBus;
    }

    public SimpleEventBus getEventBus() {
        return m_eventBus;
    }

    public void addSelectEventHandler(IpInterfaceSelectionHandler handler) {
        getEventBus().addHandler(IpInterfaceSelectionEvent.TYPE, handler);
    }
    
}

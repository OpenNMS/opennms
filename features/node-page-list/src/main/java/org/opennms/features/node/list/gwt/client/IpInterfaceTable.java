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
                if (row.getManaged().equals("U") || row.getManaged().equals("F") || row.getManaged().equals("N")) {
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
                if(selected != null) {
                }
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

package org.opennms.features.node.list.gwt.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Event;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class IpInterfaceTable extends CellTable<IpInterface> {


    public IpInterfaceTable() {
        super();
        initialize();
    }

    private void initialize() {
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
                        //TODO: fire event to top level
                        break;
                }
            }
            
        });
        
    }
}

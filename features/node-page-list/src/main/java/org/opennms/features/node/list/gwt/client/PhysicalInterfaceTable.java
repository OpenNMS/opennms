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
        
        DblClickTextColumn<PhysicalInterface> ipAddresColumn = new DblClickTextColumn<PhysicalInterface>() {

            @Override
            public String getValue(PhysicalInterface physIface) {
                return physIface.getIpAddress();
            }
            
        };
        addColumn(ipAddresColumn, "IP Address");
    }
    
    
    public SimpleEventBus getEventBus() {
        return m_eventBus;
    }

    public void setEventBus(SimpleEventBus eventBus) {
        m_eventBus = eventBus;
    }
    
}

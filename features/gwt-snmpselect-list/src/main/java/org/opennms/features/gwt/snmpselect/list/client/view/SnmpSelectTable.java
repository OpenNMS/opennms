package org.opennms.features.gwt.snmpselect.list.client.view;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.snmpselect.list.client.view.handler.SnmpSelectTableCollectUpdateHandler;
import org.opennms.features.gwt.tableresources.client.OnmsTableResources;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;

public class SnmpSelectTable extends CellTable<SnmpCellListItem> {
    
    private abstract class SnmpTextColumn extends Column<SnmpCellListItem, String>{

        public SnmpTextColumn() {
            super(new TextCell());
        }
        
    }
    
    private SnmpSelectTableCollectUpdateHandler m_fieldUpdater;
    
    public SnmpSelectTable() {
        super(15, (CellTable.Resources) GWT.create(OnmsTableResources.class));
        initializeColumns();
    }

    private void initializeColumns() {
        setRowStyles(new RowStyles<SnmpCellListItem>() {
            
            @Override
            public String getStyleNames(SnmpCellListItem cellListItem, int rowIndex) {
                String bgStyle = null;
                if(cellListItem.getIfAdminStatus() != 1){
                    bgStyle = "onms-ipinterface-status-unknown";
                }else if(cellListItem.getIfAdminStatus() == 1 && cellListItem.getIfOperStatus() == 1){
                    bgStyle = "onms-ipinterface-status-up";
                }else if(cellListItem.getIfAdminStatus() == 1 && cellListItem.getIfOperStatus() != 1){
                    bgStyle = "onms-ipinterface-status-down";
                }
                
                return bgStyle;
            }
        });
        
        SnmpTextColumn ifIndexColumn = new SnmpTextColumn() {

            @Override
            public String getValue(SnmpCellListItem item) {
                return item.getIfIndex();
            }
            
        };
        
        addColumn(ifIndexColumn, "Index");
        
        SnmpTextColumn snmpIfType = new SnmpTextColumn(){

            @Override
            public String getValue(SnmpCellListItem item) {
                return item.getSnmpType();
            }
            
        };
        
        addColumn(snmpIfType, "SNMP ifType");
        
        SnmpTextColumn snmpIfDescr = new SnmpTextColumn() {
            
            @Override
            public String getValue(SnmpCellListItem item) {
                return item.getIfDescr();
            }
        };
        addColumn(snmpIfDescr, "SNMP ifDescr");
        
        SnmpTextColumn snmpIfName = new SnmpTextColumn() {

            @Override
            public String getValue(SnmpCellListItem item) {
                return item.getIfName();
            }
            
        };
        addColumn(snmpIfName, "SNMP ifName");
        
        SnmpTextColumn snmpIfAlias = new SnmpTextColumn() {

            @Override
            public String getValue(SnmpCellListItem item) {
                return item.getIfAlias();
            }
        };
        addColumn(snmpIfAlias, "SNMP ifAlias");
        
        List<String> collectList = new ArrayList<String>() ;
        collectList.add("Collect");
        collectList.add("Don't Collect");
        collectList.add("Default");
        
        SelectionCell collectSelection = new SelectionCell(collectList);
        
        Column<SnmpCellListItem, String> collectColumn = new Column<SnmpCellListItem, String>(collectSelection){

            @Override
            public String getValue(SnmpCellListItem item) {
                if(item.getCollectFlag().equals("C") || item.getCollectFlag().equals("UC")) {
                    return "Collect";
                }else if(item.getCollectFlag().equals("N") || item.getCollectFlag().equals("UN")) {
                    return "Don't Collect";
                }else if(item.getCollectFlag().equals("Default")) {
                    return "Default";
                }else {
                    return "Default";
                }
            }
            
        };
        collectColumn.setFieldUpdater(new FieldUpdater<SnmpCellListItem, String>() {
            
            @Override
            public void update(int index, SnmpCellListItem object, String value) {
                String newCollectFlag = object.getCollectFlag();
                if(value.equals("Collect")) {
                    newCollectFlag = "UC";
                }else if(value.equals("Don't Collect")) {
                    newCollectFlag = "UN";
                }else if(value.equals("Default")) {
                    newCollectFlag = "Default";
                }
                
                object.setCollectFlag(newCollectFlag);
                
                if(getCollectUpdateHandler() != null) {
                    getCollectUpdateHandler().onSnmpInterfaceCollectUpdated(Integer.parseInt(object.getIfIndex()), object.getCollectFlag(), newCollectFlag);
                }
            }
        });
        addColumn(collectColumn, "Collect");
        
    }

    public void setCollectUpdateHandler(SnmpSelectTableCollectUpdateHandler fieldUpdater) {
        m_fieldUpdater = fieldUpdater;
    }

    public SnmpSelectTableCollectUpdateHandler getCollectUpdateHandler() {
        return m_fieldUpdater;
    }

}

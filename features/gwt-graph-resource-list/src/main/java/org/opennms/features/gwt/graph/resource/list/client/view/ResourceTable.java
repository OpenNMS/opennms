package org.opennms.features.gwt.graph.resource.list.client.view;

import org.opennms.features.gwt.tableresources.client.OnmsTableResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.SingleSelectionModel;

public class ResourceTable extends CellTable<ResourceListItem> {
    
    private SingleSelectionModel<ResourceListItem> m_selectionModel;
    
    public ResourceTable() {
        super(15, (CellTable.Resources) GWT.create(OnmsTableResources.class));
        initialize();
    }

    private void initialize() {
        TextColumn<ResourceListItem> resourceColumn = new TextColumn<ResourceListItem>() {
            
            @Override
            public String getValue(ResourceListItem listItem) {
                return "" + listItem.getValue();
            }
            
        };
        
        m_selectionModel = new SingleSelectionModel<ResourceListItem>(); 
        setSelectionModel(m_selectionModel);
        
        addColumn(resourceColumn, "Resources");
        
    }
    
    public ResourceListItem getSelectedResourceItem() {
        return m_selectionModel.getSelectedObject();
    }
}

package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

public interface ReportSelectListView<T> {
    
    public interface Presenter<T>{
        
    }
    
    void setDataList(List<ResourceListItem> dataList);
    Widget asWidget();
}

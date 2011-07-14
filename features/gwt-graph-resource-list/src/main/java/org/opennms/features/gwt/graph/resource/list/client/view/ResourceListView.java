package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

public interface ResourceListView<T> {
    
    public interface Presenter<T>{
        void onResourceItemSelected();
        void onSearchButtonClicked();
    }
    
    void setDataList(List<ResourceListItem> dataList);
    void showWarning();
    ResourceListItem getSelectedResource();
    Widget asWidget();
}

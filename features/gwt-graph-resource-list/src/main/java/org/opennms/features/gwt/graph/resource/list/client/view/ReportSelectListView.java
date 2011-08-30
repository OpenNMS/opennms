package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

public interface ReportSelectListView<T> {
    
    public interface Presenter<T>{
        void onGraphButtonClick();
        void onClearSelectionButtonClick();
        void onSearchButtonClick();
        void onGraphAllButtonClick();
    }
    
    List<ResourceListItem> getSelectedReports();
    void setDataList(List<ResourceListItem> dataList);
    void setPresenter(Presenter<T> presenter);
    Widget asWidget();
    void clearAllSelections();
    void showWarning();
    List<ResourceListItem> getDataList();
    Widget searchPopupTarget();
    List<ResourceListItem> getAllReports();
}

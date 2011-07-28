package org.opennms.features.gwt.ksc.combobox.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

public interface KscComboboxView<T> {
    
    public interface Presenter<T>{
        void onSearchButtonClicked();
        void onEnterKeyEvent();
        void onKscReportSelected();
    }
    
    String getSearchText();
    void setPresenter(Presenter<T> presenter);
    void setDataList(List<T> dataList);
    Widget asWidget();
    KscReportDetail getSelectedReport();
}

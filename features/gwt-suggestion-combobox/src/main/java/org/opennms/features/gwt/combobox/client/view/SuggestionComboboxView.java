package org.opennms.features.gwt.combobox.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

public interface SuggestionComboboxView<T> {
    
    public interface Presenter<T>{
        void onGoButtonClicked();
        void onEnterKeyEvent();
        void onNodeSelected();
    }
    
    String getSelectedText();
    void setPresenter(Presenter<T> presenter);
    void setData(List<T> dataList);
    Widget asWidget();
    NodeDetail getSelectedNode();
}

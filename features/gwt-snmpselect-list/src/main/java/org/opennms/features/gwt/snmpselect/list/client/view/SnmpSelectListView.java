package org.opennms.features.gwt.snmpselect.list.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

public interface SnmpSelectListView<T> {
    
    public interface Presenter<T>{
        void onSnmpInterfaceCollectUpdated(int interfaceId, String oldValue, String newValue);
    }
    
    Widget asWidget();

    void setPresenter(Presenter<T> presenter);
    void setDataList(List<SnmpCellListItem> dataList);
    SnmpCellListItem getUpdatedCell();
    void showError(String message);
    
}

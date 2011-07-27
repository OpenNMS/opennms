package org.opennms.features.gwt.snmpselect.list.client.presenter;

import java.util.List;

import org.opennms.features.gwt.snmpselect.list.client.rest.SnmpInterfaceRequestHandler;
import org.opennms.features.gwt.snmpselect.list.client.rest.SnmpInterfaceRestService;
import org.opennms.features.gwt.snmpselect.list.client.view.SnmpCellListItem;
import org.opennms.features.gwt.snmpselect.list.client.view.SnmpSelectListView;

import com.google.gwt.user.client.ui.HasWidgets;

public class SnmpSelectListPresenter implements Presenter, SnmpSelectListView.Presenter<SnmpCellListItem>{
    
    private SnmpSelectListView<SnmpCellListItem> m_view;
    private SnmpInterfaceRestService m_restService;

    public SnmpSelectListPresenter(SnmpSelectListView<SnmpCellListItem> view, SnmpInterfaceRestService service) {
        m_view = view;
        m_view.setPresenter(this);
        
        m_restService = service;
        m_restService.setSnmpInterfaceRequestHandler(new SnmpInterfaceRequestHandler() {
            
            @Override
            public void onResponse(List<SnmpCellListItem> dataList) {
                m_view.setDataList(dataList);
            }
            
            @Override
            public void onError(String message) {
                m_view.showError(message);
            }
        });
    }
    
    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(m_view.asWidget());
        
        m_restService.getInterfaceList();
    }

    @Override
    public void onSnmpInterfaceCollectUpdated(int ifIndex, String oldValue, String newValue) {
        m_restService.updateCollection(ifIndex, newValue);
    }

    public void setTestDataList(List<SnmpCellListItem> testDataList) {
        m_view.setDataList(testDataList);
    }

}

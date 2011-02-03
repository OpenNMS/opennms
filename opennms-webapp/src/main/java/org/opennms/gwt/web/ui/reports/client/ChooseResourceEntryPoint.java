package org.opennms.gwt.web.ui.reports.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RootPanel;

public class ChooseResourceEntryPoint implements EntryPoint, ChooseResourceView.Presenter {
        
    ChooseResourceViewImpl m_view;
    List<ResourceListItem> m_dataList;
    
    public void onModuleLoad() {
        
        RootPanel panel = RootPanel.get("opennms:chooseResource");
        if(panel != null) {
            m_dataList = getDataList();
            m_view = new ChooseResourceViewImpl();
            m_view.setPresenter(this);
            m_view.setDataList(m_dataList);
            panel.add(m_view);
        }
    }
    
    public final native JsArray<ResourceListItem> getJSData() /*-{
        return $wnd.data.records;
    }-*/;

    private List<ResourceListItem> getDataList() {
        JsArray<ResourceListItem> listItems = getJSData();
        List<ResourceListItem> data = new ArrayList<ResourceListItem>();
        for(int i = 0; i < listItems.length(); i++) {
            data.add(listItems.get(i));
        }
        return data;
    }

    public void navigateToUrl(String url) {
        Location.assign(url);
    }

    public void updateSearchTerm(String searchTerm) {
        if(searchTerm.equals("")) {
            m_view.setDataList(m_dataList);
        }else {
            List<ResourceListItem> newList = new ArrayList<ResourceListItem>();
            
            for(ResourceListItem item : m_dataList) {
                if(item.getValue().contains(searchTerm)) {
                    newList.add(item);
                }
            }
            
            m_view.setDataList(newList);
        }
        
    }

}

package org.opennms.gwt.web.ui.reports.client;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface ChooseResourceView extends IsWidget {
    
    void setDataList(List<ResourceListItem> dataList);
    void setPresenter(Presenter presenter);
    
    public interface Presenter{
        void navigateToUrl(String url);
        void updateSearchTerm(String searchTerm);
    }
}

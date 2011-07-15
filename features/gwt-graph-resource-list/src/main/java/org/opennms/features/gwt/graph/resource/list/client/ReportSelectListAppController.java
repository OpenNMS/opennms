package org.opennms.features.gwt.graph.resource.list.client;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.graph.resource.list.client.presenter.Presenter;
import org.opennms.features.gwt.graph.resource.list.client.presenter.ReportSelectListPresenter;
import org.opennms.features.gwt.graph.resource.list.client.view.ReportSelectListViewImpl;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HasWidgets;

public class ReportSelectListAppController implements Presenter {

    
    private List<ResourceListItem> m_resourceList;

    public ReportSelectListAppController(JsArray<ResourceListItem> resourceListData) {
        m_resourceList = convertJsArrayToList(resourceListData);
    }

    @Override
    public void go(HasWidgets container) {
        new ReportSelectListPresenter(new ReportSelectListViewImpl(m_resourceList)).go(container);
    }
    
    private List<ResourceListItem> convertJsArrayToList(JsArray<ResourceListItem> resourceList) {
        List<ResourceListItem> data = new ArrayList<ResourceListItem>();
        for(int i = 0; i < resourceList.length(); i++) {
            data.add(resourceList.get(i));
        }
        return data;
    }

}

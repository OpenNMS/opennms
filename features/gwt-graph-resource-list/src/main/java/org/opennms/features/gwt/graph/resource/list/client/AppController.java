package org.opennms.features.gwt.graph.resource.list.client;

import org.opennms.features.gwt.graph.resource.list.client.presenter.KscGraphResourceListPresenter;
import org.opennms.features.gwt.graph.resource.list.client.presenter.Presenter;
import org.opennms.features.gwt.graph.resource.list.client.view.KscChooseResourceListViewImpl;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;
import org.opennms.features.gwt.graph.resource.list.client.view.SearchPopup;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HasWidgets;

public class AppController implements Presenter {
    
    private JsArray<ResourceListItem> m_resourceList;
    
    public AppController(JsArray<ResourceListItem> resourceList) {
        m_resourceList = resourceList;
    }
    
    @Override
    public void go(HasWidgets container) {
        new KscGraphResourceListPresenter(new KscChooseResourceListViewImpl(), new SearchPopup(), m_resourceList).go(container);
    }

}

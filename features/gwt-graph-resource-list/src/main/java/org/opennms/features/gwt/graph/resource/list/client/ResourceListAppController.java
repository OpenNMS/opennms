package org.opennms.features.gwt.graph.resource.list.client;

import org.opennms.features.gwt.graph.resource.list.client.presenter.DefaultResourceListPresenter;
import org.opennms.features.gwt.graph.resource.list.client.presenter.Presenter;
import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListViewImpl;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;
import org.opennms.features.gwt.graph.resource.list.client.view.SearchPopup;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HasWidgets;

public class ResourceListAppController implements Presenter {

    private JsArray<ResourceListItem> m_resourceList;
    private String m_baseUrl;

    public ResourceListAppController(JsArray<ResourceListItem> resourceListData, String baseUrl) {
        m_resourceList = resourceListData;
        m_baseUrl = baseUrl;
    }

    @Override
    public void go(HasWidgets container) {
        new DefaultResourceListPresenter(new DefaultResourceListViewImpl(), new SearchPopup(), m_resourceList, m_baseUrl).go(container);
    }

}

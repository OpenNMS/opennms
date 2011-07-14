package org.opennms.features.gwt.graph.resource.list.client;

import org.opennms.features.gwt.graph.resource.list.client.presenter.KscGraphResourceListPresenter;
import org.opennms.features.gwt.graph.resource.list.client.presenter.Presenter;
import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListViewImpl;
import org.opennms.features.gwt.graph.resource.list.client.view.KscReportResourceChooser;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;
import org.opennms.features.gwt.graph.resource.list.client.view.SearchPopup;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HasWidgets;

public class KscChooseResourceAppController implements Presenter {
    
    private JsArray<ResourceListItem> m_resourceList;
    
    public KscChooseResourceAppController(JsArray<ResourceListItem> resourceList) {
        m_resourceList = resourceList;
    }
    
    @Override
    public void go(HasWidgets container) {
        new KscGraphResourceListPresenter(new DefaultResourceListViewImpl(), new SearchPopup(), m_resourceList, new KscReportResourceChooser()).go(container);
    }

}

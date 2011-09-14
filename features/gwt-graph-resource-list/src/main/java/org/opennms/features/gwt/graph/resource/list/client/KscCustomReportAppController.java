package org.opennms.features.gwt.graph.resource.list.client;

import org.opennms.features.gwt.graph.resource.list.client.presenter.KscCustomReportListPresenter;
import org.opennms.features.gwt.graph.resource.list.client.presenter.Presenter;
import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListViewImpl;
import org.opennms.features.gwt.graph.resource.list.client.view.KscCustomSelectionView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;
import org.opennms.features.gwt.graph.resource.list.client.view.SearchPopup;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HasWidgets;

public class KscCustomReportAppController implements Presenter {

    private JsArray<ResourceListItem> m_resourceList;
    private String m_baseUrl;

    public KscCustomReportAppController(JsArray<ResourceListItem> resourceList, String baseUrl) {
        m_resourceList = resourceList;
        m_baseUrl = baseUrl;
    }

    @Override
    public void go(HasWidgets container) {
        new KscCustomReportListPresenter(new DefaultResourceListViewImpl(), new SearchPopup(), m_resourceList, new KscCustomSelectionView(), m_baseUrl).go(container);

    }

}

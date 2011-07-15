package org.opennms.features.gwt.graph.resource.list.client.presenter;

import org.opennms.features.gwt.graph.resource.list.client.view.ReportSelectListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.user.client.ui.HasWidgets;

public class ReportSelectListPresenter implements Presenter, ReportSelectListView.Presenter<ResourceListItem> {

    private ReportSelectListView<ResourceListItem> m_view;

    public ReportSelectListPresenter(ReportSelectListView<ResourceListItem> view) {
        m_view = view;
    }
    
    
    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(m_view.asWidget());
    }

}

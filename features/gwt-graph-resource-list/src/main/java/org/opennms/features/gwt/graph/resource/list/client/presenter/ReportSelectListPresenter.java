package org.opennms.features.gwt.graph.resource.list.client.presenter;

import java.util.List;

import org.opennms.features.gwt.graph.resource.list.client.view.ReportSelectListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;

public class ReportSelectListPresenter implements Presenter, ReportSelectListView.Presenter<ResourceListItem> {

    private ReportSelectListView<ResourceListItem> m_view;

    public ReportSelectListPresenter(ReportSelectListView<ResourceListItem> view) {
        m_view = view;
        m_view.setPresenter(this);
    }
    
    
    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(m_view.asWidget());
    }


    @Override
    public void onGraphButtonClick() {
        List<ResourceListItem> reports = m_view.getSelectedReports();
        if(reports != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("graph/results.htm?reports=all&resourceId=");
            
            
            boolean first = true;
            for(ResourceListItem item : reports) {
                if(!first) {
                    
                    sb.append("&resourceId=");
                }
                sb.append(item.getId());
                first = false;
            }
            
            Location.assign(sb.toString());
        } else {
            m_view.showWarning();
        }
        
        
    }


    @Override
    public void onClearSelectionButtonClick() {
        m_view.clearAllSelections();
        
    }


    @Override
    public void onSearchButtonClick() {
        // TODO Auto-generated method stub
        
    }

}

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.api.IViewContribution;

import com.vaadin.ui.TabSheet;

public class WidgetManager {

    private List<IViewContribution> m_viewContributors = new ArrayList<IViewContribution>();
    private List<WidgetUpdateListener> m_listeners = new ArrayList<WidgetUpdateListener>();
    
    public WidgetManager() {
        
    }
    
    public void addUpdateListener(WidgetUpdateListener listener) {
        m_listeners.add(listener);
    }
    
    public TabSheet getTabSheet() {
        TabSheet tabSheet = new TabSheet();
        
        for(IViewContribution viewContrib : m_viewContributors) {
            
            if(viewContrib.getIcon() != null) {
                tabSheet.addTab(viewContrib.getView(), viewContrib.getTitle(), viewContrib.getIcon());
            } else {
                tabSheet.addTab(viewContrib.getView(), viewContrib.getTitle());
            }
            
        }
        
        return tabSheet;
    }
    
    public void onBind(IViewContribution viewContribution) {
        m_viewContributors.add(viewContribution);
        updateWidgetListeners();
    }

    private void updateWidgetListeners() {
        for(WidgetUpdateListener listener : m_listeners) {
            listener.widgetListUpdated(this);
        }
    }
    
    public void onUnbind(IViewContribution viewContribution) {
        m_viewContributors.remove(viewContribution);
    }

    public void removeUpdateListener(WidgetUpdateListener listener) {
        m_listeners.remove(listener);
    }
}

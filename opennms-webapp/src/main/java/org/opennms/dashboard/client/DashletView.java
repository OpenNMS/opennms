package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;

public class DashletView extends Composite {
    
    private Dashlet m_dashlet;

    protected DashletView(Dashlet dashlet) {
        m_dashlet = dashlet;
    }

    public DashletView(Dashlet dashlet, Widget view) {
        this(dashlet);
        initWidget(view);
    }
    
    public String getTitle() {
        return m_dashlet.getTitle();
    }
    
    public void setTitle(String title) {
        m_dashlet.setTitle(title);
    }
    
    public void addToTitleBar(Widget widget, DockLayoutConstant constraint) {
        m_dashlet.addToTitleBar(widget, constraint);
    }

}

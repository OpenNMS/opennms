package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class Dashlet extends Composite {
    
    class DashletTitle extends Composite {
        private DockPanel m_panel = new DockPanel();
        private Label m_label = new Label();
        private Label m_status = new Label();
        DashletTitle(String title) {
            
            m_label.setText(title);
            m_panel.add(m_label, DockPanel.WEST);
            m_panel.add(m_status, DockPanel.EAST);
            initWidget(m_panel);
        }
        
        
        public void setStatus(String status) {
            m_status.setText(status);
        }
        
    }
    
    protected VerticalPanel m_panel = new VerticalPanel();
    protected DashletTitle m_title;

    public Dashlet(String title) {
        
        m_title = new DashletTitle(title);
        m_panel.add(m_title);
        initWidget(m_panel);
    }

    protected void setContent(Widget content) {
        m_panel.add(content);
    }

    public void setStatus(String status) {
        m_title.setStatus(status);
    }

}
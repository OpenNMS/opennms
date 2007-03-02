package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;

public abstract class Dashlet extends Composite {
    
    class DashletTitle extends Composite {
        private DockPanel m_panel = new DockPanel();
        private Label m_label = new Label();
        
        DashletTitle(String title, DashletLoader loader) {
            
            m_label.setText(title);
            

            m_label.addStyleName("dashletTitle");
            m_panel.addStyleName("dashletTitlePanel");
            m_panel.add(m_label, DockPanel.WEST);
            m_panel.add(m_loader, DockPanel.EAST);

            m_panel.setCellVerticalAlignment(m_loader, DockPanel.ALIGN_MIDDLE);
            m_panel.setCellHorizontalAlignment(m_loader, DockPanel.ALIGN_RIGHT);

            initWidget(m_panel);
        }
        
        public void setTitle(String title) {
            m_label.setText(title);
        }
        
        public void add(Widget widget, DockLayoutConstant constraint) {
            m_panel.add(widget, constraint);
        }
        
    }
    
    private VerticalPanel m_panel = new VerticalPanel();
    private String m_title;
    private DashletTitle m_titleWidget;
    private DashletView m_view;
    private DashletLoader m_loader;
    private Dashboard m_dashboard;

    public Dashlet(Dashboard dashboard, String title) {
        m_title = title;
        m_dashboard = dashboard;
        initWidget(m_panel);
    }

    protected void setView(DashletView view) {
        m_view = view;
    }
    
    protected void setView(Widget view) {
        setView(new DashletView(this, view));
    }
    
    public String getTitle() {
        return m_title;
    }
    
    public void setTitle(String title) {
        m_title = title;
        m_titleWidget.setTitle(m_title);
    }
    
    public void addToTitleBar(Widget widget, DockLayoutConstant constraint) {
        m_titleWidget.add(widget, constraint);
    }
    
    public void setLoader(DashletLoader loader) {
        m_loader = loader;
    }

    protected void onLoad() {
        if (m_loader == null) {
            m_loader = new DashletLoader();
        }
        m_titleWidget = new DashletTitle(m_title, m_loader);
        
        m_panel.setStyleName("dashletPanel");
        
        m_panel.add(m_titleWidget);
        m_panel.add(m_view);
        
    }
    
    protected void error(Throwable caught) {
        m_dashboard.error(caught);
    }

    public void error(String err) {
        m_dashboard.error(err);
    }
    

	public void setSurveillanceSet(SurveillanceSet set) {
		// TODO Auto-generated method stub
		
	}
    
    
    
    

}
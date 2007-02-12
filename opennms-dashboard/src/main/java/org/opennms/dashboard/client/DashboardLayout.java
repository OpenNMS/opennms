package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: brozow
 * Date: Feb 10, 2007
 * Time: 8:34:34 PM
 */
public class DashboardLayout extends Composite {

    VerticalPanel m_base;
    
    DashboardLayout() {
    
        m_base = new VerticalPanel();
        
        Label label =new Label("dashboard");
        
        m_base.add(label);
        
        initWidget(m_base);
    }

    public void add(String string, Widget viewer) {
        m_base.add(viewer);
    }


}

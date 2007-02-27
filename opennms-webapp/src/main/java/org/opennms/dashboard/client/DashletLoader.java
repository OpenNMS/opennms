package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class DashletLoader extends Composite {
    
    Label m_label = new Label();
    
    DashletLoader() {
        m_label.addStyleName("dashletLoader");
        initWidget(m_label);
    }

    public void setStatus(String status) {
        m_label.setText(status);
    }
    
    

}

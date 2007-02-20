package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class OutageDashlet extends Composite {
    
    public OutageDashlet() {
        Label label = new Label("outageViewer");
        initWidget(label);
    }

}

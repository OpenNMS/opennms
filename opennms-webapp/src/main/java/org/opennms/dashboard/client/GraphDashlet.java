package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class GraphDashlet extends Composite {
    
    
    GraphDashlet() {
        Label label = new Label("graphViewer");
        
        initWidget(label);
    }

}

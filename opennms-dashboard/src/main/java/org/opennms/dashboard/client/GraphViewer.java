package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class GraphViewer extends Composite {
    
    
    GraphViewer() {
        Label label = new Label("graphViewer");
        
        initWidget(label);
    }

}

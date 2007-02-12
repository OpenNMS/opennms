package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class NodeStatusViewer extends Composite {
    
    public NodeStatusViewer() {
        Label label = new Label("nodeStatus");
        initWidget(label);
    }

}

package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Label;

public class GraphDashlet extends Dashlet {
    
    
    GraphDashlet() {
        
        super("Graphs");
        
        Label label = new Label("Graphs should go here.");
        
        setView(label);
    }

}

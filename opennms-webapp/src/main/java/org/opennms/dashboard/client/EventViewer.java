package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class EventViewer extends Composite {
    
    EventViewer() {
        Label label = new Label("eventViewer");
        initWidget(label);
    }

}

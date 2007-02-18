package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class NotificationViewer extends Composite {
    
    NotificationViewer() {
        Label label = new Label("notificationViewer");
        initWidget(label);
    }

}

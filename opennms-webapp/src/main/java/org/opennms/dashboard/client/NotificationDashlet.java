package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Label;

public class NotificationDashlet extends Dashlet {
    
    NotificationDashlet() {
        super("Notifications");
        Label label = new Label("Notifications Here.");
        setView(label);
    }

}

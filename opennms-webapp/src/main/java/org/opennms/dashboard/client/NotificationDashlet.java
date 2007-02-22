package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Label;

public class NotificationDashlet extends Dashlet {
    
    NotificationDashlet(Dashboard dashboard) {
        super(dashboard, "Notifications");
        Label label = new Label("Notifications Here.");
        setView(label);
    }

}

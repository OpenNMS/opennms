package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Label;

public class OutageDashlet extends Dashlet {
    
    public OutageDashlet(Dashboard dashboard) {
        super(dashboard, "Outages");
        setView(new Label("Outages go here."));
    }

}

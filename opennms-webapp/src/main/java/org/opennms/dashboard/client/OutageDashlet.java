package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Label;

public class OutageDashlet extends Dashlet {
    
    public OutageDashlet() {
        super("Outages");
        setView(new Label("Outages go here."));
    }

}

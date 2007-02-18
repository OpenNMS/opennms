package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class SiteStatusViewer extends Composite {
    
    public SiteStatusViewer() {
        Label label = new Label("siteStatus");
        initWidget(label);
    }

}

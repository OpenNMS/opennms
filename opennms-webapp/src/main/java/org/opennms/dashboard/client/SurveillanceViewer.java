package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class SurveillanceViewer extends Composite {
    
    public SurveillanceViewer() {
        Label label = new Label("surveillanceView");
        initWidget(label);
    }

}

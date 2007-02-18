package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class AlarmViewer extends Composite {
    
    AlarmViewer() {
        Label alarmViewer = new Label("alarmViewer");
        initWidget(alarmViewer);
    }

}

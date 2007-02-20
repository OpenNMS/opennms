package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AlarmDashlet extends Composite {
    
    VerticalPanel m_panel = new VerticalPanel();
    Label m_alarms = new Label();
    
    AlarmDashlet() {
        m_panel.add(new Label("alarmViewer"));
        m_panel.add(m_alarms);
        initWidget(m_panel);
    }

    public void setIntersection(SurveillanceIntersection intersection) {
        m_alarms.setText("Alarms for "+intersection);
    }

}

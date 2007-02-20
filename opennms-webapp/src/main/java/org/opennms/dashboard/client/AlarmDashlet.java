package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Label;

public class AlarmDashlet extends Dashlet {
    
    Label m_alarms = new Label();
    
    AlarmDashlet() {
        super("Alarms");
        setContent(m_alarms);
    }

    public void setIntersection(SurveillanceIntersection intersection) {
        m_alarms.setText("Alarms for "+intersection);
    }

}

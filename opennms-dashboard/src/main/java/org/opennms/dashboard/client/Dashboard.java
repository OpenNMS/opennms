package org.opennms.dashboard.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class Dashboard implements EntryPoint {

    public void onModuleLoad() {

        DashboardLayout db = new DashboardLayout();
        db.add("siteStatus", new SiteStatusViewer());
        db.add("alarms", new AlarmViewer());
        db.add("outages", new OutageViewer());
        db.add("events", new EventViewer());
        db.add("nodeStatus", new NodeStatusViewer());
        db.add("graphs", new GraphViewer());
        

        RootPanel.get().add(db);
    }

}

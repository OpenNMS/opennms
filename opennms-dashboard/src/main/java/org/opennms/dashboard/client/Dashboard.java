package org.opennms.dashboard.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class Dashboard implements EntryPoint {

    public void onModuleLoad() {

        add(new SiteStatusViewer(),   "siteStatus");
        add(new AlarmViewer(),        "alarms");
        add(new OutageViewer(),       "outages");
        add(new EventViewer(),        "events");
        add(new NodeStatusViewer(),   "nodeStatus");
        add(new NotificationViewer(), "notifications");
        add(new GraphViewer(),        "graphs");
        
    }
    
    public void add(Widget widget, String elementId) {
        RootPanel panel = RootPanel.get(elementId);
        if (panel == null) {
            throw new IllegalArgumentException("element with id '"+elementId+"' not found!");
        }
        panel.add(widget);
    }


}

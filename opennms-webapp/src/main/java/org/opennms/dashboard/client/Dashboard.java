package org.opennms.dashboard.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class Dashboard implements EntryPoint {

    public void onModuleLoad() {

        add(new SiteStatusViewer(),   "siteStatus");
        add(new AlarmViewer(),        "alarms");
        add(new OutageViewer(),       "outages");
        add(new EventViewer(),        "events");
        add(createNodeStatusViewer(),   "nodeStatus");
        add(new NotificationViewer(), "notifications");
        add(new GraphViewer(),        "graphs");
        
    }

    private NodeStatusViewer createNodeStatusViewer() {

        final NodeStatusViewer nodeStatusViewer = new NodeStatusViewer();
        
        // define the service you want to call
        NodeServiceAsync svc = (NodeServiceAsync) GWT.create(NodeService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) svc;
        endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()+"nodeService");
        
        AsyncCallback cb = new AsyncCallback() {

            public void onFailure(Throwable e) {
                throw new UnsupportedOperationException(".onFailure not yet implemented.");
            }

            public void onSuccess(Object arg) {
                nodeStatusViewer.setNodes((String[])arg);
            }
            
        };

        svc.getNodeNames(cb);

        return nodeStatusViewer;
    }
    
    public void add(Widget widget, String elementId) {
        RootPanel panel = RootPanel.get(elementId);
        if (panel == null) {
            throw new IllegalArgumentException("element with id '"+elementId+"' not found!");
        }
        panel.add(widget);
    }


}

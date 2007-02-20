package org.opennms.dashboard.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Dashboard implements EntryPoint {
    
    SurveillanceDashlet m_surveillance;
    AlarmDashlet m_alarms;
    OutageDashlet m_outages;
    NodeStatusDashlet m_nodeStatus;
    NotificationDashlet m_notifications;
    GraphDashlet m_graphs;

    public void onModuleLoad() {

        add(createSurveillanceDashlet(), "surveillanceView");
        add(createAlarmDashlet(),        "alarms");
        add(createOuageDashlet(),       "outages");
        add(createNodeStatusDashlet(),   "nodeStatus");
        add(createNotificationDashlet(), "notifications");
        add(createGraphDashlet(),        "graphs");
        
    }

    private GraphDashlet createGraphDashlet() {
        m_graphs = new GraphDashlet();
        return m_graphs;
    }

    private NotificationDashlet createNotificationDashlet() {
        m_notifications = new NotificationDashlet();
        return m_notifications;
    }

    private OutageDashlet createOuageDashlet() {
        m_outages = new OutageDashlet();
        return m_outages;
    }

    private AlarmDashlet createAlarmDashlet() {
        m_alarms = new AlarmDashlet();
        return m_alarms;
    }

    private SurveillanceDashlet createSurveillanceDashlet() {
        final SurveillanceDashlet surveillance = new SurveillanceDashlet();
        
        SurveillanceListener listener = new SurveillanceListener() {

            public void onAllClicked(SurveillanceDashlet viewer) {
                Window.alert("All");
            }

            public void onIntersectionClicked(SurveillanceDashlet viewer, SurveillanceIntersection intersection) {
                m_alarms.setIntersection(intersection);
            }

            public void onSurveillanceGroupClicked(SurveillanceDashlet viewer, SurveillanceGroup group) {
                Window.alert("Group: "+group);
            }
            
        };
        
        surveillance.addSurveillanceViewListener(listener);
        
        
        // define the service you want to call
        final SurveillanceServiceAsync svc = (SurveillanceServiceAsync) GWT.create(SurveillanceService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) svc;
        endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()+"surveillanceService.gwt");
        
        
        AsyncCallback cb = new AsyncCallback() {

            public void onFailure(Throwable e) {
                error(e);
            }

            public void onSuccess(Object arg) {
                SurveillanceData data = (SurveillanceData)arg;
                surveillance.setData(data);
                
                
                if (!data.isComplete()) {
                    final AsyncCallback cb = this;
                    Timer timer = new Timer() {
                        public void run() {
                            svc.getSurveillanceData(cb);
                        }
                    };
                    timer.schedule(2000);
                }
            }
            
        };

        svc.getSurveillanceData(cb);
        
        m_surveillance = surveillance;
        return m_surveillance;
    }

    private NodeStatusDashlet createNodeStatusDashlet() {

        final NodeStatusDashlet nodeStatus = new NodeStatusDashlet();
        
        // define the service you want to call
        NodeServiceAsync svc = (NodeServiceAsync) GWT.create(NodeService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) svc;
        endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()+"nodeService.gwt");
        
        AsyncCallback cb = new AsyncCallback() {

            public void onFailure(Throwable e) {
               error(e);
            }

            public void onSuccess(Object arg) {
                nodeStatus.setNodes((String[])arg);
            }
            
        };

        svc.getNodeNames(cb);

        m_nodeStatus = nodeStatus;
        return m_nodeStatus;
    }
    
    public void add(Widget widget, String elementId) {
        RootPanel panel = RootPanel.get(elementId);
        if (panel == null) {
            throw new IllegalArgumentException("element with id '"+elementId+"' not found!");
        }
        panel.add(widget);
    }

    public void error(Throwable e) {
        final DialogBox dialog = new DialogBox();

        VerticalPanel panel = new VerticalPanel();
        HTMLPanel html = new HTMLPanel(e.toString());
        panel.add(html);
        Button ok = new Button("OK");
        panel.add(ok);

        dialog.setWidget(panel);
        
        ok.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                dialog.hide();
            }
            
        });
        
        dialog.show();
        
    }

}

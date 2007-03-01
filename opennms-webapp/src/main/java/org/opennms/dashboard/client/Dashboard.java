package org.opennms.dashboard.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Dashboard implements EntryPoint, ErrorHandler {
    
    Dashlet m_surveillance;
    AlarmDashlet m_alarms;
    OutageDashlet m_outages;
    NodeStatusDashlet m_nodeStatus;
    NotificationDashlet m_notifications;
    GraphDashlet m_graphs;
    private SurveillanceServiceAsync m_surveillanceService;

    public void onModuleLoad() {

        add(createSurveillanceDashlet(), "surveillanceView");
        add(createAlarmDashlet(),        "alarms");
        add(createGraphDashlet(),        "graphs");
        add(createNotificationDashlet(), "notifications");
        //add(createOutageDashlet(),       "outages");
        //add(createNodeStatusDashlet(),   "nodeStatus");
        
        
        setSurveillanceSet(SurveillanceSet.DEFAULT);
        
    }

    private GraphDashlet createGraphDashlet() {
        m_graphs = new GraphDashlet(this);
        m_graphs.setSurveillanceService(getSurveillanceService());
        return m_graphs;
    }

    private NotificationDashlet createNotificationDashlet() {
        m_notifications = new NotificationDashlet(this);
        return m_notifications;
    }

    private OutageDashlet createOutageDashlet() {
        m_outages = new OutageDashlet(this);
        return m_outages;
    }

    private AlarmDashlet createAlarmDashlet() {
        m_alarms = new AlarmDashlet(this);
        m_alarms.setSurveillanceService(getSurveillanceService());
        m_alarms.setSurveillanceSet(SurveillanceSet.DEFAULT);
        return m_alarms;
    }

    private Dashlet createSurveillanceDashlet() {
        SurveillanceDashlet surveillance = new SurveillanceDashlet(this);
        
        SurveillanceListener listener = new SurveillanceListener() {

            public void onAllClicked(Dashlet viewer) {
                setSurveillanceSet(SurveillanceSet.DEFAULT);
            }

            public void onIntersectionClicked(Dashlet viewer, SurveillanceIntersection intersection) {
                setSurveillanceSet(intersection);
            }

            public void onSurveillanceGroupClicked(Dashlet viewer, SurveillanceGroup group) {
                setSurveillanceSet(group);
            }
            
        };
        
        surveillance.addSurveillanceViewListener(listener);
        
        final SurveillanceServiceAsync svc = getSurveillanceService();
        
        
        surveillance.setSurveillanceService(svc);
        
        m_surveillance = surveillance;
        return m_surveillance;
    }
    
    private SurveillanceServiceAsync getSurveillanceService() {
        if (m_surveillanceService == null) {
            String serviceEntryPoint = GWT.getModuleBaseURL()+"surveillanceService.gwt";

            // define the service you want to call
            final SurveillanceServiceAsync svc = (SurveillanceServiceAsync) GWT.create(SurveillanceService.class);
            ServiceDefTarget endpoint = (ServiceDefTarget) svc;
            endpoint.setServiceEntryPoint(serviceEntryPoint);
            m_surveillanceService = svc;
        }
        return m_surveillanceService;
    }

    private NodeStatusDashlet createNodeStatusDashlet() {

        final NodeStatusDashlet nodeStatus = new NodeStatusDashlet(this);
        
        AsyncCallback cb = new AsyncCallback() {

            public void onFailure(Throwable e) {
               error(e);
            }

            public void onSuccess(Object arg) {
                nodeStatus.setNodes((String[])arg);
            }
            
        };

        getSurveillanceService().getNodeNames(null, cb);

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
        error(e.toString());
    }
    
    public void error(String err) {
        final DialogBox dialog = new DialogBox();

        VerticalPanel panel = new VerticalPanel();
        HTMLPanel html = new HTMLPanel(err);
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

    private void setSurveillanceSet(SurveillanceSet set) {
        m_alarms.setSurveillanceSet(set);
        m_graphs.setSurveillanceSet(set);
    }

}

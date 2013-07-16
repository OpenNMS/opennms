/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.dashboard.client;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>Dashboard class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class Dashboard implements EntryPoint, ErrorHandler {
    
    Dashlet m_surveillance;
    AlarmDashlet m_alarms;
    OutageDashlet m_outages;
    NodeStatusDashlet m_nodeStatus;
    NotificationDashlet m_notifications;
    GraphDashlet m_graphs;
    private SurveillanceServiceAsync m_surveillanceService;

    /**
     * <p>onModuleLoad</p>
     */
    @Override
    public void onModuleLoad() {
        
        
        add(createSurveillanceDashlet(), "surveillanceView");
        add(createAlarmDashlet(),        "alarms");
        add(createGraphDashlet(),        "graphs");
        add(createNotificationDashlet(), "notifications");
        //add(createOutageDashlet(),       "outages");
        add(createNodeStatusDashlet(),   "nodeStatus");
        
        
        setSurveillanceSet(SurveillanceSet.DEFAULT);
        
    }

    private GraphDashlet createGraphDashlet() {
        m_graphs = new GraphDashlet(this);
        m_graphs.setSurveillanceService(getSurveillanceService());
        return m_graphs;
    }

    private NotificationDashlet createNotificationDashlet() {
        m_notifications = new NotificationDashlet(this);
        m_notifications.setSurveillanceService(getSurveillanceService());
        return m_notifications;
    }

//    private OutageDashlet createOutageDashlet() {
//        m_outages = new OutageDashlet(this);
//        return m_outages;
//    }

    private AlarmDashlet createAlarmDashlet() {
        m_alarms = new AlarmDashlet(this);
        m_alarms.setSurveillanceService(getSurveillanceService());
        m_alarms.setSurveillanceSet(SurveillanceSet.DEFAULT);
        return m_alarms;
    }

    private Dashlet createSurveillanceDashlet() {
        SurveillanceDashlet surveillance = new SurveillanceDashlet(this);
        
        SurveillanceListener listener = new SurveillanceListener() {

            @Override
            public void onAllClicked(Dashlet viewer) {
                setSurveillanceSet(SurveillanceSet.DEFAULT);
            }

            @Override
            public void onIntersectionClicked(Dashlet viewer, SurveillanceIntersection intersection) {
                setSurveillanceSet(intersection);
            }

            @Override
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
            String serviceEntryPoint = GWT.getHostPageBaseURL()+"surveillanceService.gwt";

            // define the service you want to call
            final SurveillanceServiceAsync svc = (SurveillanceServiceAsync) GWT.create(SurveillanceService.class);
            ServiceDefTarget endpoint = (ServiceDefTarget) svc;
            endpoint.setServiceEntryPoint(serviceEntryPoint);
            m_surveillanceService = svc;
        }
        return m_surveillanceService;
    }

    private NodeStatusDashlet createNodeStatusDashlet() {
        m_nodeStatus = new NodeStatusDashlet(this);
        m_nodeStatus.setSurveillanceService(getSurveillanceService());
        m_nodeStatus.setSurveillanceSet(SurveillanceSet.DEFAULT);
        return m_nodeStatus;
    }
    
    /**
     * <p>add</p>
     *
     * @param widget a {@link com.google.gwt.user.client.ui.Widget} object.
     * @param elementId a {@link java.lang.String} object.
     */
    public void add(Widget widget, String elementId) {
        RootPanel panel = RootPanel.get(elementId);
        if (panel != null) {
            panel.add(widget);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void error(Throwable e) {
        error(e.toString());
    }
    
    /**
     * <p>error</p>
     *
     * @param err a {@link java.lang.String} object.
     */
    public void error(String err) {
        final DialogBox dialog = new DialogBox();
        dialog.setText("Error Occurred");
        
        VerticalPanel panel = new VerticalPanel();
        HTMLPanel html = new HTMLPanel(err);
        html.setStyleName("Message");
        panel.add(html);
        
        Button ok = new Button("OK");
        SimplePanel buttonPanel = new SimplePanel();
        buttonPanel.setWidget(ok);
        buttonPanel.setStyleName("Button");
        panel.add(buttonPanel);
        
        dialog.setPopupPosition(Window.getScrollLeft() + 100, Window.getScrollTop() + 100);
        dialog.setWidget(panel);
        
        ok.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                dialog.hide();
            }
            
        });
        
        dialog.show();
        
    }

    private void setSurveillanceSet(SurveillanceSet set) {
    	m_surveillance.setSurveillanceSet(set);
        m_alarms.setSurveillanceSet(set);
        m_graphs.setSurveillanceSet(set);
        m_notifications.setSurveillanceSet(set);
        m_nodeStatus.setSurveillanceSet(set);
    }

}

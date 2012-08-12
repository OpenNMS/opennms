/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2007-2011 The OpenNMS Group,
 * Inc. OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.dashboard.client.portlet;

import org.opennms.features.dashboard.client.layout.BasicDBLayout;
import org.opennms.features.dashboard.client.layout.VerticalDBLayout;
import org.opennms.features.dashboard.client.portlet.IBasicPortlet;
import org.opennms.features.dashboard.client.portlet.Portlet;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
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

/**
 * <p>
 * --Dashboard class--
 * </p>
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

    private BasicDBLayout dbLayout;

    private static RootPanel log;

    /**
     * <p>
     * onModuleLoad
     * </p>
     */
    public void onModuleLoad() {

        StringBuilder mz = new StringBuilder();

        try {
            // mz.append("###Start###");
            // BasicDBLayout dbLayout = new VerticalDBLayout();
            // DOM.setStyleAttribute(dbLayout.getElement(), "backgroundColor",
            // "pink");
            // dbLayout.setPixelSize(1286, 600);
            // mz.append("###Layout panel adding to DOM###");
            // RootPanel panel = RootPanel.get("dashboardDiv");
            // if (panel == null) {
            // mz.append("###dashboard panel is null###");
            // RootPanel.get().add(dbLayout);
            // mz.append("###Added to rootpanel###");
            // } else {
            // mz.append("###dashboard panel is not null###");
            // panel.add(dbLayout);
            // mz.append("###Added to dashboard panel###");
            // }
            // mz.append("###Adding portlet 1###");
            // SimplePanel p1=new SimplePanel();
            // p1.setSize("100%", "100%");
            // DOM.setStyleAttribute(p1.getElement(), "backgroundColor",
            // "yellow");
            // IBasicPortlet B1=new Portlet("P1");
            // B1.setContentSize(500, 400);
            // B1.setContent(p1);
            // dbLayout.addNewPortlet(B1);
            // mz.append("###Successfully dded portlet 1###");
            // mz.append("###Adding portlet 2###");
            // SimplePanel p2=new SimplePanel();
            // p2.setSize("100%", "100%");
            // DOM.setStyleAttribute(p2.getElement(), "backgroundColor",
            // "green");
            // IBasicPortlet B2=new Portlet("P2");
            // B2.setContentSize(500, 400);
            // B2.setContent(p2);
            // dbLayout.addNewPortlet(B2);
            // mz.append("###Successfully dded portlet 2###");
            // mz.append("###Finish###");
            // } catch (Exception e) {
            // mz.append("###Error###");
            // mz.append("###"+e.getStackTrace()+"###");
            // }

            mz.append("###Start###");
            dbLayout = new VerticalDBLayout();
//            DOM.setStyleAttribute(dbLayout.getElement(), "backgroundColor","pink");
            dbLayout.setPixelSize(1286, 600);
            mz.append("###Layout panel adding to DOM###");
            RootPanel panel = RootPanel.get("dashboardDiv");
            if (panel == null) {
                mz.append("###dashboard panel is null###");
                RootPanel.get().add(dbLayout);
                mz.append("###Added to rootpanel###");
            } else {
                mz.append("###dashboard panel is not null###");
                panel.add(dbLayout);
                mz.append("###Added to dashboard panel###");
            }
            mz.append("###Adding portlet 1###");

             add(createSurveillanceDashlet(dbLayout), "surveillanceView");
             add(createAlarmDashlet(dbLayout), "alarms");
             add(createGraphDashlet(dbLayout), "graphs");
             add(createNotificationDashlet(dbLayout), "notifications");
             //add(createOutageDashlet(), "outages");
             add(createNodeStatusDashlet(dbLayout), "nodeStatus");

            SimplePanel p1 = new SimplePanel();
            p1.setSize("100%", "100%");
            DOM.setStyleAttribute(p1.getElement(), "backgroundColor",
                                  "yellow");
            IBasicPortlet B1 = new Portlet("P1z") ;
            B1.setContentSize(500, 400);
            B1.setContent(p1);
            dbLayout.addNewPortlet(B1);
            mz.append("###Successfully dded portlet 1###");
            mz.append("###Adding portlet 2###");
            SimplePanel p2 = new SimplePanel();
            p2.setSize("100%", "100%");
            DOM.setStyleAttribute(p2.getElement(), "backgroundColor", "green");
            IBasicPortlet B2 = new Dashlet(dbLayout, "P2z") {
            };
            B2.setContentSize(500, 400);
            B2.setContent(p2);
            dbLayout.addNewPortlet(B2);

            setSurveillanceSet(SurveillanceSet.DEFAULT);
        } catch (Exception e) {
            mz.append("###Error###");
            mz.append("###" + e.getStackTrace() + "###");
        }
        log = RootPanel.get("log");
        log(mz.toString());
    }

    private GraphDashlet createGraphDashlet(BasicDBLayout dbLayout) {
        m_graphs = new GraphDashlet(dbLayout);
        m_graphs.setSurveillanceService(getSurveillanceService());
        return m_graphs;
    }

    private NotificationDashlet createNotificationDashlet(
            BasicDBLayout dbLayout) {
        m_notifications = new NotificationDashlet(dbLayout);
        m_notifications.setSurveillanceService(getSurveillanceService());
        return m_notifications;
    }

    // private OutageDashlet createOutageDashlet() {
    // m_outages = new OutageDashlet(this);
    // return m_outages;
    // }

    private AlarmDashlet createAlarmDashlet(BasicDBLayout dbLayout) {
        m_alarms = new AlarmDashlet(dbLayout);
        m_alarms.setSurveillanceService(getSurveillanceService());
        m_alarms.setSurveillanceSet(SurveillanceSet.DEFAULT);
        return m_alarms;
    }

    private Dashlet createSurveillanceDashlet(BasicDBLayout dbLayout) {
        SurveillanceDashlet surveillance = new SurveillanceDashlet(dbLayout);

        SurveillanceListener listener = new SurveillanceListener() {

            public void onAllClicked(Dashlet viewer) {
                setSurveillanceSet(SurveillanceSet.DEFAULT);
            }

            public void onIntersectionClicked(Dashlet viewer,
                    SurveillanceIntersection intersection) {
                setSurveillanceSet(intersection);
            }

            public void onSurveillanceGroupClicked(Dashlet viewer,
                    SurveillanceGroup group) {
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
            String serviceEntryPoint = GWT.getHostPageBaseURL()
                    + "surveillanceService.gwt";

            // define the service you want to call
            final SurveillanceServiceAsync svc = (SurveillanceServiceAsync) GWT.create(SurveillanceService.class);
            ServiceDefTarget endpoint = (ServiceDefTarget) svc;
            endpoint.setServiceEntryPoint(serviceEntryPoint);
            m_surveillanceService = svc;
        }
        return m_surveillanceService;
    }

    private NodeStatusDashlet createNodeStatusDashlet(BasicDBLayout dbLayout) {
        m_nodeStatus = new NodeStatusDashlet(dbLayout);
        m_nodeStatus.setSurveillanceService(getSurveillanceService());
        m_nodeStatus.setSurveillanceSet(SurveillanceSet.DEFAULT);
        return m_nodeStatus;
    }

    /**
     * <p>
     * add
     * </p>
     * 
     * @param widget
     *            a {@link com.google.gwt.user.client.ui.Widget} object.
     * @param elementId
     *            a {@link java.lang.String} object.
     */
    public void add(Portlet widget, String elementId) {
        // RootPanel panel = RootPanel.get(elementId);
        // if (panel == null) {
        // throw new
        // IllegalArgumentException("element with id '"+elementId+"' not found!");
        // }
        // panel.add(widget);
        dbLayout.addNewPortlet(widget);
    }

    /** {@inheritDoc} */
    public void error(Throwable e) {
        error(e.toString());
    }

    /**
     * <p>
     * error
     * </p>
     * 
     * @param err
     *            a {@link java.lang.String} object.
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

        dialog.setPopupPosition(Window.getScrollLeft() + 100,
                                Window.getScrollTop() + 100);
        dialog.setWidget(panel);

        ok.addClickHandler(new ClickHandler() {

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

    public static void log(String mz) {
        if (log != null) {
            DOM.setInnerText(log.getElement(), mz);
        }
    }

}

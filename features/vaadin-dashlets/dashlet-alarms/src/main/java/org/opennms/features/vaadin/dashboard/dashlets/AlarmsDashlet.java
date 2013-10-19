/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.server.Page;
import com.vaadin.ui.*;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch;
import org.opennms.features.vaadin.dashboard.config.ui.editors.CriteriaBuilderHelper;
import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.*;

import java.util.Calendar;
import java.util.List;

/**
 * This class represents a Alert Dashlet with minimum details.
 *
 * @author Christian Pape
 */
public class AlarmsDashlet extends AbstractDashlet {
    /**
     * The {@link AlarmDao} used
     */
    private AlarmDao m_alarmDao;
    /**
     * The {@link NodeDao} used
     */
    private NodeDao m_nodeDao;
    /**
     * boosted value
     */
    private boolean boosted = false;
    /**
     * Helper for handling criterias
     */
    private CriteriaBuilderHelper m_criteriaBuilderHelper = new CriteriaBuilderHelper(OnmsAlarm.class, OnmsNode.class, OnmsCategory.class, OnmsEvent.class);

    /**
     * wallboard layout
     */
    private VerticalLayout m_wallboardLayout = null;

    /**
     * dashboard layout
     */
    private VerticalLayout m_dashboardLayout = null;


    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     * @param alarmDao    the {@link AlarmDao} to be used
     * @param nodeDao     the {@link NodeDao} to be used
     */
    public AlarmsDashlet(String name, DashletSpec dashletSpec, AlarmDao alarmDao, NodeDao nodeDao) {
        super(name, dashletSpec);
        /**
         * Setting the member fields
         */
        m_alarmDao = alarmDao;
        m_nodeDao = nodeDao;
    }

    @Override
    public Component getWallboardComponent() {
        if (m_wallboardLayout == null) {
            m_wallboardLayout = new VerticalLayout();
            m_wallboardLayout.setCaption(getName());
            m_wallboardLayout.setWidth("100%");
        }
        return m_wallboardLayout;
    }

    @Override
    public Component getDashboardComponent() {
        if (m_dashboardLayout == null) {
            /**
             * Setting up the layout
             */
            m_dashboardLayout = new VerticalLayout();
            m_dashboardLayout.setCaption(getName());
            m_dashboardLayout.setWidth("100%");
        }
        return m_dashboardLayout;
    }

    /**
     * Returns the alarms defined by this dashlet.
     *
     * @return the list of alarms
     */
    private List<OnmsAlarm> getAlarms() {
        final CriteriaBuilder alarmCb = new CriteriaBuilder(OnmsAlarm.class);

        alarmCb.alias("node", "node");
        alarmCb.alias("node.categories", "category");
        alarmCb.alias("lastEvent", "event");

        String criteria = getDashletSpec().getParameters().get("criteria");

        m_criteriaBuilderHelper.parseConfiguration(alarmCb, criteria);

        alarmCb.fetch("firstEvent", Fetch.FetchType.EAGER);
        alarmCb.fetch("lastEvent", Fetch.FetchType.EAGER);

        alarmCb.distinct();

        return m_alarmDao.findMatching(alarmCb.toCriteria());
    }

    /**
     * Adds the alarms components to a {@link com.vaadin.ui.AbstractOrderedLayout}
     *
     * @param component the component to add alarms to
     * @param alarms    the alarms list
     */
    private void addComponents(AbstractOrderedLayout component, List<OnmsAlarm> alarms) {
        if (alarms.size() == 0) {
            Label label = new Label("No alarms found!");
            label.addStyleName("alerts-noalarms-font");
            component.addComponent(label);
        } else {
            for (OnmsAlarm onmsAlarm : alarms) {
                OnmsNode onmsNode = null;

                if (onmsAlarm.getNodeId() != null) {
                    CriteriaBuilder nodeCb = new CriteriaBuilder(OnmsNode.class);
                    nodeCb.eq("id", onmsAlarm.getNodeId());

                    List<OnmsNode> nodes = m_nodeDao.findMatching(nodeCb.toCriteria());

                    if (nodes.size() == 1) {
                        onmsNode = nodes.get(0);
                    }
                }
                component.addComponent(createAlarmComponent(onmsAlarm, onmsNode));

                OnmsSeverity boostSeverity = OnmsSeverity.valueOf(getDashletSpec().getParameters().get("boostSeverity"));

                if (onmsAlarm.getSeverity().isGreaterThanOrEqual(boostSeverity)) {
                    boosted = true;
                }
            }
        }
    }

    /**
     * Updates the alarm data using the associated {@link AlarmDao} and {@link NodeDao} instances.
     *
     * @return true, if boosted, false otherwise
     */
    @Override
    public void updateDashboard() {
        List<OnmsAlarm> alarms = getAlarms();

        m_dashboardLayout.removeAllComponents();

        injectDashboardStyles();

        boosted = false;

        addComponents(m_dashboardLayout, alarms);
    }

    /**
     * Updates the alarm data using the associated {@link AlarmDao} and {@link NodeDao} instances.
     *
     * @return true, if boosted, false otherwise
     */
    @Override
    public void updateWallboard() {
        List<OnmsAlarm> alarms = getAlarms();

        OnmsSeverity boostSeverity = OnmsSeverity.valueOf(getDashletSpec().getParameters().get("boostSeverity"));

        m_wallboardLayout.removeAllComponents();

        injectWallboardStyles();

        boosted = false;

        addComponents(m_wallboardLayout, alarms);
    }

    /**
     * Returns a human-readable {@link String} representation of a timestamp in the past.
     *
     * @param secondsAll the timestamp to be used
     * @return a human-readable representation
     */
    public String getHumanReadableFormat(long secondsAll) {
        long seconds = secondsAll;
        long minutes = 0;
        long hours = 0;

        if (seconds / 60 > 0) {
            long rest = seconds % 60;
            minutes = seconds / 60;
            seconds = rest;
        }

        if (minutes / 60 > 0) {
            long rest = minutes % 60;
            hours = minutes / 60;
            minutes = rest;
        }

        String output = "";

        if (hours > 0) {
            output = hours + "h, " + minutes + "m, " + seconds + "s";
        } else {
            if (minutes > 0) {
                output = minutes + "m, " + seconds + "s";
            } else {
                output = seconds + "s";
            }
        }

        return output + " ago";
    }

    /**
     * Injects CSS styles on current page for this dashlet
     */
    private void injectWallboardStyles() {
        Page.getCurrent().getStyles().add(".alerts.cleared { background: #000000; border-left: 15px solid #858585; }");
        Page.getCurrent().getStyles().add(".alerts.normal { background: #000000; border-left: 15px solid #336600; }");
        Page.getCurrent().getStyles().add(".alerts.indeterminate {  background: #000000; border-left: 15px solid #999; }");
        Page.getCurrent().getStyles().add(".alerts.warning { background: #000000; border-left: 15px solid #FFCC00; }");
        Page.getCurrent().getStyles().add(".alerts.minor { background: #000000;  border-left: 15px solid #FF9900; }");
        Page.getCurrent().getStyles().add(".alerts.major { background: #000000; border-left: 15px solid #FF3300; }");
        Page.getCurrent().getStyles().add(".alerts.critical { background: #000000; border-left: 15px solid #CC0000; }");
        Page.getCurrent().getStyles().add(".alerts-font {color: #3ba300; font-size: 18px; line-height: normal; }");
        Page.getCurrent().getStyles().add(".alerts-noalarms-font { font-size: 18px; line-height: normal; }");
        Page.getCurrent().getStyles().add(".alerts { padding: 5px 5px; margin: 1px; }");
    }

    /**
     * Injects CSS styles on current page for this dashlet
     */
    private void injectDashboardStyles() {
        Page.getCurrent().getStyles().add(".alerts.cleared { background: #000000; border-left: 8px solid #858585; }");
        Page.getCurrent().getStyles().add(".alerts.normal { background: #000000; border-left: 8px solid #336600; }");
        Page.getCurrent().getStyles().add(".alerts.indeterminate {  background: #000000; border-left: 8px solid #999; }");
        Page.getCurrent().getStyles().add(".alerts.warning { background: #000000; border-left: 8px solid #FFCC00; }");
        Page.getCurrent().getStyles().add(".alerts.minor { background: #000000;  border-left: 8px solid #FF9900; }");
        Page.getCurrent().getStyles().add(".alerts.major { background: #000000; border-left: 8px solid #FF3300; }");
        Page.getCurrent().getStyles().add(".alerts.critical { background: #000000; border-left: 8px solid #CC0000; }");
        Page.getCurrent().getStyles().add(".alerts-font {color: #3ba300; font-size: 11px; line-height: normal; }");
        Page.getCurrent().getStyles().add(".alerts-noalarms-font { font-size: 11px; line-height: normal; }");
        Page.getCurrent().getStyles().add(".alerts { padding: 5px 5px; margin: 1px; }");
    }

    /**
     * Returns the component for visualising the alarms data.
     *
     * @param onmsAlarm an {@link OnmsAlarm} instance
     * @param onmsNode  an {@link OnmsNode} instance
     * @return component for this alarm
     */
    public Component createAlarmComponent(OnmsAlarm onmsAlarm, OnmsNode onmsNode) {

        Calendar calendar = Calendar.getInstance();

        String ago = getHumanReadableFormat((calendar.getTimeInMillis() / 1000) - (onmsAlarm.getLastEventTime().getTime() / 1000));

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        horizontalLayout.addStyleName("alerts");
        horizontalLayout.addStyleName(onmsAlarm.getSeverity().name().toLowerCase());

        Label labelAgo = new Label();
        labelAgo.setSizeUndefined();
        labelAgo.addStyleName("alerts-font");
        labelAgo.setValue(ago);

        Label labelId = new Label();
        labelId.setSizeUndefined();
        labelId.addStyleName("alerts-font");
        if (onmsNode != null) {
            labelId.setValue(onmsNode.getLabel() + " (" + onmsNode.getNodeId() + ")");
        } else {
            labelId.setValue("-");
        }

        Label labelUei = new Label();
        labelUei.setSizeUndefined();
        labelUei.addStyleName("alerts-font");
        labelUei.setValue(onmsAlarm.getUei());

        horizontalLayout.addComponent(labelAgo);
        horizontalLayout.addComponent(labelId);
        horizontalLayout.addComponent(labelUei);

        horizontalLayout.setExpandRatio(labelAgo, 1.0f);
        horizontalLayout.setExpandRatio(labelId, 3.0f);
        horizontalLayout.setExpandRatio(labelUei, 3.0f);

        return horizontalLayout;
    }

    @Override
    public boolean isBoosted() {
        return boosted;
    }
}

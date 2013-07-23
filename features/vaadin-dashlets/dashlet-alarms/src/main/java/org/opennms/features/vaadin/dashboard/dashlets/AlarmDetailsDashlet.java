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
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch;
import org.opennms.features.vaadin.dashboard.config.ui.editors.CriteriaBuilderHelper;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.*;

import java.util.List;

/**
 * This class represents a Alert Dashlet with some details.
 *
 * @author Christian Pape
 */
public class AlarmDetailsDashlet extends VerticalLayout implements Dashlet {
    /**
     * the dashlet's name
     */
    private String m_name;
    /**
     * The {@link AlarmDao} used
     */
    private AlarmDao m_alarmDao;
    /**
     * The {@link NodeDao} used
     */
    private NodeDao m_nodeDao;
    /**
     * The {@link DashletSpec} for this instance
     */
    private DashletSpec m_dashletSpec;
    /**
     * boosted value
     */
    private boolean boosted = false;
    /**
     * Helper for handling criterias
     */
    private CriteriaBuilderHelper m_criteriaBuilderHelper = new CriteriaBuilderHelper(OnmsAlarm.class, OnmsNode.class, OnmsCategory.class, OnmsEvent.class);

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     * @param alarmDao    the {@link AlarmDao} to be used
     * @param nodeDao     the {@link NodeDao} to be used
     */
    public AlarmDetailsDashlet(String name, DashletSpec dashletSpec, AlarmDao alarmDao, NodeDao nodeDao) {
        /**
         * Setting the member fields
         */
        m_name = name;
        m_dashletSpec = dashletSpec;
        m_alarmDao = alarmDao;
        m_nodeDao = nodeDao;

        /**
         * Setting up the layout
         */
        setCaption(getName());
        setWidth("100%");
    }

    /**
     * Updates the alarm data using the associated {@link AlarmDao} and {@link NodeDao} instances.
     */
    @Override
    public void update() {
        final CriteriaBuilder alarmCb = new CriteriaBuilder(OnmsAlarm.class);

        alarmCb.alias("node", "node");
        alarmCb.alias("node.categories", "category");
        alarmCb.alias("lastEvent", "event");

        String criteria = m_dashletSpec.getParameters().get("criteria");
        OnmsSeverity boostSeverity = OnmsSeverity.valueOf(m_dashletSpec.getParameters().get("boostSeverity"));

        m_criteriaBuilderHelper.parseConfiguration(alarmCb, criteria);

        alarmCb.fetch("firstEvent", Fetch.FetchType.EAGER);
        alarmCb.fetch("lastEvent", Fetch.FetchType.EAGER);

        alarmCb.distinct();

        List<OnmsAlarm> alarms = m_alarmDao.findMatching(alarmCb.toCriteria());
        removeAllComponents();

        injectStyles();

        boosted = false;

        if (alarms.size() == 0) {
            Label label = new Label("No alarms found!");
            label.addStyleName("alert-details-noalarms-font");
            addComponent(label);
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
                addComponent(createAlarmComponent(onmsAlarm, onmsNode));

                if (onmsAlarm.getSeverity().isGreaterThanOrEqual(boostSeverity)) {
                    boosted = true;
                }
            }
        }
    }

    /**
     * Injects CSS styles on current page for this dashlet
     */
    private void injectStyles() {
        Page.getCurrent().getStyles().add(".alert-details.cleared { background: #AAAAAA; border-left: 15px solid #858585; }");
        Page.getCurrent().getStyles().add(".alert-details.normal { background: #AAAAAA; border-left: 15px solid #336600; }");
        Page.getCurrent().getStyles().add(".alert-details.indeterminate { background: #AAAAAA; border-left: 15px solid #999; }");
        Page.getCurrent().getStyles().add(".alert-details.warning { background: #AAAAAA; border-left: 15px solid #FFCC00; }");
        Page.getCurrent().getStyles().add(".alert-details.minor { background: #AAAAAA; border-left: 15px solid #FF9900; }");
        Page.getCurrent().getStyles().add(".alert-details.major { background: #AAAAAA; border-left: 15px solid #FF3300; }");
        Page.getCurrent().getStyles().add(".alert-details.critical { background: #AAAAAA; border-left: 15px solid #CC0000; }");
        Page.getCurrent().getStyles().add(".alert-details-font {color: #000000; font-size: 18px; line-height: normal; }");
        Page.getCurrent().getStyles().add(".alert-details-noalarms-font { font-size: 18px; line-height: normal; }");
        Page.getCurrent().getStyles().add(".alert-details { padding: 5px 5px; margin: 1px; }");
    }

    /**
     * Returns the component for visualising the alarms data.
     *
     * @param onmsAlarm an {@link OnmsAlarm} instance
     * @param onmsNode  an {@link OnmsNode} instance
     * @return component for this alarm
     */
    public Component createAlarmComponent(OnmsAlarm onmsAlarm, OnmsNode onmsNode) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSizeFull();
        horizontalLayout.addStyleName("alert-details");
        horizontalLayout.addStyleName("alert-details-font");
        horizontalLayout.addStyleName(onmsAlarm.getSeverity().name().toLowerCase());

        VerticalLayout verticalLayout1 = new VerticalLayout();
        Label lastEvent = new Label();
        lastEvent.addStyleName("alert-details-font");
        lastEvent.setCaption("Last event");
        lastEvent.setValue(onmsAlarm.getLastEventTime().toString());

        Label firstEvent = new Label();
        firstEvent.setSizeUndefined();
        firstEvent.addStyleName("alert-details-font");
        firstEvent.setCaption("First event");
        firstEvent.setValue(onmsAlarm.getFirstEventTime().toString());

        verticalLayout1.addComponent(firstEvent);
        verticalLayout1.addComponent(lastEvent);

        horizontalLayout.addComponent(verticalLayout1);

        VerticalLayout verticalLayout2 = new VerticalLayout();

        Label nodeId = new Label();
        nodeId.setSizeUndefined();
        nodeId.addStyleName("alert-details-font");
        nodeId.setCaption("Node Id");

        if (onmsNode != null) {
            nodeId.setValue(onmsNode.getNodeId());
        } else {
            nodeId.setValue("-");
        }

        Label nodeLabel = new Label();
        nodeLabel.setSizeUndefined();
        nodeLabel.addStyleName("alert-details-font");
        nodeLabel.setCaption("Node Label");
        if (onmsNode != null) {
            nodeLabel.setValue(onmsNode.getLabel());
        } else {
            nodeLabel.setValue("-");
        }

        verticalLayout2.addComponent(nodeId);
        verticalLayout2.addComponent(nodeLabel);

        horizontalLayout.addComponent(verticalLayout2);

        Label logMessage = new Label();
        logMessage.addStyleName("alert-details-font");
        logMessage.setSizeFull();
        logMessage.setCaption("Log message");
        logMessage.setValue(onmsAlarm.getLogMsg().replaceAll("<[^>]*>", ""));

        horizontalLayout.addComponent(logMessage);
        horizontalLayout.setExpandRatio(verticalLayout1, 1.0f);
        horizontalLayout.setExpandRatio(verticalLayout2, 1.0f);
        horizontalLayout.setExpandRatio(logMessage, 3.0f);

        return horizontalLayout;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public boolean isBoosted() {
        return boosted;
    }
}

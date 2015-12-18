/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.dashboard.dashlets;

import com.google.common.collect.Lists;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch;
import org.opennms.features.vaadin.dashboard.config.ui.editors.CriteriaBuilderHelper;
import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private DashletComponent m_wallboardComponent = null;

    /**
     * dashboard layout
     */
    private DashletComponent m_dashboardComponent = null;


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
    public DashletComponent getWallboardComponent() {
        if (m_wallboardComponent == null) {
            m_wallboardComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setWidth("100%");
                    injectWallboardStyles();
                    refresh();
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
                    Page.getCurrent().getStyles().add(".v-slot-alerts-font { overflow: hidden; }");
                }

                @Override
                public void refresh() {
                    List<OnmsAlarm> alarms = getAlarms();

                    OnmsSeverity boostSeverity = OnmsSeverity.valueOf(getDashletSpec().getParameters().get("boostSeverity"));

                    m_verticalLayout.removeAllComponents();

                    boosted = false;

                    addComponents(m_verticalLayout, alarms);

                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }
        return m_wallboardComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        if (m_dashboardComponent == null) {
            m_dashboardComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setWidth("100%");
                    injectDashboardStyles();
                    refresh();
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
                    Page.getCurrent().getStyles().add(".v-slot-alerts-font { overflow: hidden; }");
                }

                @Override
                public void refresh() {
                    List<OnmsAlarm> alarms = getAlarms();

                    m_verticalLayout.removeAllComponents();

                    boosted = false;

                    addComponents(m_verticalLayout, alarms);
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }
        return m_dashboardComponent;
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

        /**
         * due to restrictions in the criteria api it's quite hard
         * to use distinct and orderBy together, so I apply a workaround
         * to avoid alarmCb.distinct();
         */

        List<OnmsAlarm> onmsAlarmList = m_alarmDao.findMatching(alarmCb.toCriteria());
        Map<Integer, OnmsAlarm> onmsAlarmMap = new LinkedHashMap<>();

        for (OnmsAlarm onmsAlarm : onmsAlarmList) {
            if (!onmsAlarmMap.containsKey(onmsAlarm.getId())) {
                onmsAlarmMap.put(onmsAlarm.getId(), onmsAlarm);
            }
        }

        return Lists.newArrayList(onmsAlarmMap.values());
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

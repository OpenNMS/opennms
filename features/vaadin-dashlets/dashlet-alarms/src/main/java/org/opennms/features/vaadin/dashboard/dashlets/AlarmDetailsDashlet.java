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

import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.features.topology.plugins.browsers.*;
import org.opennms.features.vaadin.dashboard.config.ui.editors.CriteriaBuilderHelper;
import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.*;
import org.opennms.osgi.EventProxy;
import org.opennms.osgi.VaadinApplicationContextImpl;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a Alert Dashlet with some details.
 *
 * @author Christian Pape
 */
public class AlarmDetailsDashlet extends AbstractDashlet {
    /**
     * The {@link AlarmDao} used
     */
    private AlarmDao m_alarmDao;
    /**
     * The {@link NodeDao} used
     */
    private NodeDao m_nodeDao;
    /**
     * Helper for handling criterias
     */
    private CriteriaBuilderHelper m_criteriaBuilderHelper = new CriteriaBuilderHelper(OnmsAlarm.class, OnmsNode.class, OnmsCategory.class, OnmsEvent.class);
    /**
     * wallboard layout
     */
    DashletComponent m_wallboardComponent;
    /**
     * dashboard layout
     */
    DashletComponent m_dashboardComponent;
    /**
     * alarm table
     */
    AlarmRepository m_alarmRepository;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     * @param alarmDao    the {@link AlarmDao} to be used
     * @param nodeDao     the {@link NodeDao} to be used
     */
    public AlarmDetailsDashlet(String name, DashletSpec dashletSpec, AlarmDao alarmDao, NodeDao nodeDao, AlarmRepository alarmRepository) {
        super(name, dashletSpec);

        /**
         * Setting the member fields
         */
        m_alarmDao = alarmDao;
        m_nodeDao = nodeDao;
        m_alarmRepository = alarmRepository;
    }

    @Override
    public DashletComponent getWallboardComponent() {

        if (m_wallboardComponent == null) {
            m_wallboardComponent = new AbstractDashletComponent() {

                private VerticalLayout m_verticalLayout;

                {
                    m_verticalLayout = new VerticalLayout();
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setWidth("100%");

                    refresh();
                }

                /**
                 * Injects CSS styles on current page for this dashlet
                 */
                private void injectWallboardStyles() {
                    Page.getCurrent().getStyles().add(".alert-details.cleared { background: #AAAAAA; border-left: 14px solid #858585; }");
                    Page.getCurrent().getStyles().add(".alert-details.normal { background: #AAAAAA; border-left: 14px solid #336600; }");
                    Page.getCurrent().getStyles().add(".alert-details.indeterminate { background: #AAAAAA; border-left: 14px solid #999; }");
                    Page.getCurrent().getStyles().add(".alert-details.warning { background: #AAAAAA; border-left: 14px solid #FFCC00; }");
                    Page.getCurrent().getStyles().add(".alert-details.minor { background: #AAAAAA; border-left: 14px solid #FF9900; }");
                    Page.getCurrent().getStyles().add(".alert-details.major { background: #AAAAAA; border-left: 14px solid #FF3300; }");
                    Page.getCurrent().getStyles().add(".alert-details.critical { background: #AAAAAA; border-left: 14px solid #CC0000; }");
                    Page.getCurrent().getStyles().add(".alert-details-font {color: #000000; font-size: 17px; line-height: normal; }");
                    Page.getCurrent().getStyles().add(".alert-details-noalarms-font { font-size: 17px; line-height: normal; }");
                    Page.getCurrent().getStyles().add(".alert-details { padding: 5px 5px; margin: 1px; }");
                }

                @Override
                public void refresh() {
                    List<OnmsAlarm> alarms = getAlarms();
                    m_verticalLayout.removeAllComponents();
                    injectWallboardStyles();

                    setBoosted(false);

                    addComponents(m_verticalLayout, alarms);

                    setBoosted(checkBoosted(alarms));
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

                private AlarmTable m_alarmTable;

                {
                    m_alarmTable = new AlarmTable("Alarms", new AlarmDaoContainer(m_alarmDao), m_alarmRepository);

                    m_alarmTable.setSizeFull();

                    m_alarmTable.setSortEnabled(false);

                    m_alarmTable.addHeaderClickListener(new Table.HeaderClickListener() {
                        @Override
                        public void headerClick(Table.HeaderClickEvent headerClickEvent) {
                            m_alarmTable.setSortContainerPropertyId(headerClickEvent.getPropertyId());
                            m_alarmTable.setSortEnabled(true);
                        }
                    });

                    final VaadinApplicationContextImpl context = new VaadinApplicationContextImpl();
                    final UI currentUI = UI.getCurrent();

                    context.setSessionId(currentUI.getSession().getSession().getId());
                    context.setUiId(currentUI.getUIId());

                    m_alarmTable.setVaadinApplicationContext(context);

                    final EventProxy eventProxy = new EventProxy() {
                        @Override
                        public <T> void fireEvent(final T eventObject) {
                            System.out.println("got event: {}" + eventObject);
                        }

                        @Override
                        public <T> void addPossibleEventConsumer(final T possibleEventConsumer) {
                            System.out.println("(ignoring) add consumer: {}" + possibleEventConsumer);
                        }
                    };

                    m_alarmTable.setEventProxy(eventProxy);

                    m_alarmTable.setColumnReorderingAllowed(true);
                    m_alarmTable.setColumnCollapsingAllowed(true);
                    m_alarmTable.setSortContainerPropertyId("id");
                    m_alarmTable.setSortAscending(false);
                    m_alarmTable.setCellStyleGenerator(new AlarmTableCellStyleGenerator());

                    m_alarmTable.addGeneratedColumn("severity", new SeverityGenerator());
                    m_alarmTable.addGeneratedColumn("id", new AlarmIdColumnLinkGenerator(m_alarmDao, "id"));
                    m_alarmTable.setVisibleColumns("id", "severity", "nodeLabel", "counter", "lastEventTime", "logMsg");
                    m_alarmTable.setColumnHeaders("ID", "Severity", "Node", "Count", "Last Event Time", "Log Message");

                    refresh();
                }


                @Override
                public void refresh() {
                    List<OnmsAlarm> alarms = getAlarms();

                    if (alarms.size()>0) {
                        List<Integer> alarmIds = new LinkedList<Integer>();

                        for (OnmsAlarm onmsAlarm : alarms) {
                            alarmIds.add(onmsAlarm.getId());
                        }

                        List<Restriction> restrictions = new LinkedList<Restriction>();
                        restrictions.add(new InRestriction("id", alarmIds));

                        ((OnmsDaoContainer<?,?>) m_alarmTable.getContainerDataSource()).setRestrictions(restrictions);
                    }

                    setBoosted(checkBoosted(alarms));

                    m_alarmTable.markAsDirtyRecursive();
                }

                @Override
                public Component getComponent() {
                    return m_alarmTable;
                }
            };
        }
        return m_dashboardComponent;
    }

    private boolean checkBoosted(List<OnmsAlarm> alarms) {
        for (OnmsAlarm onmsAlarm : alarms) {
            OnmsSeverity boostSeverity = OnmsSeverity.valueOf(getDashletSpec().getParameters().get("boostSeverity"));

            if (onmsAlarm.getSeverity().isGreaterThanOrEqual(boostSeverity)) {
                return true;
            }
        }
        return false;
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
     * Adds the alarms components to a {@link AbstractOrderedLayout}
     *
     * @param component the component to add alarms to
     * @param alarms    the alarms list
     */
    private void addComponents(AbstractOrderedLayout component, List<OnmsAlarm> alarms) {
        if (alarms.size() == 0) {
            Label label = new Label("No alarms found!");
            label.addStyleName("alert-details-noalarms-font");
            component.addComponent(label);
        } else {
            StringBuilder sb = new StringBuilder();

            sb.append("<table class='alert-details-dashlet onms-table'>");
            sb.append("<thead>");
            sb.append("<th class='alert-details-dashlet onms-header-cell'>ID</th><th class='alert-details-dashlet onms-header-cell'>Severity</th><th class='alert-details-dashlet onms-header-cell'>Node</th><th class='alert-details-dashlet onms-header-cell'>Count</th><th class='alert-details-dashlet onms-header-cell'>Last Event Time</th><th class='alert-details-dashlet onms-header-cell'>Log Msg</th>");
            sb.append("</thead>");

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

                sb.append("<tr class='alert-details-dashlet " + onmsAlarm.getSeverity().getLabel() + "'>");
                sb.append("<td class='alert-details-dashlet onms-cell divider bright onms' valign='middle' rowspan='1'><nobr>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + onmsAlarm.getId() + "</nobr></td>");
                sb.append("<td class='alert-details-dashlet onms-cell divider onms' valign='middle' rowspan='1'><nobr>" + onmsAlarm.getSeverity().getLabel() + "</nobr></td>");
                sb.append("<td class='alert-details-dashlet onms-cell divider onms' valign='middle' rowspan='1'><nobr>" + (onmsNode != null ? onmsNode.getLabel() : "-") + "</nobr></td>");
                sb.append("<td class='alert-details-dashlet onms-cell divider onms' valign='middle' rowspan='1'><nobr>" + onmsAlarm.getCounter() + "</nobr></td>");
                sb.append("<td class='alert-details-dashlet onms-cell divider onms' valign='middle' rowspan='1'><nobr>" + onmsAlarm.getLastEventTime().toString() + "</nobr></td>");
                sb.append("<td class='alert-details-dashlet onms-cell divider onms' valign='middle' rowspan='1'>" + onmsAlarm.getLogMsg().replaceAll("\\<.*?>", "") + "</td>");
                sb.append("</td></tr>");
            }
            sb.append("</table>");
            Label label = new Label(sb.toString());
            label.setSizeFull();
            label.setContentMode(ContentMode.HTML);
            component.addComponent(label);
        }
    }

    /**
     * Returns the component for visualising the alarms data.
     *
     * @param onmsAlarm an {@link OnmsAlarm} instance
     * @param onmsNode  an {@link OnmsNode} instance
     * @return component for this alarm
     */
    @Deprecated
    public Component createAlarmComponent(OnmsAlarm onmsAlarm, OnmsNode onmsNode) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSizeFull();
        horizontalLayout.addStyleName("alert-details");
        horizontalLayout.addStyleName("alert-details-font");
        horizontalLayout.addStyleName(onmsAlarm.getSeverity().name().toLowerCase());

        VerticalLayout verticalLayout1 = new VerticalLayout();
        VerticalLayout verticalLayout2 = new VerticalLayout();

        horizontalLayout.addComponent(verticalLayout1);
        horizontalLayout.addComponent(verticalLayout2);

        Label lastEvent = new Label();
        lastEvent.setSizeUndefined();
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

        Label logMessage = new Label();
        logMessage.addStyleName("alert-details-font");
        logMessage.setSizeFull();
        logMessage.setCaption("Log message");
        logMessage.setValue(onmsAlarm.getLogMsg().replaceAll("<[^>]*>", ""));

        HorizontalLayout horizontalLayout2 = new HorizontalLayout();
        horizontalLayout2.setSizeFull();
        horizontalLayout2.setSpacing(true);
        horizontalLayout2.addComponent(nodeId);
        horizontalLayout2.addComponent(nodeLabel);

        verticalLayout2.addComponent(horizontalLayout2);
        verticalLayout2.addComponent(logMessage);

        horizontalLayout.setExpandRatio(verticalLayout1, 1.0f);
        horizontalLayout.setExpandRatio(verticalLayout2, 4.0f);

        return horizontalLayout;
    }
}

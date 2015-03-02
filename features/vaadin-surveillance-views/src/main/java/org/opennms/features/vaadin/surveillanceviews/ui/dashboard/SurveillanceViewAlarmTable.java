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
package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;
import org.opennms.features.topology.api.support.InfoWindow;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * This class represents a table displaying the OpenNMS alarms for given row/column categories.
 *
 * @author Christian Pape
 */
public class SurveillanceViewAlarmTable extends SurveillanceViewDetailTable {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewAlarmTable.class);
    /**
     * the bean container storing the alarm instances
     */
    private BeanItemContainer<OnmsAlarm> m_beanItemContainer = new BeanItemContainer<OnmsAlarm>(OnmsAlarm.class);

    /**
     * Constructor for instantiating this component.
     *
     * @param surveillanceViewService the surveillance view service to be used
     * @param enabled                 the flag should links be enabled?
     */
    public SurveillanceViewAlarmTable(SurveillanceViewService surveillanceViewService, boolean enabled) {
        /**
         * calling the super constructor
         */
        super("Alarms", surveillanceViewService, enabled);

        /**
         * set the datasource
         */
        setContainerDataSource(m_beanItemContainer);

        /**
         * the base stylename
         */
        addStyleName("surveillance-view");

        /**
         * add node column
         */
        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(final Table table, final Object itemId, final Object propertyId) {
                final OnmsAlarm onmsAlarm = (OnmsAlarm) itemId;

                Button icon = getClickableIcon("glyphicon glyphicon-warning-sign", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        OnmsAlarm alarm = (OnmsAlarm) itemId;
                        final int alarmId = alarm.getId();

                        final URI currentLocation = Page.getCurrent().getLocation();
                        final String contextRoot = VaadinServlet.getCurrent().getServletContext().getContextPath();
                        final String redirectFragment = contextRoot + "/alarm/detail.htm?quiet=true&id=" + alarmId;

                        LOG.debug("alarm {} clicked, current location = {}, uri = {}", alarmId, currentLocation, redirectFragment);

                        try {
                            SurveillanceViewAlarmTable.this.getUI().addWindow(new InfoWindow(new URL(currentLocation.toURL(), redirectFragment), new InfoWindow.LabelCreator() {
                                @Override
                                public String getLabel() {
                                    return "Alarm Info " + alarmId;
                                }
                            }));
                        } catch (MalformedURLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                });

                Button button = new Button(onmsAlarm.getNodeLabel());
                button.setPrimaryStyleName(BaseTheme.BUTTON_LINK);
                button.setEnabled(m_enabled);

                button.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {

                        final int nodeId = onmsAlarm.getNodeId();

                        final URI currentLocation = Page.getCurrent().getLocation();
                        final String contextRoot = VaadinServlet.getCurrent().getServletContext().getContextPath();
                        final String redirectFragment = contextRoot + "/element/node.jsp?quiet=true&node=" + nodeId;

                        LOG.debug("node {} clicked, current location = {}, uri = {}", nodeId, currentLocation, redirectFragment);

                        try {
                            SurveillanceViewAlarmTable.this.getUI().addWindow(new InfoWindow(new URL(currentLocation.toURL(), redirectFragment), new InfoWindow.LabelCreator() {
                                @Override
                                public String getLabel() {
                                    return "Node Info " + nodeId;
                                }
                            }));
                        } catch (MalformedURLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                });

                HorizontalLayout horizontalLayout = new HorizontalLayout();

                horizontalLayout.addComponent(icon);
                horizontalLayout.addComponent(button);

                horizontalLayout.setSpacing(true);

                return horizontalLayout;
            }
        });

        /**
         * add logMsg column
         */
        addGeneratedColumn("logMsg", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object itemId, Object propertyId) {
                OnmsAlarm onmsAlarm = (OnmsAlarm) itemId;
                return getImageSeverityLayout(onmsAlarm.getLogMsg());
            }
        });

        /**
         * set a cell style generator that handles the logMsg column
         */
        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(final Table source, final Object itemId, final Object propertyId) {
                OnmsAlarm onmsAlarm = ((OnmsAlarm) itemId);

                String style = onmsAlarm.getSeverity().getLabel().toLowerCase();

                if ("logMsg".equals(propertyId)) {
                    style += "-image";
                }

                return style;
            }
        });

        /**
         * set column headers
         */
        setColumnHeader("node", "Node");
        setColumnHeader("logMsg", "Log Msg");
        setColumnHeader("counter", "Count");
        setColumnHeader("firstEventTime", "First Time");
        setColumnHeader("lastEventTime", "Last Time");

        /**
         * set visible columns
         */
        setVisibleColumns("node", "logMsg", "counter", "firstEventTime", "lastEventTime");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories) {
        /**
         * retrieve all matching alarms
         */
        List<OnmsAlarm> alarms = getSurveillanceViewService().getAlarmsForCategories(rowCategories, colCategories);

        /**
         * empty the container
         */
        m_beanItemContainer.removeAllItems();

        /**
         * add items to container
         */
        if (alarms != null && !alarms.isEmpty()) {
            for (OnmsAlarm alarm : alarms) {
                m_beanItemContainer.addItem(alarm);
            }
        }
        /**
         * sort the alarms
         */
        sort(new Object[]{"firstEventTime"}, new boolean[]{true});

        /**
         * refresh the table
         */
        refreshRowCache();
    }
}

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

public class SurveillanceViewAlarmTable extends SurveillanceViewDetailTable {
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewAlarmTable.class);

    private BeanItemContainer<OnmsAlarm> m_beanItemContainer = new BeanItemContainer<OnmsAlarm>(OnmsAlarm.class);

    public SurveillanceViewAlarmTable(SurveillanceViewService surveillanceViewService, boolean enabled) {
        super("Alarms", surveillanceViewService, enabled);

        setContainerDataSource(m_beanItemContainer);

        addStyleName("surveillance-view");

        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object propertyId) {
                final OnmsAlarm onmsAlarm = (OnmsAlarm) itemId;

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

                return button;
            }
        });

        addGeneratedColumn("logMsg", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object itemId, Object propertyId) {
                OnmsAlarm onmsAlarm = (OnmsAlarm) itemId;
                return getImageSeverityLayout(onmsAlarm.getLogMsg());
            }
        });

        addGeneratedColumn("icon", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object propertyId) {
                return getClickableIcon("glyphicon glyphicon-warning-sign", new Button.ClickListener() {
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
            }
        });

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

        setColumnHeader("icon", "");
        setColumnHeader("node", "Node");
        setColumnHeader("logMsg", "Log Msg");
        setColumnHeader("counter", "Count");
        setColumnHeader("firstEventTime", "First Time");
        setColumnHeader("lastEventTime", "Last Time");

        setVisibleColumns("icon", "node", "logMsg", "counter", "firstEventTime", "lastEventTime");
    }

    @Override
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories) {
        List<OnmsAlarm> alarms = getSurveillanceViewService().getAlarmsForCategories(rowCategories, colCategories);

        m_beanItemContainer.removeAllItems();

        if (alarms != null & !alarms.isEmpty()) {
            for (OnmsAlarm alarm : alarms) {
                m_beanItemContainer.addItem(alarm);
            }
        }
        sort(new Object[]{"firstEventTime"}, new boolean[]{true});

        refreshRowCache();
    }
}

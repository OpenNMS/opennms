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
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;

import java.util.List;
import java.util.Set;

public class SurveillanceViewAlarmTable extends SurveillanceViewDetailTable {

    private BeanItemContainer<OnmsAlarm> m_beanItemContainer = new BeanItemContainer<OnmsAlarm>(OnmsAlarm.class);

    public SurveillanceViewAlarmTable(SurveillanceViewService surveillanceViewService) {
        super("Alarms", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);

        addStyleName("surveillance-view");

        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object itemId, Object propertyId) {
                OnmsAlarm onmsAlarm = (OnmsAlarm) itemId;
                Link link = new Link(onmsAlarm.getNodeLabel(), new ExternalResource("/opennms/element/node.jsp?node=" + onmsAlarm.getNodeId()));
                link.setPrimaryStyleName("surveillance-view");
                return link;
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

        setColumnHeader("node", "Node");
        setColumnHeader("logMsg", "Log Msg");
        setColumnHeader("counter", "Count");
        setColumnHeader("firstEventTime", "First Time");
        setColumnHeader("lastEventTime", "Last Time");

        setVisibleColumns("node", "logMsg", "counter", "firstEventTime", "lastEventTime");
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

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
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNotification;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SurveillanceViewNotificationTable extends SurveillanceViewDetailTable {
    private BeanItemContainer<OnmsNotification> m_beanItemContainer = new BeanItemContainer<OnmsNotification>(OnmsNotification.class);
    private HashMap<OnmsNotification, String> m_customSeverity = new HashMap<>();

    public SurveillanceViewNotificationTable(SurveillanceViewService surveillanceViewService) {
        super("Notifications", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);

        addStyleName("surveillance-view");

        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object itemId, Object propertyId) {
                OnmsNotification onmsNotification = (OnmsNotification) itemId;
                Link link = new Link(onmsNotification.getNodeLabel(), new ExternalResource("/opennms/element/node.jsp?node=" + onmsNotification.getNodeId()));
                link.setTargetName("_top");
                link.setPrimaryStyleName("surveillance-view");
                return link;
            }
        });

        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(final Table source, final Object itemId, final Object propertyId) {
                OnmsNotification onmsNotification = ((OnmsNotification) itemId);
                return m_customSeverity.get(onmsNotification).toLowerCase();
            }
        });

        setColumnHeader("node", "Node");
        setColumnHeader("serviceType", "Service");
        setColumnHeader("textMsg", "Message");
        setColumnHeader("pageTime", "Sent Time");
        setColumnHeader("answeredBy", "Responder");
        setColumnHeader("respondTime", "Respond Time");

        setVisibleColumns("node", "serviceType", "textMsg", "pageTime", "answeredBy", "respondTime");
    }

    @Override
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories) {
        List<OnmsNotification> notifications = getSurveillanceViewService().getNotificationsForCategories(rowCategories, colCategories, m_customSeverity);

        m_beanItemContainer.removeAllItems();

        if (notifications != null && !notifications.isEmpty()) {
            for (OnmsNotification onmsNotification : notifications) {
                m_beanItemContainer.addItem(onmsNotification);
            }
        }

        sort(new Object[]{"pageTime"}, new boolean[]{false});

        refreshRowCache();
    }
}

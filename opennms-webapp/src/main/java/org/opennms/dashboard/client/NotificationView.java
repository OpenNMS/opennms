/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.FlexTable;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
class NotificationView extends PageableTableView {
    
    private Notification[] m_notifications;
    
    NotificationView(Dashlet dashlet) {
        super(dashlet, 8, new String[] { "Node", "Service", "Message", "Sent Time", "Responder", "Response Time" });
    }

    public void setNotifications(Notification[] notifications) {
        m_notifications = notifications;
        refresh();
        
    }
    
	protected void setRow(FlexTable table, int row, int elementIndex) {
		Notification notif = m_notifications[elementIndex];
		if (notif.getIsDashboardRole()) {
            table.setText(row, 0, notif.getNodeLabel());
		} else {
            table.setHTML(row, 0, "<a href=\"element/node.jsp?node=" + notif.getNodeId() + "\">" + notif.getNodeLabel() + "</a>");
		}
        table.setText(row, 1, notif.getServiceName());
        table.setText(row, 2, notif.getTextMessage());
        table.setText(row, 3, ""+notif.getSentTime());
        table.setText(row, 4, notif.getResponder());
        table.setText(row, 5, (notif.getRespondTime() != null) ? notif.getRespondTime().toString() : "");
        table.getRowFormatter().setStyleName(row, notif.getSeverity());
    }
    
    public int getElementCount() {
        return (m_notifications == null ? 0 : m_notifications.length);
    }
    
}
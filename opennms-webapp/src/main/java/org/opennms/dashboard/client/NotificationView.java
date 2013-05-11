/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

    /**
     * <p>setNotifications</p>
     *
     * @param notifications an array of {@link org.opennms.dashboard.client.Notification} objects.
     */
    public void setNotifications(Notification[] notifications) {
        m_notifications = notifications;
        refresh();
        
    }
    
	/** {@inheritDoc} */
    @Override
	protected void setRow(FlexTable table, int row, int elementIndex) {
		Notification notif = m_notifications[elementIndex];
		if (notif.getIsDashboardRole()) {
            table.setText(row, 0, notif.getNodeLabel());
		} else {
            table.setHTML(row, 0, "<a href=\"element/node.jsp?node=" + notif.getNodeId() + "\">" + notif.getNodeLabel() + "</a>");
		}
        table.setText(row, 1, notif.getServiceName());
        table.setHTML(row, 2, notif.getTextMessage());
        table.setText(row, 3, ""+notif.getSentTime());
        table.setText(row, 4, notif.getResponder());
        table.setText(row, 5, (notif.getRespondTime() != null) ? notif.getRespondTime().toString() : "");
        table.getRowFormatter().setStyleName(row, notif.getSeverity());
    }
    
    /**
     * <p>getElementCount</p>
     *
     * @return a int.
     */
    @Override
    public int getElementCount() {
        return (m_notifications == null ? 0 : m_notifications.length);
    }
    
}

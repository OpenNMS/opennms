/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Feb 09: Add node links for users NOT in dashboard role. ayres@opennms.org
 * Created: February 20, 2007
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

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
    public int getElementCount() {
        return (m_notifications == null ? 0 : m_notifications.length);
    }
    
}

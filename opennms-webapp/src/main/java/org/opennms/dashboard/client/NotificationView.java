/**
 * 
 */
package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;


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
        table.setText(row, 0, notif.getNodeLabel());
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
/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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
import com.google.gwt.user.client.ui.Label;


/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
class AlarmView extends PageableTableView {
    
    private Alarm[] m_alarms;
    
    AlarmView(Dashlet dashlet) {
		super(dashlet, 5, new String[] { "Node", "Description", "Count", "First Time", "Last Time" });
	}
    
    public void setAlarms(Alarm[] alarms) {
        m_alarms = alarms;
        refresh();
        
    }
    
    protected void setRow(FlexTable table, int row, int elementIndex) {
    	Alarm alarm = m_alarms[elementIndex];
    	if (alarm.getIsDashboardRole()) {
            table.setText(row, 0, alarm.getNodeLabel());
    	} else {
            table.setHTML(row, 0, "<a href=\"element/node.jsp?node=" + alarm.getNodeId() + "\">" + alarm.getNodeLabel() + "</a>");
    	}
        Label label = new Label(alarm.getLogMsg());
        label.setTitle(alarm.getDescrption());
        table.setWidget(row, 1, label);
        table.setText(row, 2, ""+alarm.getCount());
        table.setText(row, 3, alarm.getFirstEventTime().toString());
        table.setText(row, 4, alarm.getLastEventTime().toString());
        table.getRowFormatter().setStyleName(row, alarm.getSeverity());
    }
    
    public int getElementCount() {
        return (m_alarms == null ? 0 : m_alarms.length);
    }

	protected void formatCells(FlexTable table, int row) {
		super.formatCells(table, row);
	    table.getCellFormatter().addStyleName(row, 1, "bright");
	}
    
}
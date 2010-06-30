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
 * Created: March 2, 2007
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
    
    /**
     * <p>setAlarms</p>
     *
     * @param alarms an array of {@link org.opennms.dashboard.client.Alarm} objects.
     */
    public void setAlarms(Alarm[] alarms) {
        m_alarms = alarms;
        refresh();
        
    }
    
    /** {@inheritDoc} */
    protected void setRow(FlexTable table, int row, int elementIndex) {
    	Alarm alarm = m_alarms[elementIndex];
        table.setText(row, 0, alarm.getNodeLabel());
        Label label = new Label(alarm.getLogMsg());
        label.setTitle(alarm.getDescrption());
        table.setWidget(row, 1, label);
        table.setText(row, 2, ""+alarm.getCount());
        table.setText(row, 3, alarm.getFirstEventTime().toString());
        table.setText(row, 4, alarm.getLastEventTime().toString());
        table.getRowFormatter().setStyleName(row, alarm.getSeverity());
    }
    
    /**
     * <p>getElementCount</p>
     *
     * @return a int.
     */
    public int getElementCount() {
        return (m_alarms == null ? 0 : m_alarms.length);
    }

	/** {@inheritDoc} */
	protected void formatCells(FlexTable table, int row) {
		super.formatCells(table, row);
	    table.getCellFormatter().addStyleName(row, 1, "bright");
	}
    
}

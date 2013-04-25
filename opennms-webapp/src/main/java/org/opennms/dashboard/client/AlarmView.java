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

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;


/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
class AlarmView extends PageableTableView {
    
    private Alarm[] m_alarms;
    private RegExp m_regex = RegExp.compile("<(.|\n)*?>", "g");
    
    AlarmView(Dashlet dashlet) {
		super(dashlet, 5, new String[] { "Node", "Log Msg", "Count", "First Time", "Last Time" });
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
    	if (alarm.getIsDashboardRole()) {
            table.setText(row, 0, alarm.getNodeLabel());
    	} else {
            table.setHTML(row, 0, "<a href=\"element/node.jsp?node=" + alarm.getNodeId() + "\">" + alarm.getNodeLabel() + "</a>");
    	}
    	
        HTML label = new HTML();
        label.setTitle(stripHtmlTags(alarm.getDescrption()));
        label.setHTML(SafeHtmlUtils.fromTrustedString(alarm.getLogMsg()));
        table.setWidget(row, 1, label);
        table.setText(row, 2, ""+alarm.getCount());
        table.setText(row, 3, alarm.getFirstEventTime().toString());
        table.setText(row, 4, alarm.getLastEventTime().toString());
        table.getRowFormatter().setStyleName(row, alarm.getSeverity());
    }
    
    private String stripHtmlTags(String description) {
        return m_regex.replace(description, "");
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

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
 *
 * Modifications:
 * 
 * 2009 Feb 09: Add node links for users NOT in dashboard role. ayres@opennms.org
 * Created: March 4, 2007
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
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
class NodeStatusView extends PageableTableView {
    
    private NodeRtc[] m_rtcs;
    
    NodeStatusView(Dashlet dashlet) {
		super(dashlet, 5, new String[] { "Node", "Current Outages", "24 Hour Availability" });
	}
    
    protected void setRow(FlexTable table, int row, int elementIndex) {
        NodeRtc rtc = m_rtcs[elementIndex];
        
        if (rtc.getIsDashboardRole()) {
             table.setText(row, 0, rtc.getNodeLabel());
        } else {
            table.setHTML(row, 0, "<a href=\"element/node.jsp?node=" + rtc.getNodeId() + "\">" + rtc.getNodeLabel() + "</a>");
        }
        
        table.setText(row, 1, rtc.getDownServiceCount() + " of " + rtc.getServiceCount());
        table.getCellFormatter().setStyleName(row, 1, rtc.getServiceStyle());
        
        table.setText(row, 2, rtc.getAvailability());
        table.getCellFormatter().setStyleName(row, 2, rtc.getAvailabilityStyle());
        
        table.getRowFormatter().setStyleName(row, "CellStatus");
    }
    
    public int getElementCount() {
        return (m_rtcs == null ? 0 : m_rtcs.length);
    }

	protected void formatCells(FlexTable table, int row) {
        // Don't call the super implementation... it will erase our NodeRtc-specific styling on columns 1 and 2

        table.getCellFormatter().addStyleName(row, 1, "bright");
        table.getCellFormatter().addStyleName(row, 1, "divider");
        
        table.getCellFormatter().addStyleName(row, 2, "bright");
        table.getCellFormatter().addStyleName(row, 2, "divider");
	}

    public void setNodeRtc(NodeRtc[] rtcs) {
        m_rtcs = rtcs;
        refresh();
    }
    
}
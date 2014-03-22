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
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
class NodeStatusView extends PageableTableView {
    
    private NodeRtc[] m_rtcs;
    
    NodeStatusView(Dashlet dashlet) {
		super(dashlet, 5, new String[] { "Node", "Current Outages", "24 Hour Availability" });
	}
    
    /** {@inheritDoc} */
    @Override
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
    
    /**
     * <p>getElementCount</p>
     *
     * @return a int.
     */
    @Override
    public int getElementCount() {
        return (m_rtcs == null ? 0 : m_rtcs.length);
    }

	/** {@inheritDoc} */
    @Override
	protected void formatCells(FlexTable table, int row) {
        // Don't call the super implementation... it will erase our NodeRtc-specific styling on columns 1 and 2

        table.getCellFormatter().addStyleName(row, 1, "bright");
        table.getCellFormatter().addStyleName(row, 1, "divider");
        
        table.getCellFormatter().addStyleName(row, 2, "bright");
        table.getCellFormatter().addStyleName(row, 2, "divider");
	}

    /**
     * <p>setNodeRtc</p>
     *
     * @param rtcs an array of {@link org.opennms.dashboard.client.NodeRtc} objects.
     */
    public void setNodeRtc(NodeRtc[] rtcs) {
        m_rtcs = rtcs;
        refresh();
    }
    
}

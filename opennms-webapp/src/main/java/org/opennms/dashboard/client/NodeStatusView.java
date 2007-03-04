/**
 * 
 */
package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.FlexTable;


class NodeStatusView extends PageableTableView {
    
    private NodeRtc[] m_rtcs;
    
    NodeStatusView(Dashlet dashlet) {
		super(dashlet, 5, new String[] { "Node", "Current Outages", "24 Hour Availability" });
	}
    
    protected void setRow(FlexTable table, int row, int elementIndex) {
        NodeRtc rtc = m_rtcs[elementIndex];
        
        table.setText(row, 0, rtc.getNodeLabel());
        
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
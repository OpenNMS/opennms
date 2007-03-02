/**
 * 
 */
package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.FlexTable;


class AlarmView extends PageableTableView {
    
    private Alarm[] m_alarms;
    
    AlarmView() {
		super(new String[] { "Node", "Description", "Count" });
	}
    
    public void setAlarms(Alarm[] alarms) {
        m_alarms = alarms;
        refresh();
        
    }
    
    protected void setRow(FlexTable table, int row, int elementIndex) {
    	Alarm alarm = m_alarms[elementIndex];
        table.setText(row, 0, alarm.getNodeLabel());
        table.setHTML(row, 1, alarm.getDescrption());
        table.setText(row, 2, ""+alarm.getCount());
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
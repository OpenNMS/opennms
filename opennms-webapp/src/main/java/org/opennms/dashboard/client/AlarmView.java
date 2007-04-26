/**
 * 
 */
package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;


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
        table.setText(row, 0, alarm.getNodeLabel());
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
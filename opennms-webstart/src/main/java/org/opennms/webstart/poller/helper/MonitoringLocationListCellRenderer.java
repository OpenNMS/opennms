package org.opennms.webstart.poller.helper;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

public class MonitoringLocationListCellRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        OnmsMonitoringLocationDefinition location = (OnmsMonitoringLocationDefinition)value;
        
        String stringValue = location.getArea() + " - " + location.getName();
        return super.getListCellRendererComponent(list, stringValue, index, isSelected, cellHasFocus);
    }

    
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.poller.remote;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

/**
 * <p>MonitoringLocationListCellRenderer class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class MonitoringLocationListCellRenderer extends DefaultListCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6166605236770433826L;

	/** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        OnmsMonitoringLocationDefinition location = (OnmsMonitoringLocationDefinition)value;
        
        StringBuffer stringValue = new StringBuffer();
        if (location != null) {
            stringValue.append(location.getArea());
            stringValue.append(" - ");
            stringValue.append(location.getName());
        } else {
            stringValue.append("[null]");
        }
        return super.getListCellRendererComponent(list, stringValue.toString(), index, isSelected, cellHasFocus);
    }

    
}

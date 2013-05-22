/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.browsers;

import org.opennms.netmgt.model.OnmsSeverity;

import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;

public class AlarmTableCellStyleGenerator implements CellStyleGenerator {

	private static final long serialVersionUID = 5083664924723259566L;

	@Override
	public String getStyle(Table table, Object itemId, Object propertyId) {
		if (propertyId == null && table.getContainerProperty(itemId, "severityId") != null) {
			StringBuffer retval = new StringBuffer();
			Integer severity = (Integer)table.getContainerProperty(itemId, "severityId").getValue();
			Property<Boolean> prop = table.getContainerProperty(itemId, "acknowledged");
			Boolean acknowledged = false;
			if (prop != null) {
				acknowledged = prop.getValue();
			}

			if (OnmsSeverity.CLEARED.getId() == severity) {
			} else if (OnmsSeverity.CRITICAL.getId() == severity) {
				retval.append("alarm-critical");
			} else if (OnmsSeverity.INDETERMINATE.getId() == severity) {
				retval.append("alarm-indeterminate");
			} else if (OnmsSeverity.MAJOR.getId() == severity) {
				retval.append("alarm-major");
			} else if (OnmsSeverity.MINOR.getId() == severity) {
				retval.append("alarm-minor");
			} else if (OnmsSeverity.NORMAL.getId() == severity) {
				retval.append("alarm-normal");
			} else if (OnmsSeverity.WARNING.getId() == severity) {
				retval.append("alarm-warning");
			}

			if (!acknowledged) {
				retval.append("-noack");
			}

			return retval.toString();
		} else if ("severity".equals(propertyId)) {
			return "bright";
		}
		return null;
	}
}

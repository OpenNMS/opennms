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
import org.springframework.beans.factory.InitializingBean;

import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;

public class AlarmTableCellStyleGenerator implements CellStyleGenerator, TableAware, InitializingBean {

	private static final long serialVersionUID = 5083664924723259566L;

	private Table m_table;

	@Override
	public void setTable(Table table) {
		m_table = table;
	}

	@Override
	public String getStyle(Object itemId, Object propertyId) {
		if (propertyId == null) {
			Integer severity = (Integer)m_table.getContainerProperty(itemId, "severityId").getValue();
			if (OnmsSeverity.CLEARED.getId() == severity) {
			} else if (OnmsSeverity.CRITICAL.getId() == severity) {
				return "alarm-table-critical";
			} else if (OnmsSeverity.INDETERMINATE.getId() == severity) {
				return "alarm-table-indeterminate";
			} else if (OnmsSeverity.MAJOR.getId() == severity) {
				return "alarm-table-major";
			} else if (OnmsSeverity.MINOR.getId() == severity) {
				return "alarm-table-minor";
			} else if (OnmsSeverity.NORMAL.getId() == severity) {
				return "alarm-table-normal";
			} else if (OnmsSeverity.WARNING.getId() == severity) {
				return "alarm-table-warning";
			}
		} else if ("severity".equals(propertyId)) {
			return "bright";
		}
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (m_table == null) {
			throw new IllegalStateException("m_table not set");
		}
	}
}

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.db;

import java.util.List;

public class Table {
	private String m_name;
	private List<Column> m_columns;
	private List<Constraint> m_constraints;
	
	public List<Column> getColumns() {
		return m_columns;
	}
	public void setColumns(List<Column> columns) {
		m_columns = columns;
	}
	
	public List<Constraint> getConstraints() {
		return m_constraints;
	}
	public void setConstraints(List<Constraint> constraints) {
		m_constraints = constraints;
	}
	
	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		m_name = name.toLowerCase();
	}
	
	public void setNotNullOnPrimaryKeyColumns() throws Exception {
		for (Constraint constraint : getConstraints()) {
			if (constraint.isPrimaryKeyConstraint()) {
				for (String constrainedColumn : constraint.getColumns()) {
					boolean foundColumn = false;
					for (Column column : getColumns()) {
						if (constrainedColumn.equals(column.getName())) {
							column.setNotNull(true);
							foundColumn = true;
							break;
						}
					}
					if (!foundColumn) {
						throw new Exception("could not find column '" + constrainedColumn + "' for constraint: " + constraint);
					}
				}
			}
		}
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Table)) {
			return false;
			
		}
		Table other = (Table) o;

		if ((m_name == null && other.getName() != null) || (m_name != null && other.getName() == null)) {
            return false;
        }
        if (m_name != null && other.getName() != null && !m_name.equals(other.getName())) {
            return false;
        }

        if ((m_columns == null && other.getColumns() != null) || (m_columns != null && other.getColumns() == null)) {
            return false;
        }
        if (m_columns != null && other.getColumns() != null && !m_columns.equals(other.getColumns())) {
            return false;
        }
        
		if ((m_constraints == null && other.getConstraints() != null) || (m_constraints != null && other.getConstraints() == null)) {
            return false;
        }
        if (m_constraints != null && other.getConstraints() != null && !m_constraints.equals(other.getConstraints())) {
            return false;
        }

		return true;
	}
	
}

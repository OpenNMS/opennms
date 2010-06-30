//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.db;

import java.util.List;

/**
 * <p>Table class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Table {
	private String m_name;
	private List<Column> m_columns;
	private List<Constraint> m_constraints;
	
	/**
	 * <p>getColumns</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Column> getColumns() {
		return m_columns;
	}
	/**
	 * <p>setColumns</p>
	 *
	 * @param columns a {@link java.util.List} object.
	 */
	public void setColumns(List<Column> columns) {
		m_columns = columns;
	}
	
	/**
	 * <p>getConstraints</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Constraint> getConstraints() {
		return m_constraints;
	}
	/**
	 * <p>setConstraints</p>
	 *
	 * @param constraints a {@link java.util.List} object.
	 */
	public void setConstraints(List<Constraint> constraints) {
		m_constraints = constraints;
	}
	
	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}
	/**
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		m_name = name.toLowerCase();
	}
	
	/**
	 * <p>setNotNullOnPrimaryKeyColumns</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
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
	
	/** {@inheritDoc} */
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

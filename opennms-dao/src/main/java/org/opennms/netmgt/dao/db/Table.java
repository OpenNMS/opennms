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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
	public void setColumns(final List<Column> columns) {
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
	public void setConstraints(final List<Constraint> constraints) {
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
	public void setName(final String name) {
		m_name = name.toLowerCase();
	}
	
	/**
	 * <p>setNotNullOnPrimaryKeyColumns</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void setNotNullOnPrimaryKeyColumns() throws Exception {
		for (final Constraint constraint : getConstraints()) {
			if (constraint.isPrimaryKeyConstraint()) {
				for (final String constrainedColumn : constraint.getColumns()) {
					boolean foundColumn = false;
					for (final Column column : getColumns()) {
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
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof Table)) {
			return false;
			
		}
		final Table other = (Table) o;

		return new EqualsBuilder()
			.append(getColumns(), other.getColumns())
			.append(getConstraints(), other.getConstraints())
			.append(getName(), other.getName())
			.isEquals();
	}
	
	public int hashCode() {
		return new HashCodeBuilder(3, 73)
			.append(m_name)
			.append(m_columns)
			.append(m_constraints)
			.toHashCode();
	}
}

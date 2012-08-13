/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.db.install;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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

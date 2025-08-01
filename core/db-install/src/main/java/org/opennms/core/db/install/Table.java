/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        @Override
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
	
        @Override
	public int hashCode() {
		return new HashCodeBuilder(3, 73)
			.append(m_name)
			.append(m_columns)
			.append(m_constraints)
			.toHashCode();
	}
}
